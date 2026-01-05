/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package report

import (
	"context"
	_ "embed"
	"encoding/json"
	"fmt"
	"io"
	"jar-analyzer-mcp/pkg/db"
	"jar-analyzer-mcp/pkg/log"
	"jar-analyzer-mcp/pkg/model"
	"net/http"
	"strings"
	"sync"

	"github.com/gorilla/websocket"
	"github.com/mark3labs/mcp-go/mcp"
	"github.com/mark3labs/mcp-go/server"
)

var WebAddr string

type WebSocketManager struct {
	clients    map[*websocket.Conn]bool
	broadcast  chan model.ReportData
	register   chan *websocket.Conn
	unregister chan *websocket.Conn
	mutex      sync.RWMutex
}

var wsManager *WebSocketManager

//go:embed index.html
var indexHtml string

func NewWebSocketManager() *WebSocketManager {
	return &WebSocketManager{
		clients:    make(map[*websocket.Conn]bool),
		broadcast:  make(chan model.ReportData, 100), // Increase buffer size to 100
		register:   make(chan *websocket.Conn),
		unregister: make(chan *websocket.Conn),
	}
}

func (manager *WebSocketManager) Run() {
	for {
		select {
		case conn := <-manager.register:
			manager.mutex.Lock()
			manager.clients[conn] = true
			manager.mutex.Unlock()
		case conn := <-manager.unregister:
			manager.mutex.Lock()
			if _, ok := manager.clients[conn]; ok {
				delete(manager.clients, conn)
				conn.Close()
			}
			manager.mutex.Unlock()
		case data := <-manager.broadcast:
			manager.mutex.RLock()
			for client := range manager.clients {
				err := client.WriteJSON(data)
				if err != nil {
					client.Close()
					delete(manager.clients, client)
				}
			}
			manager.mutex.RUnlock()
		}
	}
}

func (manager *WebSocketManager) BroadcastData(data model.ReportData) {
	select {
	case manager.broadcast <- data:
	default:
		log.Warn("broadcast channel full, dropping message")
	}
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

func handleWebSocket(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Warn("web socket upgrade failed: %v", err)
		return
	}
	wsManager.register <- conn
	defer func() {
		wsManager.unregister <- conn
	}()
	for {
		_, _, err := conn.ReadMessage()
		if err != nil {
			break
		}
	}
}
func parseArgs(argMap any, target interface{}) error {
	data, err := json.Marshal(argMap)
	if err != nil {
		return err
	}
	return json.Unmarshal(data, target)
}
func RegisterReportTools(s *server.MCPServer, webPort int, addr string) {
	WebAddr = addr

	wsManager = NewWebSocketManager()
	go wsManager.Run()

	log.Debug("start websocket manager")

	reportTool := mcp.NewTool("report",
		mcp.WithDescription("report vulnerable tool"),
		mcp.WithString("type", mcp.Required(), mcp.Description("vulnerable type")),
		mcp.WithString("reason", mcp.Required(), mcp.Description("vulnerable reason")),
		mcp.WithNumber("score", mcp.Required(), mcp.Description("vulnerable score(max:10,min:1)")),
		mcp.WithArray("trace", mcp.Required(), mcp.Description("vulnerable trace"),
			mcp.Items(map[string]any{
				"type": "object",
				"properties": map[string]any{
					"class":  map[string]any{"type": "string"},
					"method": map[string]any{"type": "string"},
					"desc":   map[string]any{"type": "string"},
				},
			})),
	)

	log.Debug("init mcp tool")

	s.AddTool(reportTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		var args model.ReportData
		if err := parseArgs(req.Params.Arguments, &args); err != nil {
			return nil, fmt.Errorf("参数解析失败: %v", err)
		}
		return handleReport(args)
	})

	log.Debug("add mcp tool finish")

	go func() {
		http.HandleFunc("/ws", handleWebSocket)
		http.HandleFunc("/api/history", handleHistory)
		http.HandleFunc("/api/proxy", handleProxy)
		http.HandleFunc("/", handleIndex)
		if err := http.ListenAndServe(fmt.Sprintf(":%d", webPort), nil); err != nil {
			log.Warnf("http server error: %v", err)
		}
	}()

	log.Debug("start web server finish")
}

func handleIndex(writer http.ResponseWriter, req *http.Request) {
	writer.Header().Add("Content-Type", "text/html")
	htmlNewStr := strings.ReplaceAll(indexHtml, "__JAR_ANALYZER_REPORT_MCP__", WebAddr)
	log.Debugf("receive request: %s", req.RemoteAddr)
	_, _ = writer.Write([]byte(htmlNewStr))
}

func handleHistory(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	reports, err := db.GetReports()
	if err != nil {
		log.Errorf("get reports error: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	if err := json.NewEncoder(w).Encode(reports); err != nil {
		log.Errorf("encode reports error: %v", err)
	}
}

func handleProxy(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "GET, OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "*")

	if r.Method == "OPTIONS" {
		return
	}

	targetURL := r.URL.Query().Get("url")
	if targetURL == "" {
		http.Error(w, "missing url parameter", http.StatusBadRequest)
		return
	}

	log.Debugf("proxying request to: %s", targetURL)

	resp, err := http.Get(targetURL)
	if err != nil {
		log.Errorf("proxy error: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	defer resp.Body.Close()

	w.Header().Set("Content-Type", resp.Header.Get("Content-Type"))
	w.WriteHeader(resp.StatusCode)
	
	// Copy the response body to the writer
	// Since we are proxying, we don't need to parse it
	_, _ = io.Copy(w, resp.Body)
}

func handleReport(reportData model.ReportData) (*mcp.CallToolResult, error) {
	wsManager.BroadcastData(reportData)
	err := db.SaveReport(reportData)
	if err != nil {
		log.Errorf("save report error: %v", err)
		return nil, err
	}
	log.Infof("received report data. type: %s, reason: %s, trace: %v", reportData.Type, reportData.Reason, reportData.Trace)
	return mcp.NewToolResultText("report data received and broadcasted successfully"), nil
}

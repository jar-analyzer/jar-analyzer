package report

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"strings"
	"sync"

	"github.com/gorilla/websocket"
	"github.com/mark3labs/mcp-go/mcp"
	"github.com/mark3labs/mcp-go/server"
)

type ReportData struct {
	Type   string  `json:"type"`
	Reason string  `json:"reason"`
	Trace  []Trace `json:"trace"`
}

type Trace struct {
	Class  string `json:"class"`
	Method string `json:"method"`
}

type WebSocketManager struct {
	clients    map[*websocket.Conn]bool
	broadcast  chan []ReportData
	register   chan *websocket.Conn
	unregister chan *websocket.Conn
	mutex      sync.RWMutex
}

var wsManager *WebSocketManager

func NewWebSocketManager() *WebSocketManager {
	return &WebSocketManager{
		clients:    make(map[*websocket.Conn]bool),
		broadcast:  make(chan []ReportData),
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

func (manager *WebSocketManager) BroadcastData(data []ReportData) {
	select {
	case manager.broadcast <- data:
	default:
		log.Println("broadcast channel full, dropping message")
	}
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true // 允许所有来源
	},
}

func handleWebSocket(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("webSocket upgrade failed: %v", err)
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

func RegisterReportTools(s *server.MCPServer) {
	wsManager = NewWebSocketManager()
	go wsManager.Run()

	reportTool := mcp.NewTool("report",
		mcp.WithDescription("接收JSON格式的报告数据并实时显示在Web UI上"),
		mcp.WithString("data", mcp.Required(), mcp.Description("JSON格式的报告数据")),
	)

	s.AddTool(reportTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		dataStr, err := req.RequireString("data")
		if err != nil {
			return mcp.NewToolResultError("Invalid data parameter"), fmt.Errorf("data parameter must be a string: %v", err)
		}
		return handleReport(ctx, dataStr)
	})

	go func() {
		http.HandleFunc("/ws", handleWebSocket)
		http.HandleFunc("/", handleIndex)
		if err := http.ListenAndServe(fmt.Sprintf(":20080"), nil); err != nil {
			log.Printf("http server error: %v", err)
		}
	}()
}

func handleIndex(writer http.ResponseWriter, request *http.Request) {
	writer.Header().Add("Content-Type", "text/html")
	htmlBytes, err := base64.StdEncoding.DecodeString(htmlBase64)
	if err == nil {
		htmlStr := string(htmlBytes)
		htmlNewStr := strings.ReplaceAll(htmlStr, "__JAR_ANALYZER_REPORT_MCP__", "127.0.0.1:20080")
		_, _ = writer.Write([]byte(htmlNewStr))
	}
}

func handleReport(_ context.Context, dataStr string) (*mcp.CallToolResult, error) {
	var reportData []ReportData
	if err := json.Unmarshal([]byte(dataStr), &reportData); err != nil {
		return mcp.NewToolResultError("Invalid JSON format"), fmt.Errorf("failed to parse JSON: %v", err)
	}
	wsManager.BroadcastData(reportData)
	log.Printf("Received report data with %d items", len(reportData))
	return mcp.NewToolResultText("Report data received and broadcasted successfully"), nil
}

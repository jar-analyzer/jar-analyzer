package report

import (
	"context"
	_ "embed"
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
	Score  int8    `json:"score"`
	Trace  []Trace `json:"trace"`
}

type Trace struct {
	Class  string `json:"class"`
	Method string `json:"method"`
}

type WebSocketManager struct {
	clients    map[*websocket.Conn]bool
	broadcast  chan ReportData
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
		broadcast:  make(chan ReportData),
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

func (manager *WebSocketManager) BroadcastData(data ReportData) {
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
func parseArgs(argMap any, target interface{}) error {
	// 简易实现：通过JSON序列化反序列化。确保target是一个结构体指针。
	data, err := json.Marshal(argMap)
	if err != nil {
		return err
	}
	return json.Unmarshal(data, target)
}
func RegisterReportTools(s *server.MCPServer) {
	wsManager = NewWebSocketManager()
	go wsManager.Run()

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
				},
			})),
	)

	s.AddTool(reportTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		var args ReportData
		if err := parseArgs(req.Params.Arguments, &args); err != nil {
			return nil, fmt.Errorf("参数解析失败: %v", err)
		}
		return handleReport(args)
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
	htmlNewStr := strings.ReplaceAll(indexHtml, "__JAR_ANALYZER_REPORT_MCP__", "127.0.0.1:20080")
	_, _ = writer.Write([]byte(htmlNewStr))
}

func handleReport(reportData ReportData) (*mcp.CallToolResult, error) {

	// 广播数据到所有WebSocket客户端
	wsManager.BroadcastData(reportData)

	log.Printf("Received report data. type: %s, reason: %s, trace: %v", reportData.Type, reportData.Reason, reportData.Trace)

	return mcp.NewToolResultText("Report data received and broadcasted successfully"), nil
}

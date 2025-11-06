package tools

import (
	"github.com/mark3labs/mcp-go/server"
)

func RegisterAllTools(s *server.MCPServer) {
	RegisterMethodClassTools(s)
	RegisterCallGraphTools(s)
	RegisterSpringTools(s)
	RegisterJavaWebTools(s)
	RegisterCodeTools(s)
}

package main

import (
	"log"

	"github.com/mark3labs/mcp-go/server"
	"jar-analyzer-mcp/pkg/tools"
)

func main() {
	s := server.NewMCPServer(
		"jar-analyzer-mcp",
		"0.1.0",
		server.WithToolCapabilities(false),
		server.WithRecovery(),
	)
	tools.RegisterAllTools(s)
	sseServer := server.NewSSEServer(s)
	if err := sseServer.Start(":20032"); err != nil {
		log.Fatal(err)
	}
}

package main

import (
	"flag"
	"fmt"
	"log"

	"github.com/mark3labs/mcp-go/server"
	"jar-analyzer-mcp/pkg/conf"
	"jar-analyzer-mcp/pkg/tools"
)

const (
	version = "1.0.0"
	name    = "jar-analyzer-mcp"
)

func main() {
	var port int
	var jarAnalyzerUrl string

	flag.IntVar(&port, "port", 20032, "port to listen on")
	flag.StringVar(&jarAnalyzerUrl, "url", "http://127.0.0.1:10032", "Jar Analyzer URL")
	flag.Parse()

	conf.GlobalPort = port
	conf.GlobalJarAnalyzerUrl = jarAnalyzerUrl

	s := server.NewMCPServer(
		name,
		version,
		server.WithToolCapabilities(false),
		server.WithRecovery(),
	)
	tools.RegisterAllTools(s)
	sseServer := server.NewSSEServer(s)
	if err := sseServer.Start(fmt.Sprintf(":%d", conf.GlobalPort)); err != nil {
		log.Fatal(err)
	}
}

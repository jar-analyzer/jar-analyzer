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
	fmt.Println("     ____.               _____                .__                              \n" +
		"    |    |____ _______  /  _  \\   ____ _____  |  | ___.__.________ ___________ \n" +
		"    |    \\__  \\\\_  __ \\/  /_\\  \\ /    \\\\__  \\ |  |<   |  |\\___   // __ \\_  __ \\\n" +
		"/\\__|    |/ __ \\|  | \\/    |    \\   |  \\/ __ \\|  |_\\___  | /    /\\  ___/|  | \\/\n" +
		"\\________(____  /__|  \\____|__  /___|  (____  /____/ ____|/_____ \\\\___  >__|   \n" +
		"              \\/              \\/     \\/     \\/     \\/           \\/    \\/       ")
	fmt.Println("jar-analyzer-mcp (https://github.com/jar-analyzer/jar-analyzer)")
	fmt.Printf("version: %s usage: %s\n", version, "[mcp.exe -port 20032 -url http://127.0.0.1:10032]")

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

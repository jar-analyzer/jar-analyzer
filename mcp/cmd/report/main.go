/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package main

import (
	"flag"
	"fmt"
	"github.com/mark3labs/mcp-go/server"
	"jar-analyzer-mcp/pkg/db"
	"jar-analyzer-mcp/pkg/log"
	"jar-analyzer-mcp/pkg/report"
)

const (
	version = "1.2.2"
	name    = "jar-analyzer-report-mcp"
)

func main() {
	fmt.Println("     ____.               _____                .__                              \n" +
		"    |    |____ _______  /  _  \\   ____ _____  |  | ___.__.________ ___________ \n" +
		"    |    \\__  \\\\_  __ \\/  /_\\  \\ /    \\\\__  \\ |  |<   |  |\\___   // __ \\_  __ \\\n" +
		"/\\__|    |/ __ \\|  | \\/    |    \\   |  \\/ __ \\|  |_\\___  | /    /\\  ___/|  | \\/\n" +
		"\\________(____  /__|  \\____|__  /___|  (____  /____/ ____|/_____ \\\\___  >__|   \n" +
		"              \\/              \\/     \\/     \\/     \\/           \\/    \\/       ")
	fmt.Println("jar-analyzer-report-mcp (https://github.com/jar-analyzer/jar-analyzer)")
	fmt.Printf("version: %s usage: %s\n", version, "mcp.exe -port 20081 -web-host 127.0.0.1 -web-port 20080 -debug")

	var port int
	var webPort int
	var webHost string
	var debug bool

	flag.IntVar(&port, "port", 20081, "mcp port to listen on")
	flag.StringVar(&webHost, "web-host", "127.0.0.1", "mcp web server host")
	flag.IntVar(&webPort, "web-port", 20080, "web server port to listen on")
	flag.BoolVar(&debug, "debug", false, "enable debug mode")
	flag.Parse()

	if debug {
		log.SetLevel(log.DebugLevel)
	} else {
		log.SetLevel(log.InfoLevel)
	}

	fmt.Println("------------------------------------------------------------------")
	fmt.Println("[INFO] Starting Jar Analyzer Report MCP Server...")
	fmt.Println("[信息] 正在启动 Jar Analyzer Report MCP 服务器...")
	fmt.Println("------------------------------------------------------------------")
	fmt.Printf("[CONF] MCP Listen Port (MCP 监听端口): %d\n", port)
	fmt.Printf("[CONF] Web Server Host (Web 主机地址): %s\n", webHost)
	fmt.Printf("[CONF] Web Server Port (Web 监听端口): %d\n", webPort)
	fmt.Printf("[CONF] Debug Mode (调试模式): %v\n", debug)
	fmt.Println("------------------------------------------------------------------")
	fmt.Println("[HINT] Please ensure the Jar Analyzer database is initialized")
	fmt.Println("[提示] 请确保 Jar Analyzer 数据库已初始化")
	fmt.Println("------------------------------------------------------------------")

	if err := db.InitDB(); err != nil {
		log.Errorf("init db error: %v", err)
		return
	}
	defer db.CloseDB()

	mcpServer := server.NewMCPServer(
		name,
		version,
		server.WithToolCapabilities(false),
		server.WithRecovery(),
	)

	report.RegisterReportTools(mcpServer, webPort, fmt.Sprintf("%s:%d", webHost, webPort))
	sseServer := server.NewSSEServer(mcpServer)
	if err := sseServer.Start(fmt.Sprintf(":%d", port)); err != nil {
		log.Error(err)
	}
}

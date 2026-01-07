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
	"jar-analyzer-mcp/pkg/log"

	"github.com/mark3labs/mcp-go/server"
	"jar-analyzer-mcp/pkg/conf"
	"jar-analyzer-mcp/pkg/tools"
)

const (
	version = "1.2.1"
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

	var debug bool
	var port int
	var mcpAuth bool
	var mcpToken string

	var jarAnalyzerUrl string
	var jarAnAuth bool
	var jarAnToken string

	flag.IntVar(&port, "port", 20032, "port to listen on")
	flag.BoolVar(&mcpAuth, "auth", false, "enable mcp auth")
	flag.StringVar(&mcpToken, "token", "JAR-ANALYZER-MCP-TOKEN", "mcp token")
	flag.BoolVar(&debug, "debug", false, "debug mode")

	// JAR-ANALYZER CONFIG
	flag.StringVar(&jarAnalyzerUrl, "url", "http://127.0.0.1:10032", "Jar Analyzer URL")
	flag.BoolVar(&jarAnAuth, "ja", false, "enable jar-analyzer-api token")
	flag.StringVar(&jarAnToken, "jt", "JAR-ANALYZER-API-TOKEN", "jar-analyzer-api token")

	flag.Parse()

	conf.GlobalPort = port
	conf.McpAuth = mcpAuth
	conf.McpToken = mcpToken

	conf.GlobalJarAnalyzerUrl = jarAnalyzerUrl
	conf.JarAnalyzerAuth = jarAnAuth
	conf.JarAnalyzerToken = jarAnToken

	if debug {
		log.SetLevel(log.DebugLevel)
	} else {
		log.SetLevel(log.InfoLevel)
	}

	fmt.Println("------------------------------------------------------------------")
	fmt.Println("[INFO] Starting Jar Analyzer MCP Server...")
	fmt.Println("[信息] 正在启动 Jar Analyzer MCP 服务器...")
	fmt.Println("------------------------------------------------------------------")
	fmt.Printf("[CONF] Listen Port (监听端口): %d\n", port)
	fmt.Printf("[CONF] Backend URL (后端地址): %s\n", jarAnalyzerUrl)
	if mcpAuth {
		fmt.Printf("[CONF] MCP Auth (MCP鉴权): Enabled (开启) [Token: %s]\n", mcpToken)
	} else {
		fmt.Printf("[CONF] MCP Auth (MCP鉴权): Disabled (关闭)\n")
	}

	if jarAnAuth {
		fmt.Printf("[CONF] Backend Auth (后端鉴权): Enabled (开启) [Token: %s]\n", jarAnToken)
	} else {
		fmt.Printf("[CONF] Backend Auth (后端鉴权): Disabled (关闭)\n")
	}

	fmt.Printf("[CONF] Debug Mode (调试模式): %v\n", debug)
	fmt.Println("------------------------------------------------------------------")
	fmt.Println("[HINT] Please ensure Jar Analyzer is running at the backend URL")
	fmt.Println("[提示] 请确保 Jar Analyzer 正在后端地址运行")
	fmt.Println("[HINT] Use an MCP client (like Claude Desktop) to connect to this server")
	fmt.Println("[提示] 使用 MCP 客户端 (如 Claude Desktop) 连接到此服务器")
	fmt.Printf("[HINT] Connection URL: http://localhost:%d/sse\n", port)
	fmt.Printf("[提示] 连接地址: http://localhost:%d/sse\n", port)
	fmt.Println("------------------------------------------------------------------")

	s := server.NewMCPServer(
		name,
		version,
		server.WithToolCapabilities(false),
		server.WithRecovery(),
	)
	tools.RegisterAllTools(s)
	sseServer := server.NewSSEServer(s)
	if err := sseServer.Start(fmt.Sprintf(":%d", conf.GlobalPort)); err != nil {
		log.Error(err)
	}
}

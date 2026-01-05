/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
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
	version = "1.2.0"
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

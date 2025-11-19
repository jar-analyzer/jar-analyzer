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
	"fmt"
	"github.com/mark3labs/mcp-go/server"
	"jar-analyzer-mcp/pkg/report"
	"log"
)

// mcp-report 测试
// 有需要可以使用 非核心 MCP
// 固定 MCP 端口 20081
// 固定 MCP WEB 端口 20080
func main() {
	mcpServer := server.NewMCPServer(
		"report-service",
		"1.0.0",
	)
	report.RegisterReportTools(mcpServer)
	sseServer := server.NewSSEServer(mcpServer)
	if err := sseServer.Start(fmt.Sprintf(":%d", 20081)); err != nil {
		log.Fatal(err)
	}
}

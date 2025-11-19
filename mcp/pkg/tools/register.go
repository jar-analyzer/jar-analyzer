/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package tools

import (
	"github.com/mark3labs/mcp-go/server"
	"jar-analyzer-mcp/pkg/log"
)

func RegisterAllTools(s *server.MCPServer) {
	RegisterMethodClassTools(s)
	RegisterCallGraphTools(s)
	RegisterSpringTools(s)
	RegisterJavaWebTools(s)
	RegisterCodeTools(s)
	log.Debug("register all tools")
}

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
	"context"
	"jar-analyzer-mcp/pkg/conf"
	"jar-analyzer-mcp/pkg/log"
	"net/url"

	"jar-analyzer-mcp/pkg/util"

	"github.com/mark3labs/mcp-go/mcp"
	"github.com/mark3labs/mcp-go/server"
)

func RegisterCodeTools(s *server.MCPServer) {
	getCodeFernflowerTool := mcp.NewTool("get_code_fernflower",
		mcp.WithDescription("反编译并提取指定方法代码（Fernflower）"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名（点或斜杠分隔均可）")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getCodeFernflowerTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		className, err := req.RequireString("class")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		methodName, err := req.RequireString("method")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		desc := req.GetString("desc", "")
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_code_fernflower", className, methodName, desc)
		params := url.Values{"class": []string{className}, "method": []string{methodName}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/fernflower_code", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})
	// CFR 反编译器
	getCodeCFRTool := mcp.NewTool("get_code_cfr",
		mcp.WithDescription("反编译并提取指定方法代码（CFR）"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名（点或斜杠分隔均可）")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getCodeCFRTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		className, err := req.RequireString("class")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		methodName, err := req.RequireString("method")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		desc := req.GetString("desc", "")
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_code_cfr", className, methodName, desc)
		params := url.Values{"class": []string{className}, "method": []string{methodName}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/cfr_code", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})
}

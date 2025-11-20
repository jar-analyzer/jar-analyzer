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

func RegisterMethodClassTools(s *server.MCPServer) {
	getMethodsByClassTool := mcp.NewTool("get_methods_by_class",
		mcp.WithDescription("查询指定类中的所有方法信息"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名（点或斜杠分隔均可）")),
	)
	s.AddTool(getMethodsByClassTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
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
		log.Debugf("call %s, class: %s", "get_methods_by_class", className)
		params := url.Values{"class": []string{className}}
		out, err := util.HTTPGet("/api/get_methods_by_class", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getMethodsByStrTool := mcp.NewTool("get_methods_by_str",
		mcp.WithDescription("搜索包含指定字符串(String类型的变量、注解)的方法（模糊）"),
		mcp.WithString("str", mcp.Required(), mcp.Description("搜索关键字")),
	)
	s.AddTool(getMethodsByStrTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		q, err := req.RequireString("str")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		log.Debugf("call %s, str: %s", "get_methods_by_str", q)
		params := url.Values{"str": []string{q}}
		out, err := util.HTTPGet("/api/get_methods_by_str", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getClassByClassTool := mcp.NewTool("get_class_by_class",
		mcp.WithDescription("查询类的基本信息"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名（点或斜杠分隔均可）")),
	)
	s.AddTool(getClassByClassTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
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
		log.Debugf("call %s, class: %s", "get_class_by_class", className)
		params := url.Values{"class": []string{className}}
		out, err := util.HTTPGet("/api/get_class_by_class", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})
}

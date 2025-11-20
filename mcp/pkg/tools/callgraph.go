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

func RegisterCallGraphTools(s *server.MCPServer) {
	callArgs := func(req mcp.CallToolRequest) (string, string, string, *mcp.CallToolResult) {
		clazz, err := req.RequireString("class")
		if err != nil {
			return "", "", "", mcp.NewToolResultError(err.Error())
		}
		method, err := req.RequireString("method")
		if err != nil {
			return "", "", "", mcp.NewToolResultError(err.Error())
		}
		desc := req.GetString("desc", "")
		return clazz, method, desc, nil
	}

	getCallersTool := mcp.NewTool("get_callers",
		mcp.WithDescription("查询方法的所有调用者"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getCallersTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		clazz, method, desc, errRes := callArgs(req)
		if errRes != nil {
			return errRes, nil
		}
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_callers", clazz, method, desc)
		params := url.Values{"class": []string{clazz}, "method": []string{method}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/get_callers", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getCallersLikeTool := mcp.NewTool("get_callers_like",
		mcp.WithDescription("模糊查询方法的调用者"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名（模糊）")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getCallersLikeTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		clazz, method, desc, errRes := callArgs(req)
		if errRes != nil {
			return errRes, nil
		}
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_callers_like", clazz, method, desc)
		params := url.Values{"class": []string{clazz}, "method": []string{method}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/get_callers_like", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getCalleeTool := mcp.NewTool("get_callee",
		mcp.WithDescription("查询方法的被调用者"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getCalleeTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		clazz, method, desc, errRes := callArgs(req)
		if errRes != nil {
			return errRes, nil
		}
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_callee", clazz, method, desc)
		params := url.Values{"class": []string{clazz}, "method": []string{method}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/get_callee", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getMethodTool := mcp.NewTool("get_method",
		mcp.WithDescription("精确查询方法"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getMethodTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		clazz, method, desc, errRes := callArgs(req)
		if errRes != nil {
			return errRes, nil
		}
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_method", clazz, method, desc)
		params := url.Values{"class": []string{clazz}, "method": []string{method}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/get_method", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getMethodLikeTool := mcp.NewTool("get_method_like",
		mcp.WithDescription("模糊查询方法"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名（模糊）")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getMethodLikeTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		clazz, method, desc, errRes := callArgs(req)
		if errRes != nil {
			return errRes, nil
		}
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_method_like", clazz, method, desc)
		params := url.Values{"class": []string{clazz}, "method": []string{method}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/get_method_like", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getImplsTool := mcp.NewTool("get_impls",
		mcp.WithDescription("查询接口/抽象方法的实现"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getImplsTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		clazz, method, desc, errRes := callArgs(req)
		if errRes != nil {
			return errRes, nil
		}
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_impls", clazz, method, desc)
		params := url.Values{"class": []string{clazz}, "method": []string{method}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/get_impls", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getSuperImplsTool := mcp.NewTool("get_super_impls",
		mcp.WithDescription("查询父类/接口的实现"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名")),
		mcp.WithString("method", mcp.Required(), mcp.Description("方法名")),
		mcp.WithString("desc", mcp.Description("方法描述（可选）")),
	)
	s.AddTool(getSuperImplsTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		if conf.McpAuth {
			if req.Header.Get("Token") == "" {
				return mcp.NewToolResultError("need token error"), nil
			}
			if req.Header.Get("Token") != conf.McpToken {
				return mcp.NewToolResultError("need token error"), nil
			}
		}
		clazz, method, desc, errRes := callArgs(req)
		if errRes != nil {
			return errRes, nil
		}
		log.Debugf("call %s, class: %s, method: %s, desc: %s",
			"get_super_impls", clazz, method, desc)
		params := url.Values{"class": []string{clazz}, "method": []string{method}}
		if desc != "" {
			params.Set("desc", desc)
		}
		out, err := util.HTTPGet("/api/get_super_impls", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})
}

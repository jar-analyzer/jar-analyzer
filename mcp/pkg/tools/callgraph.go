/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
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

	dfsAnalyzeTool := mcp.NewTool("dfs_analyze",
		mcp.WithDescription("DFS 调用链分析（sink/source 可点或斜杠分隔）"),
		mcp.WithString("sink_class", mcp.Required(), mcp.Description("sink 类名（点或斜杠分隔均可）")),
		mcp.WithString("sink_method", mcp.Required(), mcp.Description("sink 方法名")),
		mcp.WithString("sink_method_desc", mcp.Description("sink 方法描述（可选）")),
		mcp.WithString("source_class", mcp.Description("source 类名（可选，点或斜杠分隔均可）")),
		mcp.WithString("source_method", mcp.Description("source 方法名（可选）")),
		mcp.WithString("source_method_desc", mcp.Description("source 方法描述（可选）")),
		mcp.WithString("depth", mcp.Description("搜索深度（默认 10）")),
		mcp.WithString("limit", mcp.Description("最大返回数量（默认 10）")),
		mcp.WithString("from_sink", mcp.Description("是否从 sink 开始搜索（默认 true）")),
		mcp.WithString("search_null_source", mcp.Description("是否搜索无 source 的路径（默认 true）")),
	)
	s.AddTool(dfsAnalyzeTool, dfsAnalyzeToolHandler)
}

func dfsAnalyzeToolHandler(_ context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
	if conf.McpAuth {
		if req.Header.Get("Token") == "" {
			return mcp.NewToolResultError("need token error"), nil
		}
		if req.Header.Get("Token") != conf.McpToken {
			return mcp.NewToolResultError("need token error"), nil
		}
	}

	sinkClass, err := req.RequireString("sink_class")
	if err != nil {
		return mcp.NewToolResultError(err.Error()), nil
	}
	sinkMethod, err := req.RequireString("sink_method")
	if err != nil {
		return mcp.NewToolResultError(err.Error()), nil
	}

	sinkDesc := req.GetString("sink_method_desc", "")

	sourceClass := req.GetString("source_class", "")
	sourceMethod := req.GetString("source_method", "")
	sourceDesc := req.GetString("source_method_desc", "")

	depth := req.GetString("depth", "10")
	limit := req.GetString("limit", "10")
	fromSink := req.GetString("from_sink", "true")
	searchNullSource := req.GetString("search_null_source", "true")

	log.Debugf("call %s, sink_class: %s, sink_method: %s, sink_desc: %s, source_class: %s, source_method: %s, source_desc: %s, depth: %s, limit: %s, from_sink: %s, search_null_source: %s",
		"dfs_analyze", sinkClass, sinkMethod, sinkDesc, sourceClass, sourceMethod, sourceDesc, depth, limit, fromSink, searchNullSource)

	params := url.Values{
		"sink_class":         []string{sinkClass},
		"sink_method":        []string{sinkMethod},
		"depth":              []string{depth},
		"limit":              []string{limit},
		"from_sink":          []string{fromSink},
		"search_null_source": []string{searchNullSource},
	}
	if sinkDesc != "" {
		params.Set("sink_method_desc", sinkDesc)
	}
	if sourceClass != "" {
		params.Set("source_class", sourceClass)
	}
	if sourceMethod != "" {
		params.Set("source_method", sourceMethod)
	}
	if sourceDesc != "" {
		params.Set("source_method_desc", sourceDesc)
	}

	out, err := util.HTTPGet("/api/dfs_analyze", params)
	if err != nil {
		return mcp.NewToolResultError(err.Error()), nil
	}
	return mcp.NewToolResultText(out), nil
}

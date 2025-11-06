package tools

import (
	"context"
	"net/url"

	"jar-analyzer-mcp/pkg/util"

	"github.com/mark3labs/mcp-go/mcp"
	"github.com/mark3labs/mcp-go/server"
)

func RegisterMethodClassTools(s *server.MCPServer) {
	getMethodsByClassTool := mcp.NewTool("get_methods_by_class",
		mcp.WithDescription("查询某类下的方法信息"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名（点或斜杠分隔均可）")),
	)
	s.AddTool(getMethodsByClassTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		className, err := req.RequireString("class")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		params := url.Values{"class": []string{className}}
		out, err := util.HTTPGet("/api/get_methods_by_class", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getMethodsByStrTool := mcp.NewTool("get_methods_by_str",
		mcp.WithDescription("按字符串搜索方法（模糊）"),
		mcp.WithString("str", mcp.Required(), mcp.Description("搜索关键字")),
	)
	s.AddTool(getMethodsByStrTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		q, err := req.RequireString("str")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
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
		className, err := req.RequireString("class")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		params := url.Values{"class": []string{className}}
		out, err := util.HTTPGet("/api/get_class_by_class", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})
}
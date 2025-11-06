package tools

import (
	"context"
	"jar-analyzer-mcp/pkg/util"

	"github.com/mark3labs/mcp-go/mcp"
	"github.com/mark3labs/mcp-go/server"
)

func RegisterJavaWebTools(s *server.MCPServer) {
	getAllFiltersTool := mcp.NewTool("get_all_filters",
		mcp.WithDescription("列出所有 Java Web Filters 类"),
	)
	s.AddTool(getAllFiltersTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		out, err := util.HTTPGet("/api/get_all_filters", nil)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getAllServletsTool := mcp.NewTool("get_all_servlets",
		mcp.WithDescription("列出所有 Java Web Servlets 类"),
	)
	s.AddTool(getAllServletsTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		out, err := util.HTTPGet("/api/get_all_servlets", nil)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})

	getAllListenersTool := mcp.NewTool("get_all_listeners",
		mcp.WithDescription("列出所有 Java Web Listeners 类"),
	)
	s.AddTool(getAllListenersTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		out, err := util.HTTPGet("/api/get_all_listeners", nil)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})
}

package tools

import (
	"context"
	"net/url"

	"jar-analyzer-mcp/pkg/util"

	"github.com/mark3labs/mcp-go/mcp"
	"github.com/mark3labs/mcp-go/server"
)

func RegisterCodeTools(s *server.MCPServer) {
	getMethodsByClassTool := mcp.NewTool("get_code",
		mcp.WithDescription("反编译某个 CLASS 查看代码"),
		mcp.WithString("class", mcp.Required(), mcp.Description("类名（点或斜杠分隔均可）")),
	)
	s.AddTool(getMethodsByClassTool, func(ctx context.Context, req mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		className, err := req.RequireString("class")
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		params := url.Values{"class": []string{className}}
		out, err := util.HTTPGet("/api/code", params)
		if err != nil {
			return mcp.NewToolResultError(err.Error()), nil
		}
		return mcp.NewToolResultText(out), nil
	})
}

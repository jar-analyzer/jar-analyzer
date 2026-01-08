from __future__ import annotations
# Optional: jar-analyzer built-in MCP (:20032 SSE) client.
# Step3 keeps HTTP as default. This module can be implemented later if you want
# to fetch the same 'get_code_*' via MCP instead of HTTP.
class JarAnalyzerMCP:
    def __init__(self, sse_url: str):
        self.sse_url = sse_url
    def get_code(self, class_name: str, engine: str = "fernflower") -> str:
        raise NotImplementedError("MCP client not implemented in step3; use HTTP")

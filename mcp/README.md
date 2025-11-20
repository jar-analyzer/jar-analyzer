## jar-analyzer-mcp

请使用 `golang 1.24` 以上版本编译

为什么不用 `Java` 写 `MCP`

因为官方 `Java MCP SDK` 需要 `Java 17` 这和 `Jar Analzyer` 最低要求冲突

而我不想手搓或者用小众的基础库，于是选择支持平台多且容易部署的 `golang`

### core mcp

```shell
jar-analyzer> mcp_v1.0.0_windows_amd64.exe -h
  -auth
        enable mcp auth
  -debug
        debug mode
  -ja
        enable jar-analyzer-api token
  -jt string
        jar-analyzer-api token (default "JAR-ANALYZER-API-TOKEN")
  -port int
        port to listen on (default 20032)
  -token string
        mcp token (default "JAR-ANALYZER-MCP-TOKEN")
  -url string
        Jar Analyzer URL (default "http://127.0.0.1:10032")
```

确保 `mcp` 网络可以访问通 `Jar Analyzer URL`

### report mcp

这是为了配合 `n8n workflow` 而存在的 `agent`

参考文档：[n8n-doc](../n8n-doc)

![](../mcp-img/012.png)

```shell
jar-analyzer> report_mcp_v1.0.0_windows_amd64.exe -h
  -debug
        enable debug mode
  -port int
        mcp port to listen on (default 20081)
  -web-host string
        mcp web server host (default "127.0.0.1")
  -web-port int
        web server port to listen on (default 20080)
```

为什么要配置 `mcp web server host`

因为：`web` 端使用 `websocket` 连接 `web server port`

需要确保本地浏览器可以访问通这个 `host:port`
# Security Policy

## Reporting a Vulnerability

Welcome to report any security issues. 

You can directly report them on Github's Security page.

## 安全公告

- [\[GHSA-43rf-3hm4-hv5f\] 反编译恶意的 CLASS 文件可能导致程序不可用](https://github.com/jar-analyzer/jar-analyzer/security/advisories/GHSA-43rf-3hm4-hv5f)
- [\[GHSA-x5h2-78p8-w943\] Jar Analyzer 2.13 版本之前存在 SQL 注入漏洞](https://github.com/jar-analyzer/jar-analyzer/security/advisories/GHSA-x5h2-78p8-w943)
- [\[GHSA-jmcg-r2c5-7m29\] Jar Analyzer 存在 ZIP SLIP 漏洞（最坏情况可导致 RCE 风险）](https://github.com/jar-analyzer/jar-analyzer/security/advisories/GHSA-jmcg-r2c5-7m29)
- [\[GHSA-h6vc-3rcp-p7qp\] 表达式分析功能中的 SpEL 注入漏洞可导致远程代码执行](https://github.com/jar-analyzer/jar-analyzer/security/advisories/GHSA-h6vc-3rcp-p7qp)

不再接受用户的输入导致的安全问题，除非恶意的输入 `class/jar` 文件可能导致的安全问题

不再接收 `GUI` 中可能的漏洞，不认为通过 `Webswing` 等方式暴露到 `web` 端会产生漏洞

注意：当 `Jar` 数量较多或巨大时 **可能导致临时目录和数据库文件巨大** 请确保足够的空间

有 `UI` 兼容性问题请查看 `ISSUE` 部分的置顶

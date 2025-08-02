## 目录结构

- `com.github.rjeschke.txtmark` 一个开源的 `markdown` 解析库
- `org.jetbrains.java.decompiler` 来自 `jetbrains` 的 `fern-flower` 反编译库
- `org.sqlite` 来自开源的 `sqlite` 驱动但只包含常用的驱动
- `me.n1ar4.log` 模仿 `Apache Log4j2 API` 的日志库
- `me.n1ar4.shell.analyzer` 是远程分析和查杀 `Tomcat` 内存马的项目

为什么要源码级集成 sqlite 和 fernflower

因为：

- sqlite 官方驱动体积过大，实际只需要 win/mac/linux 的 64 位即可（桌面）
- fernflower 没有官方 maven 仓库，第三方不支持 java 8 于是 fork 一份并修复 bug
## 命令行版本

`2.13` 版本集成 `https://github.com/luelueking/ClazzSearcher` 作为 `jar-analyzer` 的命令行版本

未来版本如果有精力，将会尝试补全和完善 `ClazzSearcher` 项目

使用方式：

```shell
java -jar jar-analyzer-*.jar class-researcer [其他参数]
```

具体参数和配置文件参考：[README](../class-searcher/README.md)

示例：

```shell
java -jar .\jar-analyzer-2.12.jar class-searcher --onlyJDK --f field.yml
```
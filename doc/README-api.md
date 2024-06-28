## API

现在 `Jar Analyzer` 分析完成后提供 `HTTP API` 方式进行查询

默认绑定 `0.0.0.0:10032` 你可以通过 `gui --port [port]` 指定参数

参数说明：

- ${class-name} 是完整类名例如 `java.lang.String`

| API                   | 参数                  | 功能                   |
|:----------------------|:--------------------|:---------------------|
| /api/get_jars_list    | /                   | 查询所有输入的 JAR 文件       |
| /api/get_jar_by_class | class=${class-name} | 根据输入的完整类名查询归属 JAR 文件 |
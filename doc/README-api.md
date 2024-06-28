## API

现在 `Jar Analyzer` 分析完成后提供 `HTTP API` 方式进行查询

参数说明：
- ${class-name} 是完整类名例如 `java.lang.String`

| API             | 参数                  | 功能                   |
|:----------------|:--------------------|:---------------------|
| /api/jars/list  | /                   | 查询所有输入的 JAR 文件       |
| /api/jars/class | class=${class-name} | 根据输入的完整类名查询归属 JAR 文件 |
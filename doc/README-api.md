## API

现在 `Jar Analyzer` 分析完成后提供 `HTTP API` 方式进行查询

默认绑定 `0.0.0.0:10032` 你可以通过 `gui --port [port]` 指定参数

参数说明：

- ${class-name} 是完整类名例如 `java.lang.String`
- ${method} 是普通的方法名称（除了 `<init>` 和 `<clinit>` 方法）
- ${desc} 是完整的方法描述例如 `()Ljava/lang/Class;`

| API                       | 参数                                                | 功能                   |
|:--------------------------|:--------------------------------------------------|:---------------------|
| /api/get_jars_list        | /                                                 | 查询所有输入的 JAR 文件       |
| /api/get_jar_by_class     | class=${class-name}                               | 根据输入的完整类名查询归属 JAR 文件 |
| /api/get_abs_path         | class=${class-name}                               | 得到 CLASS 文件的本地绝对路径   |
| /api/get_class_by_class   | class=${class-name}                               | 得到 CLASS 的详细信息       |
| /api/get_methods_by_class | class=${class-name}                               | 查询 CLASS 中的所有方法      |
| /api/get_callers          | class=${class-name}&method=${method}&desc=${desc} | 根据方法信息找到所有调用者        |
| /api/get_callers_like     | class=${class-name}&method=${method}&desc=${desc} | 根据方法信息模糊找到所有调用者      |
| /api/get_callee           | class=${class-name}&method=${method}&desc=${desc} | 根据方法信息找到所有被调用者       |
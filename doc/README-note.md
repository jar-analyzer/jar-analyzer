## 注意事项

### 体积问题

注意：请勿分析数量极多或体积巨大的 `JAR` 文件，可能导致巨大的临时文件和数据库，以及高内存消耗

![](../img/0039.png)

### 显示问题

如果 `Mac` 无法显示完全，请在显示器设置中勾选 `更多空间`

![](../img/mac.png)

### 乱码问题

注意：
- 在 `Windows` 下请勿双击启动，请使用 `java -jar` 或双击 `bat` 脚本启动
- 如果使用 `java -jar` 启动乱码，请加入 `-Dfile.encoding=UTF-8` 参数

### 显示问题

本工具已经根据 `1080P` 适配 （考虑到绝大多数机器应该大于等于这个分辨率）

如果你的电脑在 `1080P` 下无法正常显示，请调整缩放到 `100%`

以 `Windows 11` 为例：右键显示设置

![](../img/0010.png)

### 原理相关

本工具的基本原理：
- 解压所有 `Jar` 文件到 `jar-analyzer-temp` 目录
- 在当前目录构建数据库 `jar-analyzer.db` 文件
- 在当前目录新建文件 `.jar-analyzer` 记录状态

![](../img/0001.png)

注意：当 `Jar` 数量较多或巨大时**可能导致临时目录和数据库文件巨大**

### 命令行使用

如果你不想使用 `GUI` 版本，本项目也支持命令行方式:

```text
Usage: java -jar jar-analyzer.jar [command] [command options]
  Commands:
    build      build database
      Usage: build [options]
        Options:
          --del-cache
            delete old cache
            Default: false
          --del-exist
            delete old database
            Default: false
          -j, --jar
            jar file/dir

    gui      start jar-analyzer gui
      Usage: gui
```

示例：对当前目录的 `test.jar` 进行分析和构建数据库，并删除缓存和当前目录旧数据库

```shell
java -jar jar-analyzer.jar build --jar 1.jar --del-cache --del-exist
```

### 视频演示

[B站视频教程](https://www.bilibili.com/video/BV1ac411S7q4/)

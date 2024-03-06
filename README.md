# Jar-Analyzer V2

[CHANGE LOG](src/main/resources/CHANGELOG.MD)

![](https://img.shields.io/badge/build-passing-brightgreen)
![](https://img.shields.io/badge/build-Java%208-orange)
![](https://img.shields.io/github/downloads/jar-analyzer/jar-analyzer/total)
![](https://img.shields.io/github/v/release/jar-analyzer/jar-analyzer)

`Jar Analyzer` 是一个分析 `Jar` 文件的 `GUI` 工具：

- 支持大 `Jar` 以及批量 `Jars` 分析
- 方便地搜索方法之间的调用关系
- 分析 `LDC` 指令定位 `Jar` 中的字符串
- 一键分析 `Spring Controller/Mapping`
- 对于方法字节码和指令的高级分析
- 一键反编译，优化对内部类的处理
- 一键生成方法的 `CFG` 分析结果
- 一键生成方法的 `Stack Frame` 分析结果
- 远程分析 `Tomcat` 中的 `Servlet` 等组件
- 自定义 `SQL` 语句进行高级分析
- 集成 `ClazzSearcher` 项目作为命令行分析版本
- 允许从字节码层面直接修改方法名（测试功能）

更多的功能正在开发中

有问题和建议欢迎提 `issue`

[前往下载 (已提供国内下载地址)](https://github.com/jar-analyzer/jar-analyzer/releases/latest)

`Jar Analyzer` 的用途

- 场景1：从大量 `JAR` 中分析某个方法在哪个 `JAR` 里定义（精确到具体类具体方法）
- 场景2：从大量 `JAR` 中分析哪里调用了 `Runtime.exec` 方法（精确到具体类具体方法）
- 场景3：从大量 `JAR` 中分析字符串 `${jndi` 出现在哪些方法（精确到具体类具体方法）
- 场景4：从大量 `JAR` 中分析有哪些 `Spring Controller/Mapping` 信息（精确到具体类具体方法）
- 场景5：你需要深入地分析某个方法中 `JVM` 指令调用的传参（带有图形界面）
- 场景6：你需要深入地分析某个方法中 `JVM` 指令和栈帧的状态（带有图形界面）
- 场景7：你需要深入地分析某个方法的 `Control Flow Graph` （带有图形界面）
- 场景8：你有一个 `Tomcat` 需要远程分析其中的 `Servlet/Filter/Listener` 信息
- 场景9：查实现接口 `A` 继承接口 `B` 类注解 `C` 且方法名 `test` 方法内调用 `D` 类 `a` 方法的方法

## 一些截图

指令分析

![](img/0006.png)

`CFG` 分析

![](img/0007.png)

带图形的 `Stack Frame` 分析

![](img/0008.png)

分析 `Spring Framework`

![](img/0009.png)

从 `2.8` 版本开始支持 `tomcat` 分析（一检查杀内存马）

![](img/0017.png)

自定义 `SQL` 语句任意分析

![](img/0014.png)

方法调用搜索 (支持 `equals/like` 选项，支持黑名单过滤)

![](img/0012.png)

方法调用关系

![](img/0004.png)

`Jar Analyzer 2.12` 版本以后使用自研 `RASP` 保护程序

(Runtime Application Self-Protection)

![](img/0032.png)

## 命令行版本

[文档](doc/README-cli.md)

## 表达式搜索

[文档](doc/README-el.md)

## 注意事项

[文档](doc/README-note.md)

## 子项目

[文档](doc/README-sub.md)

## 其他

[文档](doc/README-others.md)

## 参考致谢

[文档](doc/README-thanks.md)

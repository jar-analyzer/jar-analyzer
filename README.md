# Jar-Analyzer V2

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
- 自定义 `SQL` 语句进行高级分析

更多的功能正在开发中

有问题和建议欢迎提 `issue`

[前往下载](https://github.com/jar-analyzer/jar-analyzer/releases/latest)

注意：
- 在 `Windows` 下请勿双击启动，请使用 `java -jar` 或双击 `bat` 脚本启动
- 如果使用 `java -jar` 启动乱码，请加入 `-Dfile.encoding=UTF-8` 参数

## 一些截图

指令分析

![](img/0006.png)

`CFG` 分析

![](img/0007.png)

`JVM Stack Frame` 分析

![](img/0013.png)

带图形的 `Stack Frame` 分析

![](img/0008.png)

分析 `Spring Framework`

![](img/0009.png)

自定义 `SQL` 语句任意分析

![](img/0014.png)

首页

![](img/0002.png)

方法调用搜索 (支持 `equals/like` 选项，支持黑名单过滤)

![](img/0012.png)

方法调用关系

![](img/0004.png)

一键分析，自定义 `SQL` 分析，其他插件等

![](img/0015.png)

## 注意事项

本工具已经根据 `1080P` 适配 （考虑到绝大多数机器应该大于等于这个分辨率）

如果你的电脑在 `1080P` 下无法正常显示，请调整缩放到 `100%`

以 `Windows 11` 为例：右键显示设置

![](img/0010.png)

本工具的基本原理：
- 解压所有 `Jar` 文件到 `jar-analyzer-temp` 目录
- 在当前目录构建数据库 `jar-analyzer.db` 文件
- 在当前目录新建文件 `.jar-analyzer` 记录状态

![](img/0001.png)

注意：当 `Jar` 数量较多或巨大时**可能导致临时目录和数据库文件巨大**

## Release 说明

在 `release` 中提供四种下载：
- `system` 使用系统 `JDK/JRE` 的启动脚本 (需要自行安装 `JRE`)
- `embed` 内置 `JRE` 的启动脚本 (无需另外安装一键启动)
- `linux` 内置 `shell` 启动脚本 (需要自行安装 `JRE`)
- 简单的 `Jar` 文件，不提供启动脚本

由于本工具仅在 `Windows` 中测试，其他操作系统可能会有未知的问题，欢迎提 `issue`

一般情况下，推荐使用内置 `JRE` 的 `embed` 版本启动

## 如何构建

构建一般基于 `Java 8`
- 下载 `JDK 8`
- 使用 `Maven` (https://maven.apache.org/download.cgi)
- 使用 `Python 3` 辅助 (https://www.python.org/downloads/)

步骤：

(1) mvn -B package -DskipTests --file pom.xml

(2) python build.py

(3) 复制 JRE 到 embed 版

## 其他

`Jar Analyzer` 子项目：
- [Y4 JSON](https://github.com/jar-analyzer/y4-json) 为 `Jar Analyzer` 提供 `JSON` 序列化和反序列化

如果你希望体验老版本 (不再维护) 的 `Jar Analyzer` 可以访问：
- https://github.com/4ra1n/jar-analyzer-cli
- https://github.com/4ra1n/jar-analyzer-gui

为什么我不选择 `IDEA` 而要选择 `Jar Analyzer V2` 工具：
- 因为 `IDEA` 不支持分析无源码的 `Jar` 包
- 本工具有一些进阶功能是 `IDEA` 不支持的 (指令/CFG/Stack分析)

(1) 什么是方法之间的关系

```java
class Test{
    void a(){
        new Test().b();
    }
    
    void b(){
        Test.c();
    }
    
    static void c(){
        // code
    }
}
```

如果当前方法是 `b`

对于 `a` 来说，它的 `callee` 是 `b`

对于 `b` 来说，它的 `caller` 是 `a`

(2) 如何解决接口实现的问题

```java
class Demo{
    void demo(){
        new Test().test();
    }
}

interface Test {
    void test();
}

class Test1Impl implements Test {
    @Override
    public void test() {
        // code
    }
}

class Test2Impl implements Test {
    @Override
    public void test() {
        // code
    }
}
```

现在我们有 `Demo.demo -> Test.test` 数据, 但实际上它是 `Demo.demo -> TestImpl.test`.

因此我们添加了新的规则： `Test.test -> Test1Impl.test` 和 `Test.test -> Test2Impl.test`.

首先确保数据不会丢失，然后我们可以自行手动分析反编译的代码
- `Demo.demo -> Test.test`
- `Test.test -> Test1Impl.test`/`Test.test -> Test2Impl.test`

(3) 如何解决继承关系

```java
class Zoo{
    void run(){
        Animal dog = new Dog();
        dog.eat();
    }
}

class Animal {
    void eat() {
        // code
    }
}

class Dog extends Animal {
    @Override
    void eat() {
        // code
    }
}

class Cat extends Animal {
    @Override
    void eat() {
        // code
    }
}
```
`Zoo.run -> dog.cat` 的字节码是 `INVOKEVIRTUAL Animal.eat ()V`, 但我们只有这条规则 `Zoo.run -> Animal.eat`, 丢失了 `Zoo.run -> Dog.eat` 规则

这种情况下我们添加了新规则： `Animal.eat -> Dog.eat` 和 `Animal.eat -> Cat.eat`

首先确保数据不会丢失，然后我们可以自行手动分析反编译的代码
- `Zoo.run -> Animal.eat`
- `Animal.eat -> Dog.eat`/`Animal.eat -> Cat.eat`

## 致谢

感谢以下项目提供的思路和代码
- https://github.com/JetBrains/intellij-community/tree/master/plugins/java-decompiler/engine
- https://github.com/bobbylight/RSyntaxTextArea
- https://github.com/JackOfMostTrades/gadgetinspector
- https://github.com/lsieun/learn-java-asm

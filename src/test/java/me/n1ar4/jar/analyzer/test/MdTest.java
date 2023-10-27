package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.txtmark.Processor;

public class MdTest {
    public static void main(String[] args) {
        String result = Processor.process("## 2.1-beta\n" +
                "\n" +
                "- [重要] 支持分析`spring controller`和`mapping`\n" +
                "- [BUG] 解决`mac/ubuntu`中`cfg/frame`分析乱码\n" +
                "- [功能] 支持选择`classes`目录进行分析\n" +
                "- [功能] 内置`consolas`字体启动时自动注册\n" +
                "- [功能] 启动时通过`github`的`api`检查更新\n" +
                "- [其他] 简单的性能和用户体验的优化\n" +
                "- [其他] 不再使用`exe`版本而是使用`bat/sh`脚本启动\n" +
                "- [其他] 发布`release`将包含`win`和`linux(ubuntu)`两种\n" +
                "\n" +
                "## 2.2-beta\n" +
                "\n" +
                "- [功能] 支持前后跳转：返回上一步和回到当前\n" +
                "- [BUG] 分析`spring`由于`path`为空导致的`BUG` #5\n" +
                "- [优化] 删除某些巨大的依赖库并计划自行实现\n" +
                "- [其他] 删除不必要代码以及简单优化已有代码");
        System.out.println(result);
    }
}

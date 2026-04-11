/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.starter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public interface Const {
    String version = "5.20";

    int ASMVersion = Opcodes.ASM9;

    int GlobalASMOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
    int AnalyzeASMOptions = ClassReader.EXPAND_FRAMES;

    /**
     * Fallback ASM option for handling corrupted StackMapTable class files.
     * When EXPAND_FRAMES throws IndexOutOfBoundsException, use this option to skip frame parsing.
     */
    int FallbackASMOptions = ClassReader.SKIP_FRAMES;

    /**
     * Warning message for class files parsed with SKIP_FRAMES due to corrupted StackMapTable.
     */
    String CORRUPTED_STACKMAP_WARNING =
            "// WARNING: This class file was parsed with SKIP_FRAMES mode due to corrupted StackMapTable\n" +
                    "// 警告：此 class 文件因 StackMapTable 损坏而使用 SKIP_FRAMES 模式解析\n" +
                    "// The StackMapTable contains invalid bytecode offset that exceeds code length\n" +
                    "// StackMapTable 包含超出代码长度的无效字节码偏移量\n";

    String app = "Jar Analyzer - 4ra1n - " + version;
    String checkUpdateUrl = "https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/jar-analyzer/version.txt";
    String authorUrl = "https://github.com/4ra1n";
    String projectUrl = "https://github.com/jar-analyzer/jar-analyzer";
    String newIssueUrl = "https://github.com/jar-analyzer/jar-analyzer/issues/new/choose";
    String docsUrl = "https://docs.qq.com/doc/DV3pKbG9GS0pJS0tk";
    String dbFile = "jar-analyzer.db";
    String tempDir = "jar-analyzer-temp";
    String indexDir = "jar-analyzer-document";
    String downDir = "jar-analyzer-download";
    String OpcodeForm = "Jar Analyzer - Method Opcode";
    String SPELSearch = "Jar Analyzer - SPEL Search";
    String ChangeLogForm = "Jar Analyzer - CHANGELOG";
    String CFGForm = "Jar Analyzer - CFG";
    String FrameForm = "Jar Analyzer - Frame";
    String SQLiteForm = "Jar Analyzer - SQLite";
    String BcelForm = "Jar Analyzer - BCEL Util";
    String StringForm = "Jar Analyzer - String";
    String RemoteForm = "Jar Analyzer - Remote Load";
    String PartForm = "Jar Analyzer - Partition Config";
    String SerUtilForm = "Jar Analyzer - SerUtil";
    String ExportForm = "Jar Analyzer - Export Java Code";
    String ModeForm = "Jar Analyzer - Mode";
    String blackAreaText = "# filter rule support white/black mode\n" +
            "# package filter list\n" +
            "java.util.;\n" +
            "# class filter list\n" +
            "java.lang.Object;\n";
    String classBlackAreaText = "# package black list\n" +
            "com.test.a.;\n" +
            "# class black list\n" +
            "com.test.a.Test;\n";
    String classWhiteAreaText = "# package white list\n" +
            "# com.test.a.;\n" +
            "# class white list\n" +
            "# com.test.a.Test;\n";
    String welcome = "// Jar Analyzer - Code Viewer\n" +
            "// Jar Analyzer - 代码查看器 - 支持多 TAB\n\n" +
            "// 点击 Chose File 选择文件或目录进行分析\n" +
            "// 你也可以拖拽文件或目录到面板进行分析\n\n" +
            "// 可以在 call 面板查看当前方法的 caller/callee\n" +
            "// 选中方法 Ctrl+左键 可以查看 caller/callee\n\n" +
            "// 1. 基础 call/define 搜索前往 search 面板\n" +
            "// 2. 高级搜索前往 EL Search 表达式搜索\n" +
            "// 3. 前往 web 面板进行 Java Web Source 分析\n" +
            "// 4. 前往 advance 面板可一键搜索 sink 方法\n" +
            "// 5. 前往 chains 面板可进行方法调用链分析和污点分析\n" +
            "// 6. 可以两次 shift 打开全局索引搜索（测试功能）\n\n" +
            "// 作者：4ra1n @ jar-analyzer @ " + version;
}

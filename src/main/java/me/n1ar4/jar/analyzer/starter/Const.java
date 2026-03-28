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
    String version = "5.17";

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
}

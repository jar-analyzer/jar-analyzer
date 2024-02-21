package me.n1ar4.jar.analyzer.starter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public interface Const {
    int ASMVersion = Opcodes.ASM9;
    int GlobalASMOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
    int AnalyzeASMOptions = ClassReader.EXPAND_FRAMES;
    String app = "Jar Analyzer V2 - 4ra1n";
    String version = "2.11";
    String checkUpdateUrl = "http://47.97.182.120/version.txt";
    String authorUrl = "https://github.com/4ra1n";
    String projectUrl = "https://github.com/jar-analyzer/jar-analyzer";
    String newIssueUrl = "https://github.com/jar-analyzer/jar-analyzer/issues/new";
    String dbFile = "jar-analyzer.db";
    String tempDir = "jar-analyzer-temp";
    String OpcodeForm = "Jar Analyzer V2 - Method Opcode";
    String ChangeLogForm = "Jar Analyzer V2 - CHANGELOG";
    String CFGForm = "Jar Analyzer V2 - CFG";
    String FrameForm = "Jar Analyzer V2 - Frame";
    String SQLiteForm = "Jar Analyzer V2 - SQLite";
    String ChatGPTForm = "Jar Analyzer V2 - ChatGPT";
    String StringForm = "Jar Analyzer V2 - String";
    String blackAreaText = "java.lang.Object;\njava.lang.Integer;\n";
    String classBlackAreaText = "com.test.a;\ncom.test.a.;\ncom.test.a.TestClass;\n";
}

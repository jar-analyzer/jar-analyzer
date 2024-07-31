package me.n1ar4.jar.analyzer.starter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public interface Const {
    int ASMVersion = Opcodes.ASM9;
    int GlobalASMOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
    int AnalyzeASMOptions = ClassReader.EXPAND_FRAMES;
    String app = "Jar Analyzer V2 - 4ra1n";
    String version = "2.24";
    String checkUpdateUrl = "https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/jar-analyzer/version.txt";
    String authorUrl = "https://github.com/4ra1n";
    String projectUrl = "https://github.com/jar-analyzer/jar-analyzer";
    String newIssueUrl = "https://github.com/jar-analyzer/jar-analyzer/issues/new/choose";
    String dbFile = "jar-analyzer.db";
    String tempDir = "jar-analyzer-temp";
    String downDir = "jar-analyzer-download";
    String OpcodeForm = "Jar Analyzer V2 - Method Opcode";
    String SPELSearch = "Jar Analyzer V2 - SPEL Search";
    String ChangeLogForm = "Jar Analyzer V2 - CHANGELOG";
    String CFGForm = "Jar Analyzer V2 - CFG";
    String FrameForm = "Jar Analyzer V2 - Frame";
    String SQLiteForm = "Jar Analyzer V2 - SQLite";
    String StringForm = "Jar Analyzer V2 - String";
    String RemoteForm = "Jar Analyzer V2 - Remote Load";
    String PartForm = "Jar Analyzer V2 - Partition Config";
    String SerUtilForm = "Jar Analyzer V2 - SerUtil";
    String ExportForm = "Jar Analyzer V2 - Export Java Code";
    String blackAreaText = "java.lang.Object;\njava.lang.Integer;\n";
    String classBlackAreaText = "# package black list\n" +
            "com.test.a.;\n" +
            "# class black list\n" +
            "com.test.a.Test;\n";
    String classWhiteAreaText = "# package white list\n" +
            "# com.test.a.;\n" +
            "# class white list\n" +
            "# com.test.a.Test;\n";
}

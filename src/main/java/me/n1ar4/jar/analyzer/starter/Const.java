/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.starter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public interface Const {
    String version = "3.1";

    int ASMVersion = Opcodes.ASM9;

    int GlobalASMOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
    int AnalyzeASMOptions = ClassReader.EXPAND_FRAMES;

    String app = "Jar Analyzer - 4ra1n - " + version;
    String checkUpdateUrl = "https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/jar-analyzer/version.txt";
    String authorUrl = "https://github.com/4ra1n";
    String projectUrl = "https://github.com/jar-analyzer/jar-analyzer";
    String newIssueUrl = "https://github.com/jar-analyzer/jar-analyzer/issues/new/choose";
    String dbFile = "jar-analyzer.db";
    String tempDir = "jar-analyzer-temp";
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

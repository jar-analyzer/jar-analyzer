/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.asm;

import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

public class ASMPrint {
    private static final Logger logger = LogManager.getLogger();

    public static String getPrint(InputStream is, boolean flag) {
        return getPrint(is, flag, false);
    }

    /**
     * Get ASM print output for a class file.
     *
     * @param is             The input stream of the class file
     * @param flag           true for ASMifier output, false for Textifier output
     * @param usedSkipFrames true if this class was parsed with SKIP_FRAMES due to corrupted StackMapTable
     * @return The formatted output string
     */
    public static String getPrint(InputStream is, boolean flag, boolean usedSkipFrames) {
        try {
            int parsingOptions = Const.GlobalASMOptions;
            Printer printer = flag ? new ASMifier() : new Textifier();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            PrintWriter printWriter = new PrintWriter(bao, true);

            // Add warning comment if this class was parsed with SKIP_FRAMES due to corrupted StackMapTable
            if (usedSkipFrames) {
                printWriter.print(Const.CORRUPTED_STACKMAP_WARNING);
                printWriter.println();
            }

            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, printer, printWriter);
            new ClassReader(is).accept(traceClassVisitor, parsingOptions);
            return bao.toString();
        } catch (Exception ex) {
            logger.error("asm print error: {}", ex.toString());
        }
        return null;
    }
}

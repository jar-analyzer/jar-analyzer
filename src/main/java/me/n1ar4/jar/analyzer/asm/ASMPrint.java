package me.n1ar4.jar.analyzer.asm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        try {
            int parsingOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
            Printer printer = flag ? new ASMifier() : new Textifier();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            PrintWriter printWriter = new PrintWriter(bao, true);
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, printer, printWriter);
            new ClassReader(is).accept(traceClassVisitor, parsingOptions);
            return bao.toString();
        } catch (Exception ex) {
            logger.error("asm print error: {}", ex.toString());
        }
        return null;
    }
}
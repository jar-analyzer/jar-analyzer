// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main.decompiler;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.io.PrintStream;

public class PrintStreamLogger extends IFernflowerLogger {

    private static final Logger logger = LogManager.getLogger();

    private final PrintStream stream;
    private int indent;

    public PrintStreamLogger(PrintStream printStream) {
        stream = printStream;
        indent = 0;
    }

    @Override
    public void writeMessage(String message, Severity severity) {
        if (accepts(severity)) {
            logger.debug("decompile: " + message);
        }
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable t) {
        if (accepts(severity)) {
            writeMessage(message, severity);
            t.printStackTrace(stream);
        }
    }

    @Override
    public void startReadingClass(String className) {
        if (accepts(Severity.INFO)) {
            writeMessage(className, Severity.INFO);
            ++indent;
        }
    }

    @Override
    public void endReadingClass() {
        if (accepts(Severity.INFO)) {
            --indent;
        }
    }

    @Override
    public void startClass(String className) {
        if (accepts(Severity.INFO)) {
            ++indent;
        }
    }

    @Override
    public void endClass() {
        if (accepts(Severity.INFO)) {
            --indent;
        }
    }

    @Override
    public void startMethod(String methodName) {
        if (accepts(Severity.INFO)) {
            ++indent;
        }
    }

    @Override
    public void endMethod() {
        if (accepts(Severity.INFO)) {
            --indent;
        }
    }

    @Override
    public void startWriteClass(String className) {
        if (accepts(Severity.INFO)) {
            ++indent;
        }
    }

    @Override
    public void endWriteClass() {
        if (accepts(Severity.INFO)) {
            --indent;
        }
    }
}

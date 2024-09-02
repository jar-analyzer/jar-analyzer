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

package me.n1ar4.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LoggingStream extends PrintStream {
    private final Logger logger;
    private final OutputStream originalOut;

    public LoggingStream(OutputStream out, Logger logger) {
        super(out);
        this.logger = logger;
        this.originalOut = out;
    }

    @Override
    public void println(String x) {
        if (!isLoggerCall()) {
            logger.info(x);
        } else {
            directPrintln(x);
        }
    }

    private boolean isLoggerCall() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().equals("me.n1ar4.log.Logger")) {
                return true;
            }
        }
        return false;
    }

    private void directPrintln(String x) {
        synchronized (this) {
            byte[] bytes = (x + System.lineSeparator()).getBytes();
            try {
                originalOut.write(bytes);
                originalOut.flush();
            } catch (IOException e) {
                setError();
            }
        }
    }
}
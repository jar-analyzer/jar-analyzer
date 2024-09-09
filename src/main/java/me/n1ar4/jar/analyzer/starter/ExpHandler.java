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

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class ExpHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            Class<?> edtClass = Class.forName("java.awt.EventDispatchThread");
            if (edtClass.isInstance(t)) {
                if (e instanceof ArrayIndexOutOfBoundsException) {
                    // 这里是一处已知的异常
                    // java/util/Vector elementAt ArrayIndexOutOfBoundsException
                    return;
                }
            }
            // 处理下异常：不抛出异常 记录到当前目录
            FileOutputStream fos = new FileOutputStream(Paths.get("JAR-ANALYZER-ERROR.txt").toFile());
            PrintWriter ps = new PrintWriter(fos);
            e.printStackTrace(ps);
            ps.flush();
            ps.close();
            logger.error("UNCAUGHT EXCEPTION LOGGED IN JAR-ANALYZER-ERROR.txt");
        } catch (Exception ex) {
            logger.warn("handle thread error: {}", ex.toString());
        }
    }
}

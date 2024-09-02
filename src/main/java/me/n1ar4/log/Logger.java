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

@SuppressWarnings("all")
public class Logger {
    private String formatMessage(String message, Object[] args) {
        int start = 0;
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        while (start < message.length()) {
            int open = message.indexOf("{}", start);
            if (open == -1) {
                sb.append(message.substring(start));
                break;
            }
            sb.append(message.substring(start, open));
            if (argIndex < args.length) {
                sb.append(args[argIndex++]);
            } else {
                sb.append("{}");
            }
            start = open + 2;
        }
        return sb.toString();
    }

    public void info(String message) {
        Log.info(message);
    }

    public void info(String message, Object... args) {
        Log.info(formatMessage(message, args));
    }

    public void error(String message) {
        Log.error(message);
    }

    public void error(String message, Object... args) {
        Log.error(formatMessage(message, args));
    }

    public void debug(String message) {
        Log.debug(message);
    }

    public void debug(String message, Object... args) {
        Log.debug(formatMessage(message, args));
    }

    public void warn(String message) {
        Log.warn(message);
    }

    public void warn(String message, Object... args) {
        Log.warn(formatMessage(message, args));
    }
}

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

package me.n1ar4.jar.analyzer.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import me.n1ar4.jar.analyzer.utils.ASMUtil;

import java.nio.file.Path;

public class MethodResult {
    private String className;
    private String jarName;
    private String methodName;
    private String methodDesc;
    private int isStaticInt;
    private int accessInt;
    @JSONField(serialize = false)
    private Path classPath;
    // for spring
    @JSONField(serialize = false)
    private String path;

    private int lineNumber;

    public String getPath() {
        this.path = this.path.trim();
        if (this.path.isEmpty()) {
            return "path: none";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "path: " + path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Path getClassPath() {
        return classPath;
    }

    public void setClassPath(Path classPath) {
        this.classPath = classPath;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public int getIsStaticInt() {
        return isStaticInt;
    }

    public void setIsStaticInt(int isStaticInt) {
        this.isStaticInt = isStaticInt;
    }

    public int getAccessInt() {
        return accessInt;
    }

    public void setAccessInt(int accessInt) {
        this.accessInt = accessInt;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "<html>" + ASMUtil.convertMethodDesc(
                getMethodName(), getMethodDesc()) + "</html>";
    }
}

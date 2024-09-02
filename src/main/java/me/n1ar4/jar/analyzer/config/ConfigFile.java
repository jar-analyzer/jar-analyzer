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

package me.n1ar4.jar.analyzer.config;

public class ConfigFile {
    private String jarPath;
    private String dbPath;
    private String tempPath;
    private String dbSize;
    private String totalJar;
    private String totalClass;
    private String totalMethod;
    private String lang;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public String getDbSize() {
        return dbSize;
    }

    public void setDbSize(String dbSize) {
        this.dbSize = dbSize;
    }

    public String getTotalJar() {
        return totalJar;
    }

    public void setTotalJar(String totalJar) {
        this.totalJar = totalJar;
    }

    public String getTotalClass() {
        return totalClass;
    }

    public void setTotalClass(String totalClass) {
        this.totalClass = totalClass;
    }

    public String getTotalMethod() {
        return totalMethod;
    }

    public void setTotalMethod(String totalMethod) {
        this.totalMethod = totalMethod;
    }

    @Override
    public String toString() {
        return "ConfigFile{" +
                "jarPath='" + jarPath + '\'' +
                ", dbPath='" + dbPath + '\'' +
                ", tempPath='" + tempPath + '\'' +
                ", dbSize='" + dbSize + '\'' +
                ", totalJar='" + totalJar + '\'' +
                ", totalClass='" + totalClass + '\'' +
                ", totalMethod='" + totalMethod + '\'' +
                '}';
    }
}

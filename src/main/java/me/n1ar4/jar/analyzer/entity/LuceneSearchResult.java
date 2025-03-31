/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.entity;

import me.n1ar4.jar.analyzer.starter.Const;

public class LuceneSearchResult {
    // 反编译后的代码的文件内容
    public static final int TYPE_CONTENT = 0xf0;
    // 类名：也可以理解成文件名 是一个东西
    public static final int TYPE_CLASS_NAME = 0xf1;

    private int type;
    private String absPathStr;
    private String fileName;
    private String contentStr;
    private String title;
    private String searchKey;

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAbsPathStr() {
        return absPathStr;
    }

    public void setAbsPathStr(String absPathStr) {
        this.absPathStr = absPathStr;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentStr() {
        return contentStr;
    }

    public void setContentStr(String contentStr) {
        this.contentStr = contentStr;
    }

    public String getClassName() {
        String finalClassPath = this.getAbsPathStr();
        String suffix = finalClassPath.split(Const.tempDir)[1];
        int i = suffix.indexOf("classes");
        if (suffix.contains("BOOT-INF") || suffix.contains("WEB-INF")) {
            suffix = suffix.substring(i + 8, suffix.length() - 6);
        } else {
            suffix = suffix.substring(1, suffix.length() - 6);
        }
        String className = suffix.replace("\\", "/");
        className = className.replace("/", ".");
        className = className.replace(".class", "");
        return className;
    }

    @Override
    public String toString() {
        return "LuceneSearchResult{" +
                "type=" + type +
                ", absPathStr='" + absPathStr + '\'' +
                ", fileName='" + fileName + '\'' +
                ", contentStr='" + contentStr + '\'' +
                '}';
    }
}

/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.entity;

import com.alibaba.fastjson2.JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LeakResult {
    private String className;
    private String value;
    private String typeName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LeakResult that = (LeakResult) o;
        return Objects.equals(className, that.className) && Objects.equals(value, that.value) && Objects.equals(typeName, that.typeName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(className);
        result = 31 * result + Objects.hashCode(value);
        result = 31 * result + Objects.hashCode(typeName);
        return result;
    }

    @Override
    public String toString() {
        String displayValue = value.length() > 16 ? value.substring(0, 16) + "..." : value;
        return "<html>" +
                "TYPE: " +
                "<font style=\"color: blue; font-weight: bold;\">" +
                typeName +
                "</font>" +
                " " +
                "VALUE: " +
                "<font style=\"color: green; font-weight: bold;\">" +
                displayValue +
                "</font>" +
                " " +
                "<font style=\"color: orange; font-weight: bold;\">" +
                className.replace("/", ".") +
                "</font>" +
                "</html>";
    }

    public String getCopyString() {
        Map<String, String> data = new HashMap<>();
        data.put("className", className);
        data.put("typeName", typeName);
        data.put("value", value);
        return JSON.toJSONString(data);
    }
}

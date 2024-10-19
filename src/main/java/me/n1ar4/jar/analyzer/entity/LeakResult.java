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

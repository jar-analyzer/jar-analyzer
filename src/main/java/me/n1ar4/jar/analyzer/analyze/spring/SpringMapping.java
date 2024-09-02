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

package me.n1ar4.jar.analyzer.analyze.spring;

import me.n1ar4.jar.analyzer.core.MethodReference;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class SpringMapping {
    private boolean isRest;
    private SpringController controller;
    private MethodReference.Handle methodName;
    private MethodReference methodReference;
    private String path;
    private List<SpringParam> paramMap = new ArrayList<>();

    public List<SpringParam> getParamMap() {
        return paramMap;
    }

    public void setParamMap(List<SpringParam> paramMap) {
        this.paramMap = paramMap;
    }

    public boolean isRest() {
        return isRest;
    }

    public void setRest(boolean rest) {
        isRest = rest;
    }

    public SpringController getController() {
        return controller;
    }

    public void setController(SpringController controller) {
        this.controller = controller;
    }

    public MethodReference.Handle getMethodName() {
        return methodName;
    }

    public void setMethodName(MethodReference.Handle methodName) {
        this.methodName = methodName;
    }

    public MethodReference getMethodReference() {
        return methodReference;
    }

    public void setMethodReference(MethodReference methodReference) {
        this.methodReference = methodReference;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

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


import me.n1ar4.jar.analyzer.core.ClassReference;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class SpringController {
    private boolean isRest;
    private String basePath;
    private ClassReference.Handle className;
    private ClassReference classReference;
    private final List<SpringMapping> mappings = new ArrayList<>();

    public boolean isRest() {
        return isRest;
    }

    public void setRest(boolean rest) {
        isRest = rest;
    }

    public ClassReference.Handle getClassName() {
        return className;
    }

    public void setClassName(ClassReference.Handle className) {
        this.className = className;
    }

    public ClassReference getClassReference() {
        return classReference;
    }

    public void setClassReference(ClassReference classReference) {
        this.classReference = classReference;
    }

    public List<SpringMapping> getMappings() {
        return mappings;
    }

    public void addMapping(SpringMapping mapping) {
        this.mappings.add(mapping);
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}

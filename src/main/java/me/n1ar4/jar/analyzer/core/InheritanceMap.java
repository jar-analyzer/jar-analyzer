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

package me.n1ar4.jar.analyzer.core;

import java.util.*;


@SuppressWarnings("unused")
public class InheritanceMap {
    /**
     * 子类 -> 所有父类
     */
    private final Map<ClassReference.Handle, Set<ClassReference.Handle>> inheritanceMap;
    /**
     * 父类 -> 所有子类
     */
    private final Map<ClassReference.Handle, Set<ClassReference.Handle>> subClassMap;

    public Map<ClassReference.Handle, Set<ClassReference.Handle>> getInheritanceMap() {
        return inheritanceMap;
    }

    public Map<ClassReference.Handle, Set<ClassReference.Handle>> getSubClassMap() {
        return subClassMap;
    }

    public InheritanceMap(Map<ClassReference.Handle, Set<ClassReference.Handle>> inheritanceMap) {
        this.inheritanceMap = inheritanceMap;
        subClassMap = new HashMap<>();
        for (Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>> entry : inheritanceMap.entrySet()) {
            ClassReference.Handle child = entry.getKey();
            for (ClassReference.Handle parent : entry.getValue()) {
                if (!subClassMap.containsKey(parent)) {
                    Set<ClassReference.Handle> tempSet = new HashSet<>();
                    tempSet.add(child);
                    subClassMap.put(parent, tempSet);
                } else {
                    subClassMap.get(parent).add(child);
                }
            }
        }
    }

    public Set<Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>>> entrySet() {
        return inheritanceMap.entrySet();
    }

    public Set<ClassReference.Handle> getSuperClasses(ClassReference.Handle clazz) {
        Set<ClassReference.Handle> parents = inheritanceMap.get(clazz);
        if (parents == null) {
            return null;
        }
        return Collections.unmodifiableSet(parents);
    }

    public boolean isSubclassOf(ClassReference.Handle clazz, ClassReference.Handle superClass) {
        Set<ClassReference.Handle> parents = inheritanceMap.get(clazz);
        if (parents == null) {
            return false;
        }
        return parents.contains(superClass);
    }

    public Set<ClassReference.Handle> getSubClasses(ClassReference.Handle clazz) {
        Set<ClassReference.Handle> subClasses = subClassMap.get(clazz);
        if (subClasses == null) {
            return null;
        }
        return Collections.unmodifiableSet(subClasses);
    }
}

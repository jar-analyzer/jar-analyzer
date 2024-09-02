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

package me.n1ar4.jar.analyzer.el;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class MethodEL {
    private String classNameContains;
    private Map<Integer, String> paramTypes;
    private String nameContains;
    private String startWith;
    private String endWith;
    private String returnType;
    private String isSubClassOf;
    private String isSuperClassOf;
    private Integer paramsNum;
    private Boolean isStatic;
    private String methodAnno;
    private String excludedMethodAnno;
    private String classAnno;
    private String field;

    // -------------------- GETTER/SETTER -------------------- //
    public String getStartWith() {
        return startWith;
    }

    public void setStartWith(String startWith) {
        this.startWith = startWith;
    }

    public String getEndWith() {
        return endWith;
    }

    public void setEndWith(String endWith) {
        this.endWith = endWith;
    }

    public String getIsSubClassOf() {
        return isSubClassOf;
    }

    public void setIsSubClassOf(String isSubClassOf) {
        this.isSubClassOf = isSubClassOf;
    }

    public String getIsSuperClassOf() {
        return isSuperClassOf;
    }

    public void setIsSuperClassOf(String isSuperClassOf) {
        this.isSuperClassOf = isSuperClassOf;
    }

    public void setParamsNum(Integer paramsNum) {
        this.paramsNum = paramsNum;
    }

    public Boolean getStatic() {
        return isStatic;
    }

    public void setStatic(Boolean aStatic) {
        isStatic = aStatic;
    }

    public String getClassNameContains() {
        return classNameContains;
    }

    public void setClassNameContains(String classNameContains) {
        this.classNameContains = classNameContains;
    }

    public Map<Integer, String> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Map<Integer, String> paramTypes) {
        this.paramTypes = paramTypes;
    }

    public String getNameContains() {
        return nameContains;
    }

    public void setNameContains(String nameContains) {
        this.nameContains = nameContains;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public Integer getParamsNum() {
        return paramsNum;
    }

    public void setParamsNum(int paramsNum) {
        this.paramsNum = paramsNum;
    }

    public Boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public String getMethodAnno() {
        return methodAnno;
    }

    public void setMethodAnno(String methodAnno) {
        this.methodAnno = methodAnno;
    }

    public String getExcludedMethodAnno() {
        return excludedMethodAnno;
    }

    public void setExcludedMethodAnno(String excludedMethodAnno) {
        this.excludedMethodAnno = excludedMethodAnno;
    }

    public String getClassAnno() {
        return classAnno;
    }

    public void setClassAnno(String classAnno) {
        this.classAnno = classAnno;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public MethodEL() {
        this.paramTypes = new HashMap<>();
        this.paramsNum = null;
        this.isStatic = null;
    }

    // -------------------- EL -------------------- //

    public MethodEL nameContains(String str) {
        this.nameContains = str;
        return this;
    }

    public MethodEL startWith(String str) {
        this.startWith = str;
        return this;
    }

    public MethodEL endWith(String str) {
        this.endWith = str;
        return this;
    }

    public MethodEL classNameContains(String str) {
        this.classNameContains = str;
        return this;
    }

    public MethodEL returnType(String str) {
        this.returnType = str;
        return this;
    }

    public MethodEL paramTypeMap(int index, String type) {
        this.paramTypes.put(index, type);
        return this;
    }

    public MethodEL paramsNum(int i) {
        this.paramsNum = i;
        return this;
    }

    public MethodEL isStatic(boolean flag) {
        this.isStatic = flag;
        return this;
    }

    public MethodEL isSubClassOf(String s) {
        this.isSubClassOf = s;
        return this;
    }

    public MethodEL isSuperClassOf(String s) {
        this.isSuperClassOf = s;
        return this;
    }

    public MethodEL hasAnno(String s) {
        this.methodAnno = s;
        return this;
    }

    public MethodEL excludeAnno(String s) {
        this.excludedMethodAnno = s;
        return this;
    }

    public MethodEL hasClassAnno(String s) {
        this.classAnno = s;
        return this;
    }

    public MethodEL hasField(String s) {
        this.field = s;
        return this;
    }
}

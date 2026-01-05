/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gadget;

import java.util.List;

public class GadgetInfo {
    public static final String NATIVE_TYPE = "NATIVE";
    public static final String HESSIAN_TYPE = "HESSIAN";
    public static final String JDBC_TYPE = "JDBC";
    public static final String FASTJSON_TYPE = "FASTJSON";

    private int ID;
    // NATIVE HESSIAN JDBC FASTJSON
    private String type;
    private List<String> jarsName;
    private String result;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getJarsName() {
        return jarsName;
    }

    public void setJarsName(List<String> jarsName) {
        this.jarsName = jarsName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}

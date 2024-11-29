/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.shell.analyzer.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InfoObj implements Comparable<InfoObj> {
    private String url;
    private String hash;
    private String urlDesc;
    private ArrayList<String> globalDesc;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUrlDesc() {
        return urlDesc;
    }

    public void setUrlDesc(String urlDesc) {
        this.urlDesc = urlDesc;
    }

    public ArrayList<String> getGlobalDesc() {
        return globalDesc;
    }

    public void setGlobalDesc(ArrayList<String> globalDesc) {
        this.globalDesc = globalDesc;
    }

    @Override
    public String toString() {
        String hexHash = Integer.toHexString(Integer.parseInt(hash));
        return String.format("<html>" +
                "HASH: " +
                "<font style=\"color: blue; font-weight: bold;\">%s</font>" +
                "   URL: " +
                "<font style=\"color: red; font-weight: bold;\">%s</font>" +
                "</html>", hexHash, this.url);
    }


    @Override
    public int compareTo(@NotNull InfoObj o) {
        return this.url.compareTo(o.url);
    }
}

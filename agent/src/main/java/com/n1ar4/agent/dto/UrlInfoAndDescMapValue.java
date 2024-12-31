/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.dto;

import java.util.ArrayList;

public class UrlInfoAndDescMapValue {
    public String tag;
    public ArrayList<UrlInfo> urlInfos;
    public ArrayList<String> desc;

    public UrlInfoAndDescMapValue(String tag) {
        this.tag = tag;
        this.urlInfos = new ArrayList<>();
        this.desc = new ArrayList<>();
    }
}

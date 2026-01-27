package com.n1ar4.agent.util;

import com.n1ar4.agent.webserver.FrameworkBaseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FrameworkUtils {
    public static void MergeFrameworkBaseInfoHashMap(HashMap<String, ArrayList<FrameworkBaseInfo>> dstHashMap, HashMap<String, ArrayList<FrameworkBaseInfo>> srcHashMap) {
        if (srcHashMap == null)
            return;
        for (Map.Entry<String, ArrayList<FrameworkBaseInfo>> entry : srcHashMap.entrySet()) {
            String resolverName = entry.getKey();
            if (dstHashMap.containsKey(resolverName)) {
                dstHashMap.get(resolverName).addAll(entry.getValue());
            } else {
                dstHashMap.put(resolverName, entry.getValue());
            }
        }
    }
}
package com.n1ar4.agent.Utils;

import com.n1ar4.agent.service.BasicFrameworkBaseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FrameworkUtils {
    public static void MergeFrameworkBaseInfoHashMap(HashMap<String, ArrayList<BasicFrameworkBaseInfo>> dstHashMap , HashMap<String, ArrayList<BasicFrameworkBaseInfo>> srcHashMap){
        if(srcHashMap == null)
            return;
        for (Map.Entry<String, ArrayList<BasicFrameworkBaseInfo>> entry : srcHashMap.entrySet()) {
            String resolverName = entry.getKey();
            if(dstHashMap.containsKey(resolverName)){
                dstHashMap.get(resolverName).addAll(entry.getValue());
            }else{
                dstHashMap.put(resolverName , entry.getValue());
            }
        }
    }
}
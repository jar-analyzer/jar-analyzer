/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.server.handler;

import com.alibaba.fastjson2.JSON;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.server.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

public class GetClassByClassHandler extends BaseHandler implements HttpHandler {

    // 合法的 Java 类名字符: 字母数字 _ $ . / 以及内部类的 -（极少数情况）
    // 不允许 ".." 路径遍历、控制字符、以及其他注入特殊字符
    private static boolean isValidClassName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (name.contains("..")) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '.' || c == '/' || c == '_' || c == '$';
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        CoreEngine engine = MainForm.getEngine();
        if (engine == null || !engine.isEnabled()) {
            return error();
        }
        String className = getClassName(session);
        if (StringUtil.isNull(className)) {
            return needParam("class");
        }
        // 修复 P3: 非法输入返回结构化错误而不是 null
        if (!isValidClassName(className)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "invalid class name: " + className);
            return buildJSON(JSON.toJSONString(result));
        }
        ClassResult clazz = engine.getClassByClass(className);
        // 修复 P3: 类不存在时也返回结构化错误
        if (clazz == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "class not found: " + className);
            return buildJSON(JSON.toJSONString(result));
        }
        String json = JSON.toJSONString(clazz);
        return buildJSON(json);
    }
}

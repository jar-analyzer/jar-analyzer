/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.server.handler;

import com.alibaba.fastjson2.JSON;
import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class GetCodeFernflowerHandler extends BaseHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        CoreEngine engine = MainForm.getEngine();
        if (engine == null || !engine.isEnabled()) {
            return error();
        }
        String className = getClassName(session);
        String methodName = getMethodName(session);
        String methodDesc = getMethodDesc(session);
        if (StringUtil.isNull(className)) {
            return needParam("class");
        }
        if (StringUtil.isNull(methodName)) {
            return needParam("method");
        }
        try {
            String absPath = engine.getAbsPath(className);
            if (StringUtil.isNull(absPath)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "class file not found: " + className);
                return buildJSON(JSON.toJSONString(result));
            }
            String decompiledCode = DecompileEngine.decompile(Paths.get(absPath));
            if (StringUtil.isNull(decompiledCode)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "failed to decompile class: " + className);
                return buildJSON(JSON.toJSONString(result));
            }
            String methodCode = extractMethodCode(decompiledCode, methodName, methodDesc);
            if (methodCode == null) {
                methodCode = "";
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("className", className);
            result.put("methodName", methodName);
            result.put("methodDesc", methodDesc);
            result.put("fullClassCode", decompiledCode);
            result.put("methodCode", methodCode);
            return buildJSON(JSON.toJSONString(result));
        } catch (Exception e) {
            logger.error("error getting method code: " + e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "error: " + e.getMessage());
            return buildJSON(JSON.toJSONString(result));
        }
    }
}

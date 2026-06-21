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
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.server.NanoHTTPD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GetMethodsByStrHandler extends BaseHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger();

    // 防止 SQL LIKE %x% 在 string_table 上扫出海量行导致超时:
    // 1. 关键字最少 2 个字符
    // 2. 结果数上限保护
    private static final int MIN_QUERY_LEN = 2;
    private static final int MAX_RESULTS = 500;
    private static final int MAX_QUERY_LEN = 256;

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        CoreEngine engine = MainForm.getEngine();
        if (engine == null || !engine.isEnabled()) {
            return error();
        }
        String str = getStr(session);
        // 修复 P3: 空参错误消息张冠李戴 ("class" -> "str")
        if (StringUtil.isNull(str)) {
            return needParam("str");
        }
        // 修复 P0: 输入边界保护，避免短字符串导致 SQL LIKE 全表扫描爆炸
        String trimmed = str.trim();
        if (trimmed.length() < MIN_QUERY_LEN) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "query string too short, min length is " + MIN_QUERY_LEN);
            return buildJSON(JSON.toJSONString(result));
        }
        if (trimmed.length() > MAX_QUERY_LEN) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "query string too long, max length is " + MAX_QUERY_LEN);
            return buildJSON(JSON.toJSONString(result));
        }
        try {
            ArrayList<MethodResult> res = engine.getMethodsByStr(trimmed);
            // 防止结果过大导致响应膨胀 / LLM 上下文污染
            boolean truncated = false;
            if (res.size() > MAX_RESULTS) {
                ArrayList<MethodResult> limited = new ArrayList<>(res.subList(0, MAX_RESULTS));
                res = limited;
                truncated = true;
            }
            if (truncated) {
                Map<String, Object> wrap = new HashMap<>();
                wrap.put("success", true);
                wrap.put("truncated", true);
                wrap.put("limit", MAX_RESULTS);
                wrap.put("results", res);
                return buildJSON(JSON.toJSONString(wrap));
            }
            String json = JSON.toJSONString(res);
            return buildJSON(json);
        } catch (Exception e) {
            // 修复 P0: 捕获异常防止状态损坏 / 连接泄漏向上扩散
            logger.error("get_methods_by_str error: " + e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "error: " + e.getMessage());
            return buildJSON(JSON.toJSONString(result));
        }
    }
}

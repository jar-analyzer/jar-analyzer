package me.n1ar4.jar.analyzer.server.handler;

import com.alibaba.fastjson2.JSON;
import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import java.util.ArrayList;

public class GetCallersLikeHandler extends BaseHandler implements HttpHandler {
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        CoreEngine engine = MainForm.getEngine();
        if (engine == null || !engine.isEnabled()) {
            return error();
        }
        String clazz = getClassName(session);
        String method = getMethodName(session);
        String desc = getMethodDesc(session);
        if (StringUtil.isNull(clazz)) {
            return needParam("class");
        }
        if (StringUtil.isNull(method)) {
            return needParam("method");
        }
        ArrayList<MethodResult> res = engine.getCallersLike(clazz, method, desc);
        String json = JSON.toJSONString(res);
        return buildJSON(json);
    }
}
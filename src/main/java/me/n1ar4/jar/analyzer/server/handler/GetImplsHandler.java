package me.n1ar4.jar.analyzer.server.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;

import java.util.ArrayList;

public class GetImplsHandler extends BaseHandler implements HttpHandler {
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        CoreEngine engine = MainForm.getEngine();
        if (engine == null) {
            return error();
        }
        String clazz = getClassName(session);
        String method = getMethodName(session);
        String desc = getMethodDesc(session);
        ArrayList<MethodResult> res = engine.getImpls(clazz, method, desc);
        String json = JSON.toJSONString(res, JSONWriter.Feature.PrettyFormat);
        return buildJSON(json);
    }
}

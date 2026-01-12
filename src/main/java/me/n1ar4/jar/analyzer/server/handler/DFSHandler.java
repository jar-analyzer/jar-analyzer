package me.n1ar4.jar.analyzer.server.handler;

import com.alibaba.fastjson2.JSON;
import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.dfs.DFSEngine;
import me.n1ar4.jar.analyzer.dfs.DFSResult;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;

import java.util.List;

// http://127.0.0.1:10032/api/dfs_analyze?
// sink_class=java/lang/Runtime&sink_method=exec&sink_method_desc=(Ljava/lang/String;)Ljava/lang/Process;&
// source_class=&source_method=&source_method_desc=&
// depth=10&limit=10&from_sink=true&search_null_source=true
public class DFSHandler extends BaseHandler implements HttpHandler {
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        try {
            CoreEngine engine = MainForm.getEngine();
            if (engine == null || !engine.isEnabled()) {
                return error();
            }

            String sinkClass = getSinkClass(session);
            String sinkMethod = getSinkMethod(session);
            String sinkDesc = getSinkDesc(session);

            String sourceClass = getSourceClass(session);
            String sourceMethod = getSourceMethod(session);
            String sourceDesc = getSourceDesc(session);

            boolean fromSink = Boolean.parseBoolean(getFromSink(session));
            boolean searchNullSource = Boolean.parseBoolean(getSearchNullSource(session));
            int depth = Integer.parseInt(getDepth(session));
            int limit = Integer.parseInt(getLimit(session));

            DFSEngine dfsEngine = new DFSEngine(null, fromSink, searchNullSource, depth);
            dfsEngine.setMaxLimit(limit);
            dfsEngine.setSink(sinkClass, sinkMethod, sinkDesc);
            dfsEngine.setSource(sourceClass, sourceMethod, sourceDesc);

            dfsEngine.doAnalyze();

            List<DFSResult> dfsResults = dfsEngine.getResults();

            String json = JSON.toJSONString(dfsResults);
            return buildJSON(json);
        } catch (Exception ex) {
            return errorMsg(ex.getMessage());
        }
    }

    public String getSinkClass(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("sink_class");
        if (p == null || p.isEmpty()) {
            return "";
        }
        String param = p.get(0);
        return param.replace('.', '/');
    }

    public String getSinkMethod(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("sink_method");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getSinkDesc(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("sink_method_desc");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getSourceClass(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("source_class");
        if (p == null || p.isEmpty()) {
            return "";
        }
        String param = p.get(0);
        return param.replace('.', '/');
    }

    public String getSourceMethod(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("source_method");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getSourceDesc(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("source_method_desc");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getDepth(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("depth");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getLimit(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("limit");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getFromSink(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("from_sink");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getSearchNullSource(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("search_null_source");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }
}

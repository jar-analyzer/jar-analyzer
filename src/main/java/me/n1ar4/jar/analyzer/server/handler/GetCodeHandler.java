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

import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import java.nio.file.Paths;
import java.util.Base64;

public class GetCodeHandler extends BaseHandler implements HttpHandler {
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        CoreEngine engine = MainForm.getEngine();
        if (engine == null || !engine.isEnabled()) {
            return error();
        }
        String clazz = getClassName(session);
        if (StringUtil.isNull(clazz)) {
            return needParam("class");
        }
        String code = DecompileEngine.decompile(Paths.get(engine.getAbsPath(clazz)));
        if (code == null) {
            return buildJSON("{}");
        }
        String json = String.format("{\"%s\":\"%s\"}", "code",
                Base64.getEncoder().encodeToString(code.getBytes()));
        return buildJSON(json);
    }
}

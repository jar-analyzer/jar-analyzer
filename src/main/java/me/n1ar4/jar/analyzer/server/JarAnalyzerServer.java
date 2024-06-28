package me.n1ar4.jar.analyzer.server;

import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

public class JarAnalyzerServer extends NanoHTTPD {
    private static final Logger logger = LogManager.getLogger();

    public JarAnalyzerServer() {
        super("0.0.0.0", GlobalOptions.getServerPort());
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            logger.info("start http server at: {}", getListeningPort());
        } catch (Exception e) {
            logger.error("start http server failed: {}", e);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        return PathMatcher.handleReq(session);
    }
}
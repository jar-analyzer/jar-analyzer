package me.n1ar4.jar.analyzer.gui.update;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.http.HttpResponse;
import me.n1ar4.jar.analyzer.http.Y4Client;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;

@SuppressWarnings("all")
public class UpdateChecker {
    private static final Logger logger = LogManager.getLogger();

    public static void check() {
        MainForm instance = MainForm.getInstance();
        new Thread(() -> {
            logger.info("check update from aliyun oss");
            HttpResponse resp = Y4Client.INSTANCE.get(Const.checkUpdateUrl);
            String body = new String(resp.getBody());
            if (body.isEmpty()) {
                return;
            }
            String ver = body.trim();
            LogUtil.info("latest: " + ver);
            if (!ver.equals(Const.version)) {
                String output;
                output = String.format("New Version!\n%s: %s\n%s: %s\n%s",
                        "Current Version", Const.version,
                        "Latest Version", ver,
                        "https://github.com/jar-analyzer/jar-analyzer");
                JOptionPane.showMessageDialog(instance.getMasterPanel(), output);
            }
        }).start();
    }
}

package me.n1ar4.jar.analyzer.gui.update;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.http.HttpResponse;
import me.n1ar4.jar.analyzer.http.Y4Client;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;

public class UpdateChecker {
    public static void checkUpdate() {
        MainForm instance = MainForm.getInstance();
        new Thread(() -> {
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

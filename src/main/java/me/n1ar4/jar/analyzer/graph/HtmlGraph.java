package me.n1ar4.jar.analyzer.graph;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.utils.OSUtil;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.List;

public class HtmlGraph {
    public static void run() {
        JButton btn = MainForm.getInstance().getHtmlGraphBtn();
        btn.addActionListener(e -> {
            MethodResult mr = MainForm.getCurMethod();
            List<MethodResult> calleeList = MainForm.getEngine().getCallee(
                    mr.getClassName(), mr.getMethodName(), mr.getMethodDesc());
            List<MethodResult> callerList = MainForm.getEngine().getCallers(
                    mr.getClassName(), mr.getMethodName(), mr.getMethodDesc());
            String fileName = RenderEngine.processGraph(mr, callerList, calleeList);
            if (fileName == null) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "ERROR GENERATE HTML GRAPH");
                return;
            }
            String absPath = Paths.get(fileName).toAbsolutePath().toString();
            if (OSUtil.isWindows()) {
                String cmd = String.format("start %s", absPath);
                String[] xrayCmd = new String[]{"cmd.exe", "/c", String.format("%s", cmd)};
                exec(xrayCmd);
            } else {
                String cmd = String.format("open %s", absPath);
                String[] xrayCmd = new String[]{"/bin/bash", "-c", String.format("%s", cmd)};
                exec(xrayCmd);
            }
        });
    }

    public static Process exec(String[] cmdArray) {
        try {
            String cmd = String.join(" ", cmdArray);
            LogUtil.info(String.format("run cmd: %s", cmd));
            return new ProcessBuilder(cmdArray).start();
        } catch (Exception ignored) {
        }
        return null;
    }
}

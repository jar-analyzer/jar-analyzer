package me.n1ar4.jar.analyzer.graph;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.OpenUtil;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.List;

public class HtmlGraph {
    public static void run() {
        JButton btn = MainForm.getInstance().getHtmlGraphBtn();
        btn.addActionListener(e -> {
            MethodResult mr = MainForm.getCurMethod();

            if (mr == null) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "YOU SHOULD SELECT A METHOD FIRST");
                return;
            }

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
            OpenUtil.open(absPath);
        });
    }
}

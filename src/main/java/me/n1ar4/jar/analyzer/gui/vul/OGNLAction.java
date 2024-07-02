package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;

public class OGNLAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getOGNLGetValueButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            String className = "ognl/Ognl";
            String methodName = "getValue";

            new Thread(() -> CoreHelper.refreshCallSearch(
                    className, methodName, null)).start();
        });
    }
}

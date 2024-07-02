package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;

public class JNDIVulAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getJNDIButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            String className = "javax/naming/Context";
            String methodName = "lookup";
            String methodDesc = "(Ljava/lang/String;)Ljava/lang/Object;";

            new Thread(() -> CoreHelper.refreshCallSearch(
                    className, methodName, methodDesc)).start();
        });
    }
}

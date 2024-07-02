package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;

public class RuntimeExecAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getRuntimeExecButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            String className = "java/lang/Runtime";
            String methodName = "exec";

            new Thread(() -> CoreHelper.refreshCallSearch(
                    className, methodName, null)).start();
        });
    }
}

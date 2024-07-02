package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;

public class DefineClassAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getDefineClassButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            String methodName = "defineClass";
            String methodDesc = "(Ljava/lang/String;[BII)Ljava/lang/Class;";

            new Thread(() -> CoreHelper.refreshCallSearch(
                    null, methodName, methodDesc)).start();
        });
    }
}

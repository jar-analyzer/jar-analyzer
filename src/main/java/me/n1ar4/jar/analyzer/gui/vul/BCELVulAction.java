package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;

public class BCELVulAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getBCELLoadClassButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            String className = "com/sun/org/apache/bcel/internal/util/ClassLoader";
            String methodName = "loadClass";
            String methodDesc = "(Ljava/lang/String;)Ljava/lang/Class;";

            new Thread(() -> CoreHelper.refreshCallSearch(
                    className, methodName, methodDesc)).start();
        });
    }
}

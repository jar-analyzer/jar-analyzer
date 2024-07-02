package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;

public class ReadObjectVulAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getReadObjectButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            String className = "java/io/ObjectInputStream";
            String methodName = "readObject";
            String methodDesc = "()Ljava/lang/Object;";

            new Thread(() -> CoreHelper.refreshCallSearch(
                    className, methodName, methodDesc)).start();
        });
    }
}

package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;

public class ScriptEngineAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getScriptEngineEvalButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            String className = "javax/script/ScriptEngine";
            String methodName = "eval";
            String methodDesc = "(Ljava/lang/String;)Ljava/lang/Object;";

            new Thread(() -> CoreHelper.refreshCallSearch(
                    className, methodName, methodDesc)).start();
        });
    }
}

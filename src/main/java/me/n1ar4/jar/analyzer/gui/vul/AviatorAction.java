package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class AviatorAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getAviatorExecuteButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // com/googlecode/aviator/AviatorEvaluatorInstance.execute
            SearchCondition ar = new SearchCondition();
            ar.setClassName("com/googlecode/aviator/AviatorEvaluatorInstance");
            ar.setMethodName("execute");
            conditions.add(ar);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}

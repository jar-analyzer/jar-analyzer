package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class RhinoAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getRhinoEvalButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // org/mozilla/javascript/Context.evaluateString
            SearchCondition ar = new SearchCondition();
            ar.setClassName("org/mozilla/javascript/Context");
            ar.setMethodName("evaluateString");
            conditions.add(ar);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}

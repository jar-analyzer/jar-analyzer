package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class MvelAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getMvelEvalButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // org/mvel2/MVEL.eval
            SearchCondition ar = new SearchCondition();
            ar.setClassName("org/mvel2/MVEL");
            ar.setMethodName("eval");
            conditions.add(ar);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}

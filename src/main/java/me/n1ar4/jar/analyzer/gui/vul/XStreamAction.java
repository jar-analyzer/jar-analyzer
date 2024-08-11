package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class XStreamAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getXStreamButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // com/thoughtworks/xstream/XStream.fromXML
            SearchCondition ar = new SearchCondition();
            ar.setClassName("com/thoughtworks/xstream/XStream");
            ar.setMethodName("fromXML");
            conditions.add(ar);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}

package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class ZIPVulAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getUnzipButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            SearchCondition zi = new SearchCondition();
            zi.setClassName("java/util/zip/ZipInputStream");
            zi.setMethodName("<init>");
            conditions.add(zi);

            SearchCondition zg = new SearchCondition();
            zg.setClassName("java/util/zip/ZipFile");
            zg.setMethodName("getInputStream");
            conditions.add(zg);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}

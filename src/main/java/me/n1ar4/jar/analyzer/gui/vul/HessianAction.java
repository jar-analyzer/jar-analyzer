package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class HessianAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getHessianButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // com/caucho/hessian/io/AbstractHessianInput.readObject
            SearchCondition ar = new SearchCondition();
            ar.setClassName("com/caucho/hessian/io/AbstractHessianInput");
            ar.setMethodName("readObject");
            conditions.add(ar);

            // com/caucho/hessian/io/HessianInput.readObject
            SearchCondition hr = new SearchCondition();
            hr.setClassName("com/caucho/hessian/io/HessianInput");
            hr.setMethodName("readObject");
            conditions.add(hr);

            // com/caucho/hessian/io/Hessian2Input.readObject
            SearchCondition h2r = new SearchCondition();
            h2r.setClassName("com/caucho/hessian/io/Hessian2Input");
            h2r.setMethodName("readObject");
            conditions.add(h2r);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}

package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class FastjsonVulAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getFastjsonButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }
            List<SearchCondition> conditions = new ArrayList<>();

            // com/alibaba/fastjson/JSON.parse
            SearchCondition jp = new SearchCondition();
            jp.setClassName("com/alibaba/fastjson/JSON");
            jp.setMethodName("parse");
            conditions.add(jp);

            // com/alibaba/fastjson/JSON.parseObject
            SearchCondition jpo = new SearchCondition();
            jpo.setClassName("com/alibaba/fastjson/JSON");
            jpo.setMethodName("parseObject");
            conditions.add(jpo);

            // com/alibaba/fastjson/JSONObject.parse
            SearchCondition jop = new SearchCondition();
            jop.setClassName("com/alibaba/fastjson/JSONObject");
            jop.setMethodName("parse");
            conditions.add(jop);

            // com/alibaba/fastjson/JSONObject.parseObject
            SearchCondition jopo = new SearchCondition();
            jopo.setClassName("com/alibaba/fastjson/JSON");
            jopo.setMethodName("parseObject");
            conditions.add(jopo);

            // com/alibaba/fastjson/JSONArray.parse
            SearchCondition jap = new SearchCondition();
            jap.setClassName("com/alibaba/fastjson/JSONArray");
            jap.setMethodName("parse");
            conditions.add(jap);

            // com/alibaba/fastjson/JSONArray.parseObject
            SearchCondition japo = new SearchCondition();
            japo.setClassName("com/alibaba/fastjson/JSONArray");
            japo.setMethodName("parseObject");
            conditions.add(japo);

            // com/alibaba/fastjson/support/spring/FastJsonHttpMessageConverter.<init>
            SearchCondition fjc = new SearchCondition();
            fjc.setClassName("com/alibaba/fastjson/support/spring/FastJsonHttpMessageConverter");
            fjc.setMethodName("<init>");
            conditions.add(fjc);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}

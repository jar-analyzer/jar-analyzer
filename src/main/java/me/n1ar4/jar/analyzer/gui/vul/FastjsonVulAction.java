/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

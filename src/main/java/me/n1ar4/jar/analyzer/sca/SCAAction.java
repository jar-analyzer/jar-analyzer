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

package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.sca.dto.CVEData;
import me.n1ar4.jar.analyzer.sca.dto.SCARule;
import me.n1ar4.jar.analyzer.sca.log.SCALogger;

import java.util.List;
import java.util.Map;

public class SCAAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null) {
            return;
        }
        instance.getScaOutHtmlRadio().setSelected(true);
        instance.getScaOutConsoleRadio().setSelected(false);
        instance.getOutputFileText().setEnabled(false);
        // 设置输出
        if (SCALogger.logger == null) {
            SCALogger.logger = new SCALogger(instance.getScaConsoleArea());
        }
        instance.getScaLog4jBox().setEnabled(true);
        instance.getScaLog4jBox().setSelected(true);
        instance.getScaFastjsonBox().setEnabled(true);
        instance.getScaFastjsonBox().setSelected(true);
        instance.getScaShiroBox().setEnabled(true);
        instance.getScaShiroBox().setSelected(true);
        // 解析 CVE 数据库
        Map<String, CVEData> cveMap = SCAVulDB.getCVEMap();
        // 解析规则
        List<SCARule> log4jRuleList = SCAParser.getApacheLog4j2Rules();
        List<SCARule> fastjsonRuleList = SCAParser.getFastjsonRules();
        List<SCARule> shiroRuleList = SCAParser.getShiroRules();
        // 按钮绑定
        instance.getScaOpenBtn().addActionListener(new SCAOpenActionListener());
        instance.getScaResultOpenBtn().addActionListener(new SCAOpenResultListener());
        instance.getScaStartBtn().addActionListener(
                new SCAStartActionListener(
                        cveMap,
                        log4jRuleList,
                        fastjsonRuleList,
                        shiroRuleList));
    }
}

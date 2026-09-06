/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * sanitizer.json 的加载校验。
 * <p>
 * 历史上该文件出过两类静默失效问题：
 * <ul>
 *   <li>零参数方法配 ALL_PARAMS（如 ESAPI.encoder()）——anyArgTainted 对零参数
 *       永远为 false，规则永不触发（死规则）</li>
 *   <li>虚方法 paramIndex 按参数序数而非 locals 槽位理解（encodeForSQL 曾配 1）</li>
 * </ul>
 * desc 写错不会报错只会静默失效，这里在 CI 提前拦截。
 */
class SanitizerRuleTest {

    @Test
    void noDeadZeroArgAllParamsRules() {
        List<Sanitizer> rules = loadRules();
        for (Sanitizer s : rules) {
            int argCount = Type.getArgumentTypes(s.getMethodDesc()).length;
            if (argCount == 0) {
                // 零参数方法没有任何入参可判定，ALL_PARAMS/任意索引都无法触发
                assertTrue(s.getParamIndex() != Sanitizer.ALL_PARAMS,
                        "死规则：零参数方法 " + s.getClassName() + "." + s.getMethodName()
                                + " 配 ALL_PARAMS 永不触发");
            }
        }
    }

    @Test
    void anchorRulesPresent() {
        List<Sanitizer> rules = loadRules();
        assertTrue(rules.size() >= 40, "净化规则数量异常: " + rules.size());

        // encodeForSQL 虚方法：0=this 1=Codec 2=String，索引必须是 2
        Sanitizer sql = find(rules, "org/owasp/esapi/Encoder", "encodeForSQL");
        assertNotNull(sql);
        assertTrue(sql.getParamIndex() == 2,
                "encodeForSQL 应指向第 2 槽（0=this 1=Codec 2=String）");

        // 数值转换净化（防 parseInt 返回值被通用传播染污）
        assertNotNull(find(rules, "java/lang/Integer", "parseInt"),
                "缺少 Integer.parseInt 数值转换净化");
        // 现代 XSS 净化库
        assertNotNull(find(rules, "org/owasp/encoder/Encode", "forHtml"),
                "缺少 OWASP Java Encoder 规则");
        assertNotNull(find(rules, "org/jsoup/Jsoup", "clean"),
                "缺少 jsoup clean 规则");
    }

    private static List<Sanitizer> loadRules() {
        InputStream in = SanitizerRuleTest.class.getClassLoader()
                .getResourceAsStream("sanitizer.json");
        SanitizerRule rule = SanitizerRule.loadJSON(in);
        assertNotNull(rule.getRules());
        return rule.getRules();
    }

    private static Sanitizer find(List<Sanitizer> rules, String cls, String method) {
        for (Sanitizer s : rules) {
            if (s.getClassName().equals(cls) && s.getMethodName().equals(method)) {
                return s;
            }
        }
        return null;
    }
}

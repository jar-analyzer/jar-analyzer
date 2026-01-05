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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import me.n1ar4.jar.analyzer.utils.IOUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.InputStream;
import java.util.List;

public class SanitizerRule {
    private static final Logger logger = LogManager.getLogger();

    @JSONField
    private List<Sanitizer> rules;

    public static SanitizerRule loadJSON(InputStream in) {
        try {
            String jsonData = IOUtil.readString(in);
            if (jsonData == null || jsonData.trim().isEmpty()) {
                logger.warn("sanitizer json data is empty");
                return new SanitizerRule();
            }
            SanitizerRule rule = JSON.parseObject(jsonData, SanitizerRule.class);
            if (rule == null) {
                logger.warn("failed to parse sanitizer json");
                return new SanitizerRule();
            }
            logger.info("loaded {} sanitizer rules", rule.getRules() != null ? rule.getRules().size() : 0);
            return rule;
        } catch (Exception ex) {
            logger.error("error loading sanitizer rules: {}", ex.toString());
            return new SanitizerRule();
        }
    }

    public List<Sanitizer> getRules() {
        return rules;
    }

    public void setRules(List<Sanitizer> rules) {
        this.rules = rules;
    }
}

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PropagationRuleSet {
    private static final Logger logger = LogManager.getLogger();

    @JSONField
    private List<PropagationRule> rules;

    public static PropagationRuleSet loadJSON(InputStream in) {
        try {
            if (in == null) {
                return empty();
            }
            String jsonData = IOUtil.readString(in);
            if (jsonData == null || jsonData.trim().isEmpty()) {
                return empty();
            }
            PropagationRuleSet set = JSON.parseObject(jsonData, PropagationRuleSet.class);
            if (set == null) {
                return empty();
            }
            logger.info("loaded {} propagation rules",
                    set.getRules() == null ? 0 : set.getRules().size());
            return set;
        } catch (Exception ex) {
            logger.error("error loading propagation rules: {}", ex.toString());
            return empty();
        }
    }

    private static PropagationRuleSet empty() {
        PropagationRuleSet s = new PropagationRuleSet();
        s.rules = new ArrayList<>();
        return s;
    }

    public List<PropagationRule> getRules() {
        return rules == null ? Collections.<PropagationRule>emptyList() : rules;
    }

    public void setRules(List<PropagationRule> rules) {
        this.rules = rules;
    }
}

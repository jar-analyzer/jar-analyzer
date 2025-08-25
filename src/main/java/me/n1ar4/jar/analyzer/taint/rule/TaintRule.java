/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint.rule;

import java.io.InputStream;
import java.util.List;

public class TaintRule {
    private List<PropagatorRule> propagatorRuleList;
    private List<SanitizerRule> sanitizerRuleList;
    private List<SourceRule> sourceRulesList;
    private List<SinkRule> sinkRuleList;

    public static TaintRule fromJsonFile(InputStream is) {
        // TODO
        return null;
    }

    public List<PropagatorRule> getPropagatorRuleList() {
        return propagatorRuleList;
    }

    public void setPropagatorRuleList(List<PropagatorRule> propagatorRuleList) {
        this.propagatorRuleList = propagatorRuleList;
    }

    public List<SanitizerRule> getSanitizerRuleList() {
        return sanitizerRuleList;
    }

    public void setSanitizerRuleList(List<SanitizerRule> sanitizerRuleList) {
        this.sanitizerRuleList = sanitizerRuleList;
    }

    public List<SourceRule> getSourceRulesList() {
        return sourceRulesList;
    }

    public void setSourceRulesList(List<SourceRule> sourceRulesList) {
        this.sourceRulesList = sourceRulesList;
    }

    public List<SinkRule> getSinkRuleList() {
        return sinkRuleList;
    }

    public void setSinkRuleList(List<SinkRule> sinkRuleList) {
        this.sinkRuleList = sinkRuleList;
    }
}

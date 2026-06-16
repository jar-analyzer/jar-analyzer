/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagNode;
import me.n1ar4.jar.analyzer.ai.workflow.nodes.*;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;

import java.awt.*;

/**
 * 节点视觉分类。
 * <p>
 * 提供 SVG 图标 + 主题色（边框/Header 背景），用于在画布上区分不同节点。
 * 颜色与 n8n 的视觉相近：紫=trigger/agent、蓝=http、绿=merge/transform、橙=loop、紫=if、红=report。
 */
public enum NodeKind {

    TRIGGER("Trigger", new Color(255, 176, 32), new Color(255, 248, 220)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfTriggerIcon;
        }
    },
    CONSTANTS("Constants", new Color(123, 97, 255), new Color(244, 240, 255)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfConstantsIcon;
        }
    },
    HTTP("HTTP", new Color(63, 169, 245), new Color(229, 244, 255)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfHttpIcon;
        }
    },
    MERGE("Merge", new Color(39, 174, 96), new Color(231, 248, 240)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfMergeIcon;
        }
    },
    LOOP("Loop", new Color(230, 126, 34), new Color(253, 240, 224)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfLoopIcon;
        }
    },
    IF("If", new Color(155, 81, 224), new Color(244, 236, 255)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfIfIcon;
        }
    },
    TRANSFORM("Transform", new Color(12, 166, 120), new Color(230, 252, 245)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfTransformIcon;
        }
    },
    AGENT("AI Agent", new Color(123, 97, 255), new Color(244, 240, 255)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfAgentIcon;
        }
    },
    REPORT("Report", new Color(224, 49, 49), new Color(255, 245, 245)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfReportIcon;
        }
    },
    GENERIC("Node", new Color(120, 120, 120), new Color(245, 245, 245)) {
        @Override
        public FlatSVGIcon icon() {
            return SvgManager.WfTransformIcon;
        }
    };

    private final String label;
    private final Color accent;
    private final Color headerBg;

    NodeKind(String label, Color accent, Color headerBg) {
        this.label = label;
        this.accent = accent;
        this.headerBg = headerBg;
    }

    public String getLabel() {
        return label;
    }

    public Color getAccent() {
        return accent;
    }

    public Color getHeaderBg() {
        return headerBg;
    }

    public abstract FlatSVGIcon icon();

    /**
     * 根据 DAG 节点的实际 Java 类型推断节点视觉分类。
     */
    public static NodeKind of(DagNode node) {
        if (node == null) {
            return GENERIC;
        }
        if (node instanceof ConstantsNode) {
            return CONSTANTS;
        }
        if (node instanceof HttpGetNode) {
            return HTTP;
        }
        if (node instanceof MergeNode) {
            return MERGE;
        }
        if (node instanceof LoopOverItemsNode) {
            return LOOP;
        }
        if (node instanceof IfNode) {
            return IF;
        }
        if (node instanceof TransformNode) {
            return TRANSFORM;
        }
        if (node instanceof AiAgentNode) {
            return AGENT;
        }
        return GENERIC;
    }
}

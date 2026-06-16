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

import me.n1ar4.jar.analyzer.ai.workflow.core.NodeStatus;

import java.awt.*;

/**
 * 节点视图：在画布上的位置 / 尺寸 / 状态徽章 / 视觉分类。
 */
public final class NodeView {

    /**
     * 默认节点尺寸（与 n8n 视觉接近）。
     */
    public static final int WIDTH = 220;
    public static final int HEIGHT = 72;
    public static final int CORNER = 14;

    private final String id;
    private final String title;
    private final NodeKind kind;

    /**
     * 画布坐标系（model）下的左上角。
     */
    private double x;
    private double y;

    /**
     * 当前状态。
     */
    private NodeStatus status = NodeStatus.PENDING;

    /**
     * 状态对应的描述（鼠标悬停可见、画布右下角显示）。
     */
    private String statusMessage;

    public NodeView(String id, String title, NodeKind kind, double x, double y) {
        this.id = id;
        this.title = title == null ? id : title;
        this.kind = kind == null ? NodeKind.GENERIC : kind;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public NodeKind getKind() {
        return kind;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status == null ? NodeStatus.PENDING : status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public double getCenterX() {
        return x + WIDTH / 2.0;
    }

    public double getCenterY() {
        return y + HEIGHT / 2.0;
    }

    public double getOutputAnchorX() {
        return x + WIDTH;
    }

    public double getOutputAnchorY() {
        return y + HEIGHT / 2.0;
    }

    public double getInputAnchorX() {
        return x;
    }

    public double getInputAnchorY() {
        return y + HEIGHT / 2.0;
    }

    public double getBottomAnchorX() {
        return x + WIDTH / 2.0;
    }

    public double getBottomAnchorY() {
        return y + HEIGHT;
    }

    public double getTopAnchorX() {
        return x + WIDTH / 2.0;
    }

    public double getTopAnchorY() {
        return y;
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= x + WIDTH && py >= y && py <= y + HEIGHT;
    }

    /**
     * 状态颜色（用作发光描边）。
     */
    public Color statusColor() {
        switch (status) {
            case RUNNING:
                return new Color(255, 176, 32);   // amber
            case SUCCESS:
                return new Color(39, 174, 96);    // green
            case FAILED:
                return new Color(224, 49, 49);    // red
            case SKIPPED:
                return new Color(160, 160, 160);  // gray
            case CANCELLED:
                return new Color(120, 120, 120);
            case PENDING:
            default:
                return null;
        }
    }
}

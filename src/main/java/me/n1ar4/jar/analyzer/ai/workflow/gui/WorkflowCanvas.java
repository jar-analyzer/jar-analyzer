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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Workflow 可视化画布。
 * <p>
 * 视觉特性：
 * <ul>
 *   <li>深色背景 + 点阵网格（n8n 风格）</li>
 *   <li>圆角节点卡片 + 软阴影 + 头部图标 + 状态徽章</li>
 *   <li>节点之间用三次贝塞尔曲线连接 + 末端箭头</li>
 *   <li>RUNNING 状态边发光（脉冲动画 30fps）</li>
 *   <li>鼠标拖动空白处平移；滚轮缩放（0.4x ~ 2.0x）；节点可拖动微调位置</li>
 * </ul>
 * <p>
 * 线程模型：
 * <ul>
 *   <li>{@link #setStatus(String, NodeStatus, String)} 可在任意线程调用，会切到 EDT 后再 repaint</li>
 *   <li>所有 paint/mouse 事件都在 EDT 内处理</li>
 * </ul>
 */
public final class WorkflowCanvas extends JPanel {

    private static final long serialVersionUID = 1L;

    // 视觉常量 -----------------------------------------------------------
    private static final Color BG_TOP = new Color(36, 38, 46);
    private static final Color BG_BOTTOM = new Color(24, 26, 32);
    private static final Color GRID_DOT = new Color(255, 255, 255, 28);
    private static final Color CARD_BG = new Color(48, 51, 60);
    private static final Color CARD_BORDER = new Color(82, 86, 96);
    private static final Color CARD_TITLE = new Color(232, 235, 240);
    private static final Color CARD_SUBTITLE = new Color(170, 175, 185);
    private static final Color EDGE_COLOR = new Color(140, 145, 158);
    private static final Color EDGE_ACTIVE = new Color(255, 176, 32);
    private static final Color SHADOW = new Color(0, 0, 0, 80);

    private static final int GRID_SPACING = 24;

    // 状态 ---------------------------------------------------------------
    private WorkflowGraphModel model;
    private final Map<String, NodeView> idToView = new HashMap<>();

    /**
     * 视图变换：缩放 + 平移。
     */
    private double scale = 0.95;
    private double offsetX = 0;
    private double offsetY = 0;

    /**
     * 拖动状态。
     */
    private int lastDragX;
    private int lastDragY;
    private boolean panning;
    private NodeView draggingNode;

    /**
     * RUNNING 边发光动画用的相位（0..1）。
     */
    private float runningPhase;

    /**
     * 节点点击监听（GUI 主类用来显示节点详情）。
     */
    private NodeClickListener listener;

    public WorkflowCanvas() {
        setOpaque(true);
        setBackground(BG_BOTTOM);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                lastDragX = e.getX();
                lastDragY = e.getY();
                NodeView hit = hitTest(e.getX(), e.getY());
                if (hit != null) {
                    draggingNode = hit;
                    panning = false;
                } else {
                    draggingNode = null;
                    panning = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingNode = null;
                panning = false;
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                NodeView hit = hitTest(e.getX(), e.getY());
                if (hit != null && listener != null) {
                    listener.onNodeClicked(hit);
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastDragX;
                int dy = e.getY() - lastDragY;
                lastDragX = e.getX();
                lastDragY = e.getY();
                if (draggingNode != null) {
                    draggingNode.setLocation(
                            draggingNode.getX() + dx / scale,
                            draggingNode.getY() + dy / scale);
                    repaint();
                } else if (panning) {
                    offsetX += dx;
                    offsetY += dy;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                NodeView hit = hitTest(e.getX(), e.getY());
                setCursor(hit != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = -e.getPreciseWheelRotation() * 0.1;
                double next = Math.max(0.3, Math.min(2.0, scale + delta));
                if (next == scale) {
                    return;
                }
                // 以鼠标位置为缩放中心
                double mx = e.getX();
                double my = e.getY();
                double oldScale = scale;
                offsetX = mx - (mx - offsetX) * (next / oldScale);
                offsetY = my - (my - offsetY) * (next / oldScale);
                scale = next;
                repaint();
            }
        });

        // 30fps 动画 timer
        Timer timer = new Timer(33, e -> {
            runningPhase = (runningPhase + 0.03f) % 1.0f;
            // 仅当存在 RUNNING 节点时才需要 repaint
            if (hasRunningNode()) {
                repaint();
            }
        });
        timer.setRepeats(true);
        timer.start();

        // 第一次 layout 完成后自动 fit
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (!autoFitDone && getWidth() > 0 && getHeight() > 0 && model != null) {
                    zoomToFit();
                    autoFitDone = true;
                    repaint();
                }
            }
        });
    }

    private boolean autoFitDone = false;

    // ------------------- 公开 API -------------------

    public void setModel(WorkflowGraphModel model) {
        this.model = model;
        this.idToView.clear();
        this.autoFitDone = false;
        if (model != null) {
            for (NodeView nv : model.getNodes()) {
                this.idToView.put(nv.getId(), nv);
            }
            // 自适应初始视图
            zoomToFit();
        }
        repaint();
    }

    public WorkflowGraphModel getModel() {
        return model;
    }

    /**
     * 更新节点状态（线程安全，自动切 EDT）。
     */
    public void setStatus(final String nodeId, final NodeStatus status, final String message) {
        if (nodeId == null) {
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                NodeView nv = idToView.get(nodeId);
                if (nv == null) {
                    return;
                }
                nv.setStatus(status);
                nv.setStatusMessage(message);
                repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public void resetAllStatus() {
        if (model == null) {
            return;
        }
        for (NodeView nv : model.getNodes()) {
            nv.setStatus(NodeStatus.PENDING);
            nv.setStatusMessage(null);
        }
        repaint();
    }

    public void zoomToFit() {
        if (model == null || model.getNodes().isEmpty()) {
            scale = 1.0;
            offsetX = 0;
            offsetY = 0;
            return;
        }
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            // 还没 layout，下次 resize 再 fit
            return;
        }
        int gw = model.totalWidth();
        int gh = model.totalHeight();
        if (gw <= 0 || gh <= 0) {
            return;
        }
        double sx = (w - 40.0) / gw;
        double sy = (h - 40.0) / gh;
        scale = Math.max(0.3, Math.min(1.2, Math.min(sx, sy)));
        offsetX = (w - gw * scale) / 2.0;
        offsetY = 20;
    }

    public interface NodeClickListener {
        void onNodeClicked(NodeView node);
    }

    public void setNodeClickListener(NodeClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1100, 600);
    }

    // ------------------- 渲染 -------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);

            paintBackground(g2);
            paintGrid(g2);

            if (model == null) {
                return;
            }

            AffineTransform old = g2.getTransform();
            g2.translate(offsetX, offsetY);
            g2.scale(scale, scale);

            // 先画边
            for (EdgeView e : model.getEdges()) {
                paintEdge(g2, e);
            }
            // 再画节点（覆盖在边上）
            for (NodeView nv : model.getNodes()) {
                paintNode(g2, nv);
            }

            g2.setTransform(old);
            paintHud(g2);
        } finally {
            g2.dispose();
        }
    }

    private void paintBackground(Graphics2D g2) {
        // 简单的双色对角渐变
        java.awt.GradientPaint gp = new java.awt.GradientPaint(
                0, 0, BG_TOP, getWidth(), getHeight(), BG_BOTTOM);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void paintGrid(Graphics2D g2) {
        // 用细点表示，避免线条干扰阅读
        int spacing = (int) Math.max(8, GRID_SPACING * scale);
        int startX = (int) (offsetX % spacing);
        int startY = (int) (offsetY % spacing);
        g2.setColor(GRID_DOT);
        for (int x = startX; x < getWidth(); x += spacing) {
            for (int y = startY; y < getHeight(); y += spacing) {
                g2.fillRect(x, y, 1, 1);
            }
        }
    }

    private void paintEdge(Graphics2D g2, EdgeView e) {
        NodeView from = idToView.get(e.getFromId());
        NodeView to = idToView.get(e.getToId());
        if (from == null || to == null) {
            return;
        }
        // 自适应锚点：根据 from / to 的相对位置决定从哪一侧进出，让 S 形折返也能漂亮地画出来
        double[] anchors = chooseAnchors(from, to);
        double x1 = anchors[0];
        double y1 = anchors[1];
        double x2 = anchors[2];
        double y2 = anchors[3];
        // 出方向 / 入方向
        double outDirX = anchors[4];
        double outDirY = anchors[5];
        double inDirX = anchors[6];
        double inDirY = anchors[7];

        double dist = Math.hypot(x2 - x1, y2 - y1);
        double handle = Math.max(40, dist * 0.45);
        double cx1 = x1 + outDirX * handle;
        double cy1 = y1 + outDirY * handle;
        double cx2 = x2 + inDirX * handle;
        double cy2 = y2 + inDirY * handle;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x1, y1);
        path.curveTo(cx1, cy1, cx2, cy2, x2, y2);

        boolean running = from.getStatus() == NodeStatus.RUNNING
                || to.getStatus() == NodeStatus.RUNNING;
        Color base = e.isActive() ? EDGE_ACTIVE : EDGE_COLOR;
        if (running) {
            // 发光：先画一层粗的半透明
            float alpha = 0.35f + 0.45f * Math.abs((float) Math.sin(runningPhase * Math.PI * 2));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(EDGE_ACTIVE);
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        g2.setColor(base);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);

        // 末端箭头方向用入方向的反向
        double arrFromX = x2 - inDirX * 16;
        double arrFromY = y2 - inDirY * 16;
        paintArrow(g2, arrFromX, arrFromY, x2, y2, base);
    }

    /**
     * 根据 from/to 的相对位置选择锚点：
     * <ul>
     *   <li>水平对齐（同一行，左右）：右侧出 → 左侧进（默认）</li>
     *   <li>下游在 from 下方且 X 已折返（向左）：从 from 底部出 → to 顶部入（折回连接）</li>
     *   <li>纯下方且 X 接近：from 底 → to 顶</li>
     * </ul>
     *
     * @return [outX, outY, inX, inY, outDirX, outDirY, inDirX, inDirY] 共 8 个值，方向是单位向量。
     */
    private static double[] chooseAnchors(NodeView from, NodeView to) {
        double fromCx = from.getCenterX();
        double fromCy = from.getCenterY();
        double toCx = to.getCenterX();
        double toCy = to.getCenterY();
        double dx = toCx - fromCx;
        double dy = toCy - fromCy;

        // 折返：to 在 from 左侧或正下方 + 大幅下移 -> 从 from 底部出，to 顶部入
        boolean wrapLine = dy > NodeView.HEIGHT && dx <= NodeView.WIDTH * 0.5;
        if (wrapLine) {
            return new double[]{
                    from.getBottomAnchorX(), from.getBottomAnchorY(),
                    to.getTopAnchorX(), to.getTopAnchorY(),
                    0, 1,  // 出方向：向下
                    0, -1  // 入方向：从上往下进
            };
        }
        // 默认水平：右出左入
        return new double[]{
                from.getOutputAnchorX(), from.getOutputAnchorY(),
                to.getInputAnchorX(), to.getInputAnchorY(),
                1, 0,
                -1, 0
        };
    }

    private void paintArrow(Graphics2D g2, double fromX, double fromY,
                            double toX, double toY, Color color) {
        double angle = Math.atan2(toY - fromY, toX - fromX);
        double size = 8.5;
        double a1 = angle - Math.toRadians(28);
        double a2 = angle + Math.toRadians(28);
        Path2D.Double tri = new Path2D.Double();
        tri.moveTo(toX, toY);
        tri.lineTo(toX - size * Math.cos(a1), toY - size * Math.sin(a1));
        tri.lineTo(toX - size * Math.cos(a2), toY - size * Math.sin(a2));
        tri.closePath();
        g2.setColor(color);
        g2.fill(tri);
    }

    private void paintNode(Graphics2D g2, NodeView nv) {
        double x = nv.getX();
        double y = nv.getY();
        int w = NodeView.WIDTH;
        int h = NodeView.HEIGHT;

        // 阴影
        g2.setColor(SHADOW);
        g2.fill(new RoundRectangle2D.Double(
                x + 3, y + 4, w, h, NodeView.CORNER, NodeView.CORNER));

        // 卡片底色
        g2.setColor(CARD_BG);
        g2.fill(new RoundRectangle2D.Double(
                x, y, w, h, NodeView.CORNER, NodeView.CORNER));

        // 头部色条（左侧 6px 圆角）
        Color accent = nv.getKind().getAccent();
        g2.setColor(accent);
        Path2D.Double left = new Path2D.Double();
        left.append(new RoundRectangle2D.Double(x, y, 6, h,
                NodeView.CORNER, NodeView.CORNER), false);
        // 用一个 rect 把右边补齐成竖条
        g2.fill(new RoundRectangle2D.Double(x, y, 6, h, 6, 6));

        // 状态描边
        Color st = nv.statusColor();
        if (st != null) {
            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(st);
            g2.draw(new RoundRectangle2D.Double(
                    x - 0.5, y - 0.5, w + 1, h + 1, NodeView.CORNER, NodeView.CORNER));
        } else {
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(CARD_BORDER);
            g2.draw(new RoundRectangle2D.Double(
                    x, y, w, h, NodeView.CORNER, NodeView.CORNER));
        }

        // 图标背景圆
        int iconBoxX = (int) x + 16;
        int iconBoxY = (int) y + (h - 36) / 2;
        g2.setColor(nv.getKind().getHeaderBg());
        g2.fillRoundRect(iconBoxX, iconBoxY, 36, 36, 10, 10);

        // 图标
        try {
            int ix = iconBoxX + 4;
            int iy = iconBoxY + 4;
            nv.getKind().icon().paintIcon(this, g2, ix, iy);
        } catch (Throwable ignored) {
        }

        // 标题
        g2.setColor(CARD_TITLE);
        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        String title = trimTo(nv.getTitle(), 22);
        g2.drawString(title, iconBoxX + 48, (int) y + 26);

        // 子标题（节点 id / 类型）
        g2.setColor(CARD_SUBTITLE);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g2.drawString(nv.getKind().getLabel() + "  ·  " + trimTo(nv.getId(), 18),
                iconBoxX + 48, (int) y + 44);

        // 状态徽标（右上）
        if (st != null) {
            int badgeX = (int) x + w - 18;
            int badgeY = (int) y + 12;
            g2.setColor(st);
            g2.fillOval(badgeX - 5, badgeY - 5, 10, 10);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawOval(badgeX - 5, badgeY - 5, 10, 10);
        }

        // 输出端口
        g2.setColor(accent);
        g2.fillOval((int) (x + w - 5), (int) (y + h / 2.0 - 5), 10, 10);
        g2.setColor(new Color(0, 0, 0, 90));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval((int) (x + w - 5), (int) (y + h / 2.0 - 5), 10, 10);

        // 输入端口
        g2.setColor(new Color(160, 165, 175));
        g2.fillOval((int) (x - 5), (int) (y + h / 2.0 - 5), 10, 10);
        g2.setColor(new Color(0, 0, 0, 90));
        g2.drawOval((int) (x - 5), (int) (y + h / 2.0 - 5), 10, 10);
    }

    private void paintHud(Graphics2D g2) {
        // 右上角小卡片显示缩放比例 + 简短操作提示
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        String txt = String.format("zoom %.0f%%   ·   wheel=zoom  drag=pan",
                scale * 100);
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int padX = 10;
        int padY = 6;
        int textW = fm.stringWidth(txt);
        int boxW = textW + padX * 2;
        int boxH = fm.getHeight() + padY;
        int boxX = getWidth() - boxW - 12;
        int boxY = 12;
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);
        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawString(txt, boxX + padX,
                boxY + padY / 2 + fm.getAscent());
    }

    // ------------------- 辅助 -------------------

    private NodeView hitTest(int viewX, int viewY) {
        if (model == null) {
            return null;
        }
        // view -> model coords
        double mx = (viewX - offsetX) / scale;
        double my = (viewY - offsetY) / scale;
        // 后画的节点在前；倒序遍历
        java.util.List<NodeView> list = model.getNodes();
        for (int i = list.size() - 1; i >= 0; i--) {
            NodeView nv = list.get(i);
            if (nv.contains(mx, my)) {
                return nv;
            }
        }
        return null;
    }

    private boolean hasRunningNode() {
        if (model == null) {
            return false;
        }
        for (NodeView nv : model.getNodes()) {
            if (nv.getStatus() == NodeStatus.RUNNING) {
                return true;
            }
        }
        return false;
    }

    private static String trimTo(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 1) + "…";
    }

    @SuppressWarnings("unused")
    private static Point2D.Double bezier(double t,
                                         double x1, double y1,
                                         double cx1, double cy1,
                                         double cx2, double cy2,
                                         double x2, double y2) {
        double mt = 1 - t;
        double x = mt * mt * mt * x1 + 3 * mt * mt * t * cx1
                + 3 * mt * t * t * cx2 + t * t * t * x2;
        double y = mt * mt * mt * y1 + 3 * mt * mt * t * cy1
                + 3 * mt * t * t * cy2 + t * t * t * y2;
        return new Point2D.Double(x, y);
    }
}

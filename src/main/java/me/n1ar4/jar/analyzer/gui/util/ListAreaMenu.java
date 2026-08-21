/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 黑白名单输入框右键菜单：恢复默认 / 查看语法规则
 */
public class ListAreaMenu {

    public static void install(JTextArea area, String defaultText, String title, String rulesHtml) {
        area.addMouseListener(new MouseAdapter() {
            private void maybeShow(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                JPopupMenu menu = new JPopupMenu();
                JMenuItem restoreItem = new JMenuItem("恢复默认 (Restore Default)");
                // 引擎启动后列表被锁定，此时不允许恢复默认
                restoreItem.setEnabled(area.isEditable());
                restoreItem.addActionListener(ev -> area.setText(defaultText));
                JMenuItem rulesItem = new JMenuItem("语法规则 (Syntax Rules)");
                rulesItem.addActionListener(ev -> JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(area),
                        rulesHtml, title, JOptionPane.INFORMATION_MESSAGE));
                menu.add(restoreItem);
                menu.addSeparator();
                menu.add(rulesItem);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShow(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShow(e);
            }
        });
    }

    // ---- 各区域的语法规则文案 ------------------------------------------

    /**
     * 通用规则（ListParser 支持）
     */
    private static String commonRules() {
        return "<li>注释：<b>#</b> / <b>//</b> / <b>/*</b> 开头的行会被忽略</li>" +
                "<li>包名条目：<b>com.a.</b>（以点结尾，前缀匹配整个包下所有类）</li>" +
                "<li>类名条目：<b>com.a.Demo</b>（完整类名，精确匹配）</li>" +
                "<li>分隔符：换行或 <b>;</b>（同一行可以写多个条目）</li>" +
                "<li>兼容 <b>com.a.*;</b> 通配写法（等价于 <b>com.a.</b>）</li>";
    }

    private static String wrap(String specific) {
        return "<html><body style='width:420px'><h3>语法规则 (Syntax Rules)</h3><ul>"
                + commonRules() + "</ul>" + specific + "</body></html>";
    }

    /**
     * Starter 页签：类黑名单
     */
    public static String classBlackRules() {
        return wrap("<p><b style='color:#D32F2F'>黑名单</b>：匹配（包前缀或类名精确）的类将被" +
                "<b>排除</b>，不参与分析。黑名单为空则不过滤。</p>");
    }

    /**
     * Starter 页签：类白名单
     */
    public static String classWhiteRules() {
        return wrap("<p><b style='color:#1565C0'>白名单</b>：仅<b>保留</b>匹配（包前缀或类名精确）" +
                "的类。白名单为空或全为注释时不过滤全部保留。</p>" +
                "<p><b style='color:#D32F2F'>注意：白名单写错会导致一个类都分析不到！</b>" +
                "出现该情况时请右键恢复默认后重新分析。</p>");
    }

    /**
     * Search 页签：搜索过滤列表
     */
    public static String searchFilterRules() {
        return wrap("<p>该列表配合下方下拉框的过滤模式使用：</p>" +
                "<p><b style='color:#D32F2F'>黑名单模式</b>：搜索结果中<b>排除</b>匹配项</p>" +
                "<p><b style='color:#1565C0'>白名单模式</b>：搜索结果中仅<b>保留</b>匹配项</p>");
    }

    /**
     * DFS 高级设置：类名黑名单
     */
    public static String dfsBlackRules() {
        return "<html><body style='width:420px'><h3>语法规则 (Syntax Rules)</h3><ul>" +
                "<li>每行一个<b>完整类名</b>：例如 <b>java.lang.Thread</b></li>" +
                "<li>不支持分号分隔与包名前缀（必须是完整类名）</li>" +
                "<li>空行会被忽略</li>" +
                "</ul><p>DFS 分析过程中将<b>忽略</b>这些类（用于排除干扰类）。</p>" +
                "</body></html>";
    }
}

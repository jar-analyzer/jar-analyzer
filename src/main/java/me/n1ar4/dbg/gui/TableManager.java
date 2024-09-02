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

package me.n1ar4.dbg.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class TableManager {
    private static final Set<Integer> highlightedRows = new HashSet<>();
    private static long curCodeIndex = 0;
    private static long jumpLocation = -1;
    private static int jumpRow = -1;

    public static void addHighlight(int row) {
        highlightedRows.add(row);
    }

    public static void addJump(long row) {
        jumpLocation = row;
    }

    public static void addJumpRow(int row) {
        jumpRow = row;
    }

    public static long getJumpLocation() {
        return jumpLocation;
    }

    public static void reset() {
        jumpLocation = -1;
        jumpRow = -1;
    }

    public static void setCur(long index) {
        highlightedRows.clear();
        curCodeIndex = index;
    }

    public static void setBytecodeTable() {
        MainForm instance = MainForm.getInstance();
        JTable bytecodeTable = instance.getBytecodeTable();

        // IGNORE BOUNDARY
        bytecodeTable.setShowGrid(false);
        bytecodeTable.setGridColor(new Color(0, 0, 0, 0));
        bytecodeTable.setIntercellSpacing(new Dimension(0, 0));

        // RENDER ACTION
        Color defaultFontColor = UIManager.getColor("Table.foreground");
        Color lighterRed = new Color(255, 100, 100);
        Color lighterYellow = new Color(225, 202, 130);

        bytecodeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                // DO NOT MODIFY FONT COLOR
                c.setForeground(defaultFontColor);

                boolean highlightRow = false;
                String actual = String.format("%08x", curCodeIndex);
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Object cellValue = table.getValueAt(row, col);
                    if (cellValue != null && cellValue.toString().equals(actual)) {
                        highlightRow = true;
                        break;
                    }
                }

                if (highlightedRows.contains(row)) {
                    c.setBackground(lighterRed);
                } else if (highlightRow) {
                    c.setBackground(Color.CYAN);
                } else if (jumpLocation != -1 && row == jumpRow) {
                    c.setBackground(lighterYellow);
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

        // MOUSE ACTION LISTENER
        bytecodeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = bytecodeTable.rowAtPoint(e.getPoint());
                int col = bytecodeTable.columnAtPoint(e.getPoint());
                if (col == 0) {
                    if (highlightedRows.contains(row)) {
                        highlightedRows.remove(row);
                    } else {
                        highlightedRows.add(row);
                    }
                    bytecodeTable.repaint();
                }
            }
        });

        // IGNORE HEADER RENDER
        bytecodeTable.setTableHeader(null);
    }
}

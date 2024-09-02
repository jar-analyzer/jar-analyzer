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

package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import javax.swing.*;

public class CommonSearchAction {
    public static void run() {
        JRadioButton methodCallR = MainForm.getInstance().getMethodCallRadioButton();
        JRadioButton methodDefR = MainForm.getInstance().getMethodDefinitionRadioButton();
        JRadioButton strContainsR = MainForm.getInstance().getStringContainsRadioButton();
        JRadioButton binaryR = MainForm.getInstance().getBinarySearchRadioButton();
        methodCallR.addActionListener(e -> {
            if (methodCallR.isSelected()) {
                LogUtil.info("select method call search");
                MainForm.getInstance().getSearchClassText().setEnabled(true);
                MainForm.getInstance().getSearchMethodText().setEnabled(true);
                MainForm.getInstance().getSearchStrText().setText(null);
                MainForm.getInstance().getSearchStrText().setEnabled(false);
            }
        });
        methodDefR.addActionListener(e -> {
            if (methodDefR.isSelected()) {
                LogUtil.info("select method def search");
                MainForm.getInstance().getSearchClassText().setEnabled(true);
                MainForm.getInstance().getSearchMethodText().setEnabled(true);
                MainForm.getInstance().getSearchStrText().setText(null);
                MainForm.getInstance().getSearchStrText().setEnabled(false);
            }
        });
        strContainsR.addActionListener(e -> {
            if (strContainsR.isSelected()) {
                LogUtil.info("select string contains search");
                MainForm.getInstance().getSearchClassText().setText(null);
                MainForm.getInstance().getSearchMethodText().setText(null);
                MainForm.getInstance().getSearchClassText().setEnabled(false);
                MainForm.getInstance().getSearchMethodText().setEnabled(false);
                MainForm.getInstance().getSearchStrText().setEnabled(true);
            }
        });
        binaryR.addActionListener(e -> {
            if (binaryR.isSelected()) {
                LogUtil.info("select binary search");
                MainForm.getInstance().getSearchClassText().setText(null);
                MainForm.getInstance().getSearchMethodText().setText(null);
                MainForm.getInstance().getSearchClassText().setEnabled(false);
                MainForm.getInstance().getSearchMethodText().setEnabled(false);
                MainForm.getInstance().getSearchStrText().setEnabled(true);
            }
        });
    }
}

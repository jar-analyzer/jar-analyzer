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

public class TipsAction {
    public static void run() {
        MainForm.getInstance().getStartBuildDatabaseButton().setToolTipText(
                "only one analysis is allowed per startup"
        );

        MainForm.getInstance().getNextBtn().setToolTipText(
                "go to next state"
        );

        MainForm.getInstance().getPrevBtn().setToolTipText(
                "go to previous state"
        );

        MainForm.getInstance().getChoseBtn().setToolTipText(
                "support jar/jar dir/class dir"
        );

        MainForm.getInstance().getCfgBtn().setToolTipText(
                "show method control flow graph"
        );

        MainForm.getInstance().getSimpleFrameButton().setToolTipText(
                "show method jvm frame analyze - simple"
        );

        MainForm.getInstance().getFrameBtn().setToolTipText(
                "show method jvm frame analyze - full"
        );

        MainForm.getInstance().getJavaAsmBtn().setToolTipText(
                "show method java asm code"
        );

        MainForm.getInstance().getOpcodeBtn().setToolTipText(
                "show method opcode analyze"
        );

        MainForm.getInstance().getCleanButton().setToolTipText(
                "clean all cache files"
        );

        MainForm.getInstance().getShowStringListButton().setToolTipText(
                "show all strings"
        );

        MainForm.getInstance().getRefreshButton().setToolTipText(
                "refresh spring analyze"
        );

        MainForm.getInstance().getStartSearchButton().setToolTipText(
                "start search"
        );
    }
}

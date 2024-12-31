/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
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

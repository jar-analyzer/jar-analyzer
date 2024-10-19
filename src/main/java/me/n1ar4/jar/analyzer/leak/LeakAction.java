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

package me.n1ar4.jar.analyzer.leak;

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.LeakResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;

public class LeakAction {
    private static final Logger logger = LogManager.getLogger();

    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null) {
            return;
        }

        JCheckBox codeBox = instance.getLeakCodeBox();
        JCheckBox jwtBox = instance.getLeakJWTBox();
        JCheckBox idCardBox = instance.getLeakIdBox();
        JCheckBox ipAddrBox = instance.getLeakIpBox();
        JCheckBox emailBox = instance.getLeakEmailBox();
        JCheckBox urlBox = instance.getLeakUrlBox();
        JCheckBox jdbcBox = instance.getLeakJdbcBox();
        JCheckBox filePathBox = instance.getLeakFileBox();
        JCheckBox macAddrBox = instance.getLeakMacBox();
        JCheckBox phoneBox = instance.getLeakPhoneBox();

        JCheckBox useCpBox = instance.getLeakCpBox();
        JCheckBox useLdcBox = instance.getLeakLdcBox();
        JCheckBox useBase64Box = instance.getLeakDetBase64Box();

        JList<LeakResult> leakList = instance.getLeakResultList();

        logger.info("registering leak action");
        instance.getLeakStartBtn().addActionListener(e -> {
            CoreEngine engine = MainForm.getEngine();

        });
        instance.getLeakCleanBtn().addActionListener(e -> {
            leakList.setModel(new DefaultListModel<>());
            JOptionPane.showMessageDialog(instance.getMasterPanel(), "clean data finish");
        });
    }
}

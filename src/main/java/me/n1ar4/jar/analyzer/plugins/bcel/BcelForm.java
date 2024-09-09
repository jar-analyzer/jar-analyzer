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

package me.n1ar4.jar.analyzer.plugins.bcel;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import org.apache.bcel.classfile.Utility;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BcelForm {
    private JPanel masterPanel;
    private JScrollPane bcelScroll;
    private JTextArea bcelArea;
    private JPanel opPanel;
    private JButton cleanBtn;
    private JButton checkBtn;
    private JButton decompileBtn;

    private static final String DEFAULT_BCEL = "$$BCEL$$$l$8b$I$A$A$A$A$A$A$A$95Q$c9J$DA$Q$7d" +
            "m$96I$c6hb$W$f7$r$k$84D$d0F$c8$z$e2E$Q$P$83$K$91x$ee$qm$d2$a1g$st$sJ$fc$x$3d$ux$" +
            "f0$D$fc$u$b1z$U$X$f0b$j$aax$efU$bd$ee$ea$7e$7d$7b$7e$B$d0$c0$96$8b4$W$b2$u$a2$94" +
            "A$d9E$F$8b$O$96$i$y3$a4$PU$a0$a2$p$86D$ad$defH$k$87$3d$c9$90$f7T$m$cf$s$7eG$9aK$" +
            "d1$d1$c4$U$bd$b0$xt$5b$Ye$f1$t$99$8c$Gj$cc$c0$3d_$f2$e0$40$98$G$l$K$c3E$m$f4$f4N" +
            "$g$3e$d2$93$be$K$c6$bc$d3$95$9a$9fJ$ad$c3$ab$d0$e8$5e$93$n5$b0$88$c1m$85$T$d3$95" +
            "$t$ca$ba$e5$bf$5b$f6$87$e2F$e4$e0$m$e3$60$r$87U$ac1$94$e3$99$ea$ad$d5$ab$d7$a1$a" +
            "9Z$5b$H$eb9l$60$93a$ef_w$60$u$d8$p$b8$WA$9f$9fw$86$b2$h$fd$a2Z$d3q$q$7dz$95pBB$c" +
            "5$8b$V$V$f2$L$a3$82$a8$V$Z$v$7cZ$a3$f4$H$cd$e0$8c$y$d2$B$cd$d5$bc$l$96$R$d1$fdf$" +
            "bd$8dm$a4$e8$3fl$cc$80$d9$r$vg$Jq$aa$8cjj$f7$J$ec$3e$96$5d$ca$e9$98Lb$96r$ee$a3$" +
            "81$ea$i$d5$y$e6$bf$86w$a8$dbF$f6$B3$c5$c4$p$92$d6$80$c5$Gn$y$a5$a9$d5$n$s$l$h$X$" +
            "de$B$deF$f8Q$j$C$A$A";


    private static void show(JPanel masterPanel) {
        JOptionPane.showMessageDialog(masterPanel, "please enter a valid bcel code");
    }

    private static boolean check(byte[] data) {
        try {
            Path p = Paths.get(Const.tempDir).resolve(Paths.get("test-bcel.class"));
            Files.write(p, data);
            String result = DecompileEngine.decompile(p);
            if (result == null || result.isEmpty()) {
                return false;
            }
            MainForm.getCodeArea().setText(result);
            MainForm.getCodeArea().setCaretPosition(0);
            return true;
        } catch (Exception ig) {
            LogUtil.error(ig.toString());
        }
        return false;
    }

    public BcelForm() {
        bcelArea.setText(DEFAULT_BCEL);
        cleanBtn.addActionListener(e -> bcelArea.setText(""));
        checkBtn.addActionListener(e -> {
            String bcel = bcelArea.getText();
            bcel = bcel.trim();
            if (bcel.isEmpty()) {
                show(masterPanel);
                return;
            }
            if (!bcel.toUpperCase().startsWith("$$BCEL")) {
                show(masterPanel);
                return;
            }
            bcel = bcel.substring(8);
            try {
                Utility.decode(bcel, true);
            } catch (Exception ignoredTrue) {
                try {
                    Utility.decode(bcel, false);
                } catch (Exception ignored) {
                    show(masterPanel);
                    return;
                }
            }
            JOptionPane.showMessageDialog(masterPanel, "pass");
        });
        decompileBtn.addActionListener(e -> {
            String bcel = bcelArea.getText();
            bcel = bcel.trim();
            byte[] data;
            bcel = bcel.substring(8);
            try {
                data = Utility.decode(bcel, true);
                if (check(data)) {
                    return;
                }
                data = Utility.decode(bcel, false);
                if (check(data)) {
                    return;
                }
                LogUtil.warn("cannot decompile the bcel code");
            } catch (Exception ignored) {
            }
        });
    }

    public static void start() {
        JFrame frame = new JFrame(Const.BcelForm);
        frame.setContentPane(new BcelForm().masterPanel);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        masterPanel = new JPanel();
        masterPanel.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        bcelScroll = new JScrollPane();
        masterPanel.add(bcelScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        bcelArea = new JTextArea();
        bcelArea.setColumns(50);
        bcelArea.setLineWrap(true);
        bcelArea.setRows(20);
        bcelScroll.setViewportView(bcelArea);
        opPanel = new JPanel();
        opPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        masterPanel.add(opPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cleanBtn = new JButton();
        cleanBtn.setText("CLEAN");
        opPanel.add(cleanBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBtn = new JButton();
        checkBtn.setText("CHECK");
        opPanel.add(checkBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        decompileBtn = new JButton();
        decompileBtn.setText("DECOMPILE");
        opPanel.add(decompileBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return masterPanel;
    }

}

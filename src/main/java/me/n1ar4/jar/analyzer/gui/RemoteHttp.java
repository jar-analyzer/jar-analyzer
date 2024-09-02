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

package me.n1ar4.jar.analyzer.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.n1ar4.jar.analyzer.gui.action.BuildAction;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import okhttp3.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class RemoteHttp {
    private static final Logger logger = LogManager.getLogger();
    private JPanel remotePanel;
    private JPanel rootPanel;
    private JTextField urlText;
    private JProgressBar progressBar;
    private JButton downBtn;
    private JButton loadBtn;
    private JLabel urlLabel;
    private JPanel opPanel;
    private static RemoteHttp instance;
    private static JFrame globalFrame;
    private static String filename = null;
    private static boolean finish = false;

    public static void start() {
        JFrame frame = new JFrame(Const.RemoteForm);
        instance = new RemoteHttp();
        instance.init();
        frame.setContentPane(instance.rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.setVisible(true);
        frame.setResizable(false);
        globalFrame = frame;
    }

    private void init() {
        instance.progressBar.setValue(0);
        downBtn.addActionListener(e -> new Thread(() -> {
            finish = false;
            OkHttpClient okHttpClient = new OkHttpClient();
            String url = urlText.getText();
            if (url == null || url.trim().isEmpty()) {
                JOptionPane.showMessageDialog(instance.rootPanel, "error url");
                return;
            }
            progressBar.setValue(1);
            progressBar.setValue(2);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Connection", "close")
                    .build();
            progressBar.setValue(3);
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                @SuppressWarnings("all")
                public void onFailure(Call call, IOException ignored) {
                }

                @Override
                @SuppressWarnings("all")
                public void onResponse(Call call, Response response) {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len;
                    FileOutputStream fos = null;
                    try {
                        if (response.body() == null) {
                            return;
                        }
                        is = response.body().byteStream();
                        long total = response.body().contentLength();
                        filename = "temp" + UUID.randomUUID() + ".jar";
                        File file = new File(Const.downDir, filename);
                        try {
                            Files.createDirectories(Paths.get(Const.downDir));
                        } catch (Exception ignored) {
                        }
                        fos = new FileOutputStream(file);
                        progressBar.setValue(4);
                        long sum = 0;
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            sum += len;
                            int progress = (int) (sum * 1.0f / total * 100);
                            if (progress < 4) {
                                progress = 4;
                            }
                            progressBar.setValue(progress);
                        }
                        fos.flush();
                    } catch (Exception ignored) {
                    } finally {
                        try {
                            if (is != null)
                                is.close();
                        } catch (IOException ignored) {
                        }
                        try {
                            if (fos != null)
                                fos.close();
                        } catch (IOException ignored) {
                        }
                        finish = true;
                    }
                }
            });
        }).start());

        loadBtn.addActionListener(e -> {
            if (finish) {
                Path down = Paths.get(Const.downDir);
                try {
                    Files.createDirectory(down);
                } catch (Exception ignored) {
                }
                Path finalPath = down.resolve(Paths.get(filename));
                logger.info("load {}", finalPath.toString());
                BuildAction.start(finalPath.toAbsolutePath().toString());
                globalFrame.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(instance.rootPanel, "download first");
            }
        });
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
        remotePanel = new JPanel();
        remotePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 5), -1, -1));
        remotePanel.add(rootPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        urlLabel = new JLabel();
        urlLabel.setText("HTTP URL");
        rootPanel.add(urlLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        urlText = new JTextField();
        rootPanel.add(urlText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, -1), new Dimension(150, -1), null, 0, false));
        progressBar = new JProgressBar();
        progressBar.setBorderPainted(true);
        progressBar.setStringPainted(true);
        rootPanel.add(progressBar, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        opPanel = new JPanel();
        opPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(opPanel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        downBtn = new JButton();
        downBtn.setText("DOWNLOAD");
        opPanel.add(downBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        loadBtn = new JButton();
        loadBtn.setText("LOAD");
        opPanel.add(loadBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return remotePanel;
    }

}

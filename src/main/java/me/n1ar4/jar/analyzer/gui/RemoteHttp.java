/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui;

import me.n1ar4.jar.analyzer.gui.action.BuildAction;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
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
        initializeComponents();
    }

    private void initializeComponents() {
        remotePanel = new JPanel();
        SwingLayout.configureGrid(remotePanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        rootPanel = new JPanel();
        SwingLayout.configureGrid(rootPanel, 4, 2, new Insets(0, 0, 10, 5), -1, -1);
        SwingLayout.add(remotePanel, rootPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        urlLabel = new JLabel();
        urlLabel.setText("HTTP URL");
        SwingLayout.add(rootPanel, urlLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        final Component spacer1 = Box.createGlue();
        SwingLayout.add(rootPanel, spacer1, 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, false, true, null, null, null, 0);
        urlText = new JTextField();
        SwingLayout.add(rootPanel, urlText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(400, -1), new Dimension(150, -1), null, 0);
        progressBar = new JProgressBar();
        progressBar.setBorderPainted(true);
        progressBar.setStringPainted(true);
        SwingLayout.add(rootPanel, progressBar, 2, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 1);
        opPanel = new JPanel();
        SwingLayout.configureGrid(opPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(rootPanel, opPanel, 1, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        downBtn = new JButton();
        downBtn.setText("DOWNLOAD");
        SwingLayout.add(opPanel, downBtn, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 1);
        loadBtn = new JButton();
        loadBtn.setText("LOAD");
        SwingLayout.add(opPanel, loadBtn, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return remotePanel;
    }

}

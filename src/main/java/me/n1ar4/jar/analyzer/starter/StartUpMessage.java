package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.IconManager;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class StartUpMessage {
    private static final Logger logger = LogManager.getLogger();

    public static void run() {
        // 异步加载 MainForm 组件
        logger.info("async load main form instance");
        final ArrayList<JFrame> frameList = new ArrayList<>();
        new Thread(() -> frameList.add(MainForm.start())).start();

        JWindow splashWindow = new JWindow();
        JLabel splashLabel = new JLabel(IconManager.startUpIcon);
        splashWindow.getContentPane().add(splashLabel, BorderLayout.CENTER);
        splashWindow.setSize(859, 560);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setForeground(new Color(109, 168, 253));
        progressBar.setStringPainted(true);
        splashWindow.getContentPane().add(progressBar, BorderLayout.SOUTH);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - splashWindow.getSize().width) / 2;
        int y = (screenSize.height - splashWindow.getSize().height) / 2;
        splashWindow.setLocation(x, y);
        splashWindow.setVisible(true);

        Timer progressBarTimer = new Timer(25, null);
        progressBarTimer.addActionListener(e -> {
            int value = progressBar.getValue();
            value += 1;
            progressBar.setValue(value);
            if (value >= 100) {
                progressBarTimer.stop();
            }
        });
        progressBarTimer.start();

        Timer fadeInTimer = new Timer(50, null);
        fadeInTimer.addActionListener(e -> {
            float opacity = splashWindow.getOpacity();
            opacity += 0.08f;
            if (opacity >= 1.0f) {
                opacity = 1.0f;
                fadeInTimer.stop();
            }
            splashWindow.setOpacity(opacity);
        });
        splashWindow.setOpacity(0.0f);
        fadeInTimer.start();

        logger.info("wait for startup");
        try {
            Thread.sleep(2500);
        } catch (InterruptedException ignored) {
        }

        Timer fadeOutTimer = new Timer(50, null);
        fadeOutTimer.addActionListener(e -> {
            float opacity = splashWindow.getOpacity();
            opacity -= 0.08f;
            // 动画渲染完成
            if (opacity <= 0.0f) {
                fadeOutTimer.stop();
                splashWindow.setVisible(false);
                splashWindow.dispose();
                SwingUtilities.invokeLater(new Thread(() -> {
                    if (frameList.size() != 1) {
                        logger.error("main form frame init error");
                        return;
                    }
                    JFrame frame = frameList.get(0);
                    logger.info("set main form frame visible");
                    frame.setVisible(true);
                }));
            } else {
                splashWindow.setOpacity(opacity);
            }
        });
        fadeOutTimer.start();
    }
}

package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.IconManager;

import javax.swing.*;
import java.awt.*;

public class StartUpMessage {
    public static void run() {
        JWindow splashWindow = new JWindow();
        JLabel splashLabel = new JLabel(IconManager.startUpIcon);
        splashWindow.getContentPane().add(splashLabel);
        splashWindow.setSize(859, 560);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - splashWindow.getSize().width) / 2;
        int y = (screenSize.height - splashWindow.getSize().height) / 2;
        splashWindow.setLocation(x, y);
        splashWindow.setVisible(true);
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
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        Timer fadeOutTimer = new Timer(50, null);
        fadeOutTimer.addActionListener(e -> {
            float opacity = splashWindow.getOpacity();
            opacity -= 0.08f;
            if (opacity <= 0.0f) {
                fadeOutTimer.stop();
                splashWindow.setVisible(false);
                splashWindow.dispose();
                SwingUtilities.invokeLater(MainForm::start);
            } else {
                splashWindow.setOpacity(opacity);
            }
        });
        fadeOutTimer.start();
    }
}

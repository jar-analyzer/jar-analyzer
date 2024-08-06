package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.cli.StartCmd;
import me.n1ar4.jar.analyzer.gui.util.JarAnalyzerLaf;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;

public class ThemeHelper {
    private static final Logger logger = LogManager.getLogger();

    static void process(StartCmd startCmd) {
        // default|metal|win|win-classic|motif|mac|gtk|cross|aqua|nimbus
        String theme = startCmd.getTheme();
        String lookAndFeel;
        if (theme == null || theme.trim().isEmpty()) {
            // SET LOOK AND FEEL
            if (JarAnalyzerLaf.setup()) {
                logger.info("setup look and feel success");
            }
        } else {
            try {
                switch (theme) {
                    case "":
                    case "default":
                        // SET LOOK AND FEEL
                        if (JarAnalyzerLaf.setup()) {
                            logger.info("setup look and feel success");
                        }
                        break;
                    case "metal":
                        lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    case "win":
                        lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    case "win-classic":
                        lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    case "motif":
                        lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    case "mac":
                        lookAndFeel = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    case "gtk":
                        lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    case "cross":
                        lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    case "aqua":
                        lookAndFeel = "com.apple.laf.AquaLookAndFeel";
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    case "nimbus":
                        lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
                        UIManager.setLookAndFeel(lookAndFeel);
                        break;
                    default:
                        logger.warn("error theme name");
                        logger.info("set default look and feel");
                        // SET LOOK AND FEEL
                        if (JarAnalyzerLaf.setup()) {
                            logger.info("setup look and feel success");
                        }
                        break;
                }
            } catch (Exception ignored) {
                logger.warn("load theme error");
                logger.info("set default look and feel");
                // SET LOOK AND FEEL
                if (JarAnalyzerLaf.setup()) {
                    logger.info("setup look and feel success");
                }
            }
        }
    }
}

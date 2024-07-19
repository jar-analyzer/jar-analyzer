package me.n1ar4.jar.analyzer.starter;

import com.beust.jcommander.JCommander;
import me.n1ar4.jar.analyzer.cli.BuildCmd;
import me.n1ar4.jar.analyzer.cli.Client;
import me.n1ar4.jar.analyzer.cli.SearcherCmd;
import me.n1ar4.jar.analyzer.cli.StartCmd;
import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.JarAnalyzerLaf;
import me.n1ar4.jar.analyzer.server.HttpServer;
import me.n1ar4.jar.analyzer.utils.ConsoleUtils;
import me.n1ar4.jar.analyzer.utils.JNIUtil;
import me.n1ar4.jar.analyzer.utils.OSUtil;
import me.n1ar4.log.LogLevel;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.log.LoggingStream;

import javax.swing.*;


public class Application {
    private static final Logger logger = LogManager.getLogger();
    @SuppressWarnings("all")
    private static final BuildCmd buildCmd = new BuildCmd();
    @SuppressWarnings("all")
    private static final StartCmd startCmd = new StartCmd();
    @SuppressWarnings("all")
    private static final SearcherCmd searcherCmd = new SearcherCmd();

    /**
     * Main Method
     * 　　 へ　　　　　／|
     * 　　/＼7　　　 ∠＿/
     * 　 /　│　　 ／　／
     * 　│　Z ＿,＜　／　　 /`ヽ
     * 　│　　　　　ヽ　　 /　　〉
     * 　 Y　　　　　`　 /　　/
     * 　?●　?　●　　??〈　　/
     * 　()　 へ　　　　|　＼〈
     * 　　>? ?_　 ィ　 │ ／／
     * 　 / へ　　 /　?＜| ＼＼
     * 　 ヽ_?　　(_／　 │／／
     * 　　7　　　　　　　|／
     * 　　＞―r￣￣~∠--|
     */
    public static void main(String[] args) {
        // CHECK WINDOWS
        if (OSUtil.isWindows()) {
            boolean ok = JNIUtil.extractDllSo("console.dll", null, true);
            if (ok) {
                try {
                    ConsoleUtils.setWindowsColorSupport();
                } catch (Exception ignored) {
                }
            }
        }

        // SET LOG LEVEL
        LogManager.setLevel(LogLevel.INFO);

        // PRINT LOGO
        Logo.print();

        // VERSION CHECK
        Version.check();

        JCommander commander = JCommander.newBuilder()
                .addCommand(BuildCmd.CMD, buildCmd)
                .addCommand(StartCmd.CMD, startCmd)
                .addCommand(SearcherCmd.CMD, searcherCmd)
                .build();

        try {
            commander.parse(args);
        } catch (Exception ignored) {
            commander.usage();
            return;
        }

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

        Client.run(commander, buildCmd);
        // RUN GUI
        try {
            // CHECK SINGLE INSTANCE
            if (!Single.canRun()) {
                System.exit(0);
            }
            // REDIRECT SYSTEM OUT
            System.setOut(new LoggingStream(System.out, logger));
            System.out.println("set y4-log io-streams");
            System.setErr(new LoggingStream(System.err, logger));
            System.err.println("set y4-log err-streams");

            int port = startCmd.getPort();
            if (port < 1 || port > 65535) {
                port = 10032;
            }
            GlobalOptions.setServerPort(port);
            logger.info("set server port {}", port);
            // START HTTP SERVER
            new Thread(HttpServer::start).start();

            // START GUI
            MainForm.start();
        } catch (Exception ex) {
            logger.error("start jar analyzer error: {}", ex.toString());
        }
    }
}

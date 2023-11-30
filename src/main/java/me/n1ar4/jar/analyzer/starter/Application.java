package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.JarAnalyzerLaf;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.log.LoggingStream;

public class Application {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        Logo.print();
        try {
            if (JarAnalyzerLaf.setup()) {
                logger.info("setup look and feel success");
            }

            if (!Single.canRun()) {
                System.exit(0);
            }

            System.setOut(new LoggingStream(System.out, logger));
            System.out.println("set log42j io-streams");

            MainForm.start();
        } catch (Exception ex) {
            logger.error("start jar analyzer error: {}", ex.toString());
        }
    }
}

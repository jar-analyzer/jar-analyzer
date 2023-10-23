package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.gui.util.JarAnalyzerLaf;
import me.n1ar4.jar.analyzer.gui.MainForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        Logo.print();
        try {
            if (JarAnalyzerLaf.setup()) {
                logger.info("setup look and feel success");
            }
            MainForm.start();
        } catch (Exception ex) {
            logger.error("start jar analyzer error: {}", ex.toString());
        }
    }
}

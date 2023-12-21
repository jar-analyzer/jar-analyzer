package me.n1ar4.jar.analyzer.starter;

import com.beust.jcommander.JCommander;
import me.n1ar4.jar.analyzer.cli.BuildCmd;
import me.n1ar4.jar.analyzer.cli.Client;
import me.n1ar4.jar.analyzer.cli.StartCmd;
import me.n1ar4.jar.analyzer.cli.Y4LangCmd;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.JarAnalyzerLaf;
import me.n1ar4.log.LogLevel;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.log.LoggingStream;


public class Application {
    private static final Logger logger = LogManager.getLogger();
    private static final BuildCmd buildCmd = new BuildCmd();
    private static final StartCmd startCmd = new StartCmd();
    private static final Y4LangCmd y4langCmd = new Y4LangCmd();

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
        // SET LOG LEVEL
        LogManager.setLevel(LogLevel.INFO);
        // PRINT LOGO
        Logo.print();

        JCommander commander = JCommander.newBuilder()
                .addCommand("build", buildCmd)
                .addCommand("gui", startCmd)
                .addCommand("y4lang", y4langCmd)
                .build();
        try {
            commander.parse(args);
        } catch (Exception ignored) {
            commander.usage();
            return;
        }
        Client.run(commander, buildCmd, y4langCmd);
        // RUN GUI
        try {
            // SET LOOK AND FEEL
            if (JarAnalyzerLaf.setup()) {
                logger.info("setup look and feel success");
            }
            // CHECK SINGLE INSTANCE
            if (!Single.canRun()) {
                System.exit(0);
            }
            // REDIRECT SYSTEM OUT
            System.setOut(new LoggingStream(System.out, logger));
            System.out.println("set log42j io-streams");
            System.setErr(new LoggingStream(System.err, logger));
            System.err.println("set log4j err-streams");
            // START GUI
            MainForm.start();
        } catch (Exception ex) {
            logger.error("start jar analyzer error: {}", ex.toString());
        }
    }
}

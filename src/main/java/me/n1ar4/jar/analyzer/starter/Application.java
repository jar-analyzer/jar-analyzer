package me.n1ar4.jar.analyzer.starter;

import com.beust.jcommander.JCommander;
import me.n1ar4.jar.analyzer.cli.BuildCmd;
import me.n1ar4.jar.analyzer.cli.Client;
import me.n1ar4.jar.analyzer.cli.StartCmd;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.JarAnalyzerLaf;
import me.n1ar4.jar.analyzer.utils.ConsoleUtils;
import me.n1ar4.jar.analyzer.utils.JNIUtil;
import me.n1ar4.jar.analyzer.utils.OSUtil;
import me.n1ar4.log.LogLevel;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.log.LoggingStream;


public class Application {
    private static final Logger logger = LogManager.getLogger();
    private static final BuildCmd buildCmd = new BuildCmd();
    private static final StartCmd startCmd = new StartCmd();

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
        if (args.length > 0 && args[0].trim().equals("class-searcher")) {
            ClassSearcher.start(args);
            return;
        }
        // SET LOG LEVEL
        LogManager.setLevel(LogLevel.INFO);
        // PRINT LOGO
        Logo.print();

        // CHECK WINDOWS
        if (OSUtil.isWindows()) {
            JNIUtil.extractDllSo("console.dll", null, true);
            ConsoleUtils.setWindowsColorSupport();
            logger.info("check windows console finish");
        }

        JCommander commander = JCommander.newBuilder()
                .addCommand("build", buildCmd)
                .addCommand("gui", startCmd)
                .build();
        try {
            commander.parse(args);
        } catch (Exception ignored) {
            commander.usage();
            return;
        }
        Client.run(commander, buildCmd);
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
            System.out.println("set y4-log io-streams");
            System.setErr(new LoggingStream(System.err, logger));
            System.err.println("set y4-log err-streams");
            // START GUI
            MainForm.start();
        } catch (Exception ex) {
            logger.error("start jar analyzer error: {}", ex.toString());
        }
    }
}

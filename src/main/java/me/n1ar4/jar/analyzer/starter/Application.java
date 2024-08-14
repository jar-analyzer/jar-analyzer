package me.n1ar4.jar.analyzer.starter;

import com.beust.jcommander.JCommander;
import me.n1ar4.jar.analyzer.cli.BuildCmd;
import me.n1ar4.jar.analyzer.cli.Client;
import me.n1ar4.jar.analyzer.cli.StartCmd;
import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.jar.analyzer.server.HttpServer;
import me.n1ar4.jar.analyzer.utils.ConsoleUtils;
import me.n1ar4.jar.analyzer.utils.JNIUtil;
import me.n1ar4.jar.analyzer.utils.OSUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogLevel;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.log.LoggingStream;
import me.n1ar4.security.Security;

public class Application {
    private static final Logger logger = LogManager.getLogger();
    @SuppressWarnings("all")
    private static final BuildCmd buildCmd = new BuildCmd();
    @SuppressWarnings("all")
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
        // SET SECURITY MANAGER
        Security.setSecurityManager();
        // SET OBJECT INPUT FILTER
        Security.setObjectInputFilter();

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

        // PRINT LOGO
        Logo.print();

        // VERSION CHECK
        Version.check();

        JCommander commander = JCommander.newBuilder()
                .addCommand(BuildCmd.CMD, buildCmd)
                .addCommand(StartCmd.CMD, startCmd)
                .build();

        try {
            commander.parse(args);
        } catch (Exception ignored) {
            commander.usage();
            return;
        }

        // SET LOG LEVEL (debug|info|warn|error)
        LogLevel logLevel;
        String logLevelStr = startCmd.getLogLevel();
        if (logLevelStr == null || StringUtil.isNull(logLevelStr)) {
            System.out.println("[-] UNKNOWN LOG LEVEL USE INFO LEVEL BY DEFAULT");
            logLevel = LogLevel.INFO;
        } else {
            switch (logLevelStr) {
                case "debug":
                    logLevel = LogLevel.DEBUG;
                    break;
                case "info":
                    logLevel = LogLevel.INFO;
                    break;
                case "warn":
                    logLevel = LogLevel.WARN;
                    break;
                case "error":
                    logLevel = LogLevel.ERROR;
                    break;
                default:
                    logLevel = LogLevel.INFO;
                    System.out.println("[-] UNKNOWN LOG LEVEL USE INFO LEVEL BY DEFAULT");
                    break;
            }
        }
        LogManager.setLevel(logLevel);

        // THEME PROCESS
        ThemeHelper.process(startCmd);

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

            // SET AWT EVENT EXCEPTION
            Thread.setDefaultUncaughtExceptionHandler(new ExpHandler());

            StartUpMessage.run();
        } catch (Exception ex) {
            logger.error("start jar analyzer error: {}", ex.toString());
        }
    }
}

package me.n1ar4.jar.analyzer.cli;

import com.beust.jcommander.JCommander;
import me.n1ar4.jar.analyzer.core.CoreRunner;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client {
    private static final Logger logger = LogManager.getLogger();

    public static void run(JCommander commander, BuildCmd buildCmd, Y4LangCmd y4langCmd) {
        String cmd = commander.getParsedCommand();
        if (cmd == null || cmd.trim().isEmpty()) {
            commander.usage();
            System.exit(-1);
        }
        if (cmd.equals("build")) {
            logger.info("use build command");
            if (buildCmd.getJar() == null || buildCmd.getJar().isEmpty()) {
                logger.error("need --jar file");
                commander.usage();
                System.exit(-1);
            }
            String jarPath = buildCmd.getJar();
            Path jarPathPath = Paths.get(jarPath);
            if (!Files.exists(jarPathPath)) {
                logger.error("jar file not exist");
                commander.usage();
                System.exit(-1);
            }
            if (buildCmd.delCache()) {
                logger.info("delete cache files");
                try {
                    DirUtil.removeDir(new File(Const.tempDir));
                } catch (Exception ignored) {
                    logger.warn("delete cache files fail");
                }
            }
            if (buildCmd.delExist()) {
                logger.info("delete old db");
                try {
                    Files.delete(Paths.get(Const.dbFile));
                } catch (Exception ignored) {
                    logger.warn("delete old db fail");
                }
            }
            CoreRunner.run(jarPathPath, null, false);
            logger.info("write file to: {}", Const.dbFile);
            System.exit(0);
        } else if (cmd.equals("gui")) {
            logger.info("run jar-analyzer gui");
        } else if (cmd.equals("y4lang")) {
            String file = y4langCmd.getFile();
            String[] args = new String[]{file};
            logger.info("run y4lang script");
            System.exit(0);
        } else {
            throw new RuntimeException("invalid params");
        }
    }
}

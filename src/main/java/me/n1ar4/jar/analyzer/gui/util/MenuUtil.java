package me.n1ar4.jar.analyzer.gui.util;

import com.github.rjeschke.txtmark.Processor;
import me.n1ar4.games.flappy.FBMainFrame;
import me.n1ar4.games.plane.Game;
import me.n1ar4.games.pocker.Main;
import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.gui.*;
import me.n1ar4.jar.analyzer.http.HttpResponse;
import me.n1ar4.jar.analyzer.http.Y4Client;
import me.n1ar4.jar.analyzer.os.SystemChart;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.shell.analyzer.form.ShellForm;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class MenuUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final JCheckBoxMenuItem showInnerConfig = new JCheckBoxMenuItem("show inner class");
    private static final JCheckBoxMenuItem fixClassPathConfig = new JCheckBoxMenuItem("fix class path");
    private static final JCheckBoxMenuItem sortedByMethodConfig = new JCheckBoxMenuItem("sort results by method name");
    private static final JCheckBoxMenuItem sortedByClassConfig = new JCheckBoxMenuItem("sort results by class name");
    private static final JCheckBoxMenuItem logAllSqlConfig = new JCheckBoxMenuItem("save all sql statement");
    private static final JCheckBoxMenuItem chineseConfig = new JCheckBoxMenuItem("Chinese");
    private static final JCheckBoxMenuItem englishConfig = new JCheckBoxMenuItem("English");

    public static void setLangFlag() {
        if (GlobalOptions.getLang() == GlobalOptions.CHINESE) {
            chineseConfig.setState(true);
        } else if (GlobalOptions.getLang() == GlobalOptions.ENGLISH) {
            englishConfig.setState(true);
        }
    }

    static {
        showInnerConfig.setState(false);
        fixClassPathConfig.setState(false);
        sortedByMethodConfig.setState(false);
        sortedByClassConfig.setState(true);
        logAllSqlConfig.setSelected(true);

        chineseConfig.addActionListener(e -> {
            chineseConfig.setState(chineseConfig.getState());
            englishConfig.setState(!chineseConfig.getState());
            if (chineseConfig.getState()) {
                logger.info("use chinese language");
                GlobalOptions.setLang(GlobalOptions.CHINESE);
                MainForm.refreshLang(true);
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "已切换到中文");
                ConfigFile cf = MainForm.getConfig();
                if (cf == null) {
                    return;
                }
                cf.setLang("zh");
                MainForm.setConfig(cf);
                ConfigEngine.saveConfig(cf);
            }
        });

        englishConfig.addActionListener(e -> {
            englishConfig.setState(englishConfig.getState());
            chineseConfig.setState(!englishConfig.getState());
            if (englishConfig.getState()) {
                logger.info("use english language");
                GlobalOptions.setLang(GlobalOptions.ENGLISH);
                MainForm.refreshLang(true);
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "use english language");
                ConfigFile cf = MainForm.getConfig();
                if (cf == null) {
                    return;
                }
                cf.setLang("en");
                MainForm.setConfig(cf);
                ConfigEngine.saveConfig(cf);
            }
        });

        sortedByMethodConfig.addActionListener(e -> {
            sortedByMethodConfig.setState(sortedByMethodConfig.getState());
            sortedByClassConfig.setState(!sortedByMethodConfig.getState());
        });

        sortedByClassConfig.addActionListener(e -> {
            sortedByClassConfig.setState(sortedByClassConfig.getState());
            sortedByMethodConfig.setState(!sortedByClassConfig.getState());
        });
    }

    public static JCheckBoxMenuItem getShowInnerConfig() {
        return showInnerConfig;
    }

    public static JCheckBoxMenuItem getFixClassPathConfig() {
        return fixClassPathConfig;
    }

    public static JCheckBoxMenuItem getLogAllSqlConfig() {
        return logAllSqlConfig;
    }

    public static boolean sortedByMethod() {
        return sortedByMethodConfig.getState();
    }

    public static boolean sortedByClass() {
        return sortedByClassConfig.getState();
    }

    public static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createAboutMenu());
        menuBar.add(createConfigMenu());
        menuBar.add(language());
        menuBar.add(loadRemote());
        menuBar.add(exportJava());
        menuBar.add(createGames());
        JMenu system = new JMenu("system info");
        JMenuItem systemItem = new JMenuItem("open");
        systemItem.addActionListener(e -> SystemChart.start0());
        system.add(systemItem);
        menuBar.add(system);
        return menuBar;
    }

    private static JMenu exportJava() {
        JMenu export = new JMenu("export");
        JMenuItem proxyItem = new JMenuItem("decompile and export");
        proxyItem.setIcon(IconManager.javaIcon);
        proxyItem.addActionListener(e -> {
            if (MainForm.getEngine() == null) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "PLEASE LOAD JAR FIRST");
                return;
            }
            ExportForm.start();
        });
        export.add(proxyItem);
        return export;
    }

    private static JMenu loadRemote() {
        JMenu loadRemote = new JMenu("remote");
        JMenuItem loadByHttp = new JMenuItem("load jars (http)");
        loadByHttp.setIcon(IconManager.javaIcon);
        loadByHttp.addActionListener(e -> RemoteHttp.start());
        loadRemote.add(loadByHttp);
        JMenuItem start = new JMenuItem("start tomcat analyzer");
        start.setIcon(IconManager.javaIcon);
        start.addActionListener(e -> ShellForm.start0());
        loadRemote.add(start);
        JMenuItem dbgItem = new JMenuItem("open bytecode debugger");
        dbgItem.setIcon(IconManager.javaIcon);
        dbgItem.addActionListener(e -> me.n1ar4.dbg.gui.MainForm.start());
        loadRemote.add(dbgItem);
        JMenuItem proxyItem = new JMenuItem("open proxy config");
        proxyItem.setIcon(IconManager.javaIcon);
        proxyItem.addActionListener(e -> ProxyForm.start());
        loadRemote.add(proxyItem);
        return loadRemote;
    }

    private static JMenu createGames() {
        try {
            JMenu gameMenu = new JMenu("games");
            JMenuItem flappyItem = new JMenuItem("Flappy Bird");
            InputStream is = MainForm.class.getClassLoader().getResourceAsStream(
                    "game/flappy/flappy_bird/bird1_0.png");
            if (is == null) {
                return null;
            }
            ImageIcon flappyIcon = new ImageIcon(ImageIO.read(is));
            flappyItem.setIcon(flappyIcon);
            flappyItem.addActionListener(e -> new FBMainFrame().startGame());
            JMenuItem pokerItem = new JMenuItem("斗地主");
            is = MainForm.class.getClassLoader().getResourceAsStream(
                    "game/pocker/images/logo.png");
            if (is == null) {
                return null;
            }
            ImageIcon pokerIcon = new ImageIcon(ImageIO.read(is));
            pokerItem.setIcon(pokerIcon);
            pokerItem.addActionListener(e -> new Thread(Main::new).start());

            JMenuItem planeItem = new JMenuItem("雷电");
            is = MainForm.class.getClassLoader().getResourceAsStream(
                    "game/plane/logo.png");
            if (is == null) {
                return null;
            }
            ImageIcon planeIcon = new ImageIcon(ImageIO.read(is));
            planeItem.setIcon(planeIcon);
            planeItem.addActionListener(e -> Game.start());

            gameMenu.add(flappyItem);
            gameMenu.add(pokerItem);
            gameMenu.add(planeItem);
            return gameMenu;
        } catch (Exception ex) {
            logger.error("error: {}", ex.toString());
        }
        return null;
    }

    private static JMenu language() {
        try {
            JMenu configMenu = new JMenu("language");
            configMenu.add(chineseConfig);
            configMenu.add(englishConfig);
            return configMenu;
        } catch (Exception ex) {
            logger.error("error: {}", ex.toString());
        }
        return null;
    }

    private static JMenu createConfigMenu() {
        try {
            JMenu configMenu = new JMenu("config");
            configMenu.add(showInnerConfig);
            configMenu.add(fixClassPathConfig);
            configMenu.add(sortedByMethodConfig);
            configMenu.add(sortedByClassConfig);
            configMenu.add(logAllSqlConfig);
            JMenuItem partitionConfig = new JMenuItem("partition config");
            partitionConfig.setIcon(IconManager.javaIcon);
            partitionConfig.addActionListener(e -> PartForm.start());
            configMenu.add(partitionConfig);
            return configMenu;
        } catch (Exception ex) {
            logger.error("error: {}", ex.toString());
        }
        return null;
    }

    private static JMenu createAboutMenu() {
        try {
            JMenu aboutMenu = new JMenu("help");
            JMenuItem bugItem = new JMenuItem("report bug");
            InputStream is = MainForm.class.getClassLoader().getResourceAsStream("img/issue.png");
            if (is == null) {
                return null;
            }
            ImageIcon imageIcon = new ImageIcon(ImageIO.read(is));
            bugItem.setIcon(imageIcon);
            aboutMenu.add(bugItem);
            bugItem.addActionListener(e -> {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI oURL = new URI(Const.newIssueUrl);
                    desktop.browse(oURL);
                } catch (Exception ex) {
                    logger.error("error: {}", ex.toString());
                }
            });

            JMenuItem authorItem = new JMenuItem("project");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/address.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            authorItem.setIcon(imageIcon);
            aboutMenu.add(authorItem);
            authorItem.addActionListener(e -> {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI oURL = new URI(Const.projectUrl);
                    desktop.browse(oURL);
                } catch (Exception ex) {
                    logger.error("error: {}", ex.toString());
                }
            });
            JMenuItem jarItem = new JMenuItem("version: " + Const.version);
            is = MainForm.class.getClassLoader().getResourceAsStream("img/ver.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            jarItem.setIcon(imageIcon);
            aboutMenu.add(jarItem);
            JMenuItem updateItem = new JMenuItem("changelogs");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/update.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            updateItem.setIcon(imageIcon);
            updateItem.addActionListener(e -> {
                try {
                    InputStream i = MenuUtil.class.getClassLoader().getResourceAsStream("CHANGELOG.MD");
                    if (i == null) {
                        return;
                    }
                    int bufferSize = 1024;
                    char[] buffer = new char[bufferSize];
                    StringBuilder out = new StringBuilder();
                    Reader in = new InputStreamReader(i, StandardCharsets.UTF_8);
                    for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                        out.append(buffer, 0, numRead);
                    }
                    ChangeLogForm.start(Processor.process(out.toString()));
                } catch (Exception ex) {
                    logger.error("error: {}", ex.toString());
                }
            });
            aboutMenu.add(updateItem);
            JMenuItem downItem = new JMenuItem("check update");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/normal.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            downItem.setIcon(imageIcon);
            downItem.addActionListener(e -> {
                new Thread(() -> {
                    HttpResponse resp = Y4Client.INSTANCE.get(Const.checkUpdateUrl);
                    String body = new String(resp.getBody());
                    if (body.isEmpty()) {
                        return;
                    }
                    String ver = body.trim();
                    LogUtil.info("latest: " + ver);
                    String output;
                    output = String.format("%s: %s\n%s: %s",
                            "Current Version", Const.version,
                            "Latest Version", ver);
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(), output);
                }).start();
            });
            aboutMenu.add(downItem);
            return aboutMenu;
        } catch (Exception ex) {
            return null;
        }
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.gui.util;

import com.github.rjeschke.txtmark.Processor;
import me.n1ar4.games.flappy.FBMainFrame;
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
import java.io.IOException;
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
    private static final JCheckBoxMenuItem enableFixMethodImplConfig = new JCheckBoxMenuItem(
            "enable fix methods impl/override");
    private static final JCheckBoxMenuItem disableFixMethodImplConfig = new JCheckBoxMenuItem(
            "disable fix methods impl/override");

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
        enableFixMethodImplConfig.setSelected(true);

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

        enableFixMethodImplConfig.addActionListener(e -> {
            enableFixMethodImplConfig.setState(enableFixMethodImplConfig.getState());
            disableFixMethodImplConfig.setState(!enableFixMethodImplConfig.getState());
        });

        disableFixMethodImplConfig.addActionListener(e -> {
            disableFixMethodImplConfig.setState(disableFixMethodImplConfig.getState());
            enableFixMethodImplConfig.setState(!disableFixMethodImplConfig.getState());
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

    public static boolean enableFixMethodImpl() {
        return enableFixMethodImplConfig.getState();
    }

    public static boolean disableFixMethodImpl() {
        return disableFixMethodImplConfig.getState();
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
        systemItem.setIcon(IconManager.javaIcon);
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

            gameMenu.add(flappyItem);
            gameMenu.add(pokerItem);
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
            configMenu.add(enableFixMethodImplConfig);
            configMenu.add(disableFixMethodImplConfig);
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

    @SuppressWarnings("all")
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

            JMenuItem projectItem = new JMenuItem("project");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/address.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            projectItem.setIcon(imageIcon);
            aboutMenu.add(projectItem);
            projectItem.addActionListener(e -> {
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
            JMenuItem changelogItem = new JMenuItem("changelogs");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/update.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            changelogItem.setIcon(imageIcon);
            changelogItem.addActionListener(e -> {
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
                    ChangeLogForm.start(Const.ChangeLogForm, Processor.process(out.toString()));
                } catch (Exception ex) {
                    logger.error("error: {}", ex.toString());
                }
            });
            aboutMenu.add(changelogItem);
            JMenuItem thanksItem = new JMenuItem("thanks");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/github.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            thanksItem.setIcon(imageIcon);
            thanksItem.addActionListener(e -> {
                try {
                    InputStream i = MenuUtil.class.getClassLoader().getResourceAsStream("thanks.md");
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
                    ChangeLogForm.start("THANKS", Processor.process(out.toString()));
                } catch (Exception ex) {
                    logger.error("error: {}", ex.toString());
                }
            });
            aboutMenu.add(thanksItem);
            JMenuItem checkUpdateItem = new JMenuItem("check update");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/normal.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            checkUpdateItem.setIcon(imageIcon);
            checkUpdateItem.addActionListener(e -> new Thread(() -> {
                logger.info("check update from aliyun oss");
                HttpResponse resp = Y4Client.INSTANCE.get(Const.checkUpdateUrl);
                String body = new String(resp.getBody());
                if (body.isEmpty()) {
                    return;
                }
                String ver = body.trim();
                LogUtil.info("latest: " + ver);
                String output;
                output = String.format("<html>" +
                                "<p>本项目是免费开源软件，不存在任何商业版本/收费版本</p>" +
                                "<p>This project is free and open-source software</p>" +
                                "<p>There are no commercial or paid versions</p>" +
                                "<p>%s: %s</p>" +
                                "<p>%s: %s</p>" +
                                "</html>",
                        "当前版本 / Current Version", Const.version,
                        "最新版本 / Latest Version", ver);
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(), output);
            }).start());
            aboutMenu.add(checkUpdateItem);
            JMenuItem aboutItem = new JMenuItem("about");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/java.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            aboutItem.setIcon(imageIcon);
            aboutItem.addActionListener(e -> new Thread(() -> {
                InputStream aboutIs = MainForm.class.getClassLoader().getResourceAsStream("img/about.png");
                if (aboutIs != null) {
                    try {
                        ImageIcon aboutIcon = new ImageIcon(ImageIO.read(aboutIs));
                        JFrame aboutFrame = new JFrame(String.format("about - jar-analyzer v%s @ 4ra1n", Const.version));
                        aboutFrame.setResizable(false);
                        aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        aboutFrame.setSize(450, 530);
                        aboutFrame.setLayout(new BorderLayout());
                        JLabel imageLabel = new JLabel(aboutIcon);
                        aboutFrame.add(imageLabel, BorderLayout.NORTH);
                        JLabel infoLabel = new JLabel("jar-analyzer project @ 4ra1n", JLabel.CENTER);
                        aboutFrame.add(infoLabel, BorderLayout.CENTER);
                        JTextField linkField = new JTextField("https://github.com/jar-analyzer/jar-analyzer");
                        linkField.setEditable(false);
                        linkField.setHorizontalAlignment(JTextField.CENTER);
                        aboutFrame.add(linkField, BorderLayout.SOUTH);
                        aboutFrame.setLocationRelativeTo(null);
                        aboutFrame.setVisible(true);
                    } catch (IOException ignored) {
                    }
                }
            }).start());
            aboutMenu.add(aboutItem);
            return aboutMenu;
        } catch (Exception ex) {
            return null;
        }
    }
}

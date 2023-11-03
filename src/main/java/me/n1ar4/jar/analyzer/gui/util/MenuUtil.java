package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.gui.ChangeLogForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import com.github.rjeschke.txtmark.Processor;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final MainForm instance = MainForm.getInstance();

    public static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createAboutMenu());
        menuBar.add(createVersionMenu());
        return menuBar;
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

            return aboutMenu;
        } catch (Exception ex) {
            return null;
        }
    }

    private static JMenu createVersionMenu() {
        try {
            JMenu verMenu = new JMenu("version");
            JMenuItem jarItem = new JMenuItem("version: " + Const.version);
            InputStream is = MainForm.class.getClassLoader().getResourceAsStream("img/ver.png");
            if (is == null) {
                return null;
            }
            ImageIcon imageIcon = new ImageIcon(ImageIO.read(is));
            jarItem.setIcon(imageIcon);

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

            JMenuItem downItem = new JMenuItem("check update");
            is = MainForm.class.getClassLoader().getResourceAsStream("img/normal.png");
            if (is == null) {
                return null;
            }
            imageIcon = new ImageIcon(ImageIO.read(is));
            downItem.setIcon(imageIcon);
            downItem.addActionListener(e -> {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(Const.checkUpdateUrl)
                        .addHeader("Connection", "close")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    @SuppressWarnings("all")
                    public void onFailure(Call call, IOException e) {
                        JOptionPane.showMessageDialog(instance.getMasterPanel(), e.toString());
                    }

                    @Override
                    @SuppressWarnings("all")
                    public void onResponse(Call call, Response response) {
                        try {
                            if (response.body() == null) {
                                JOptionPane.showMessageDialog(instance.getMasterPanel(), "network error");
                            }
                            String body = response.body().string();
                            String ver = body.split("\"tag_name\":")[1].split(",")[0];
                            ver = ver.substring(1, ver.length() - 1);

                            String output;
                            output = String.format("%s: %s\n%s: %s",
                                    "Current Version", Const.version,
                                    "Latest Version", ver);
                            JOptionPane.showMessageDialog(instance.getMasterPanel(), output);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(instance.getMasterPanel(), ex.toString());
                        }
                    }
                });
            });

            verMenu.add(jarItem);
            verMenu.add(updateItem);
            verMenu.add(downItem);
            return verMenu;
        } catch (Exception ex) {
            logger.error("error: {}", ex.toString());
        }
        return null;
    }
}

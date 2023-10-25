package me.n1ar4.jar.analyzer.gui.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class IconManager {
    public static ImageIcon showIcon;
    public static ImageIcon javaIcon;
    public static ImageIcon dbIcon;
    public static ImageIcon startIcon;
    public static ImageIcon jarIcon;
    public static ImageIcon curIcon;
    public static ImageIcon auIcon;
    public static ImageIcon githubIcon;
    public static ImageIcon chatIcon;

    static {
        try {
            showIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/logo.png"))));
            Image image = showIcon.getImage();
            Image resizedImage = image.getScaledInstance(90, 75, Image.SCALE_SMOOTH);
            showIcon = new ImageIcon(resizedImage);

            javaIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/java.png"))));

            dbIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/db.png"))));

            startIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/start.png"))));

            jarIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/jar.png"))));

            curIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/cur.png"))));

            auIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/au.png"))));
            image = auIcon.getImage();
            resizedImage = image.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            auIcon = new ImageIcon(resizedImage);

            githubIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/github.png"))));

            chatIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/chat.png"))));
            image = chatIcon.getImage();
            resizedImage = image.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            chatIcon = new ImageIcon(resizedImage);
        } catch (Exception ignored) {
        }
    }
}

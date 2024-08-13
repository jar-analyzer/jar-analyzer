package me.n1ar4.games.flappy;

import org.dom4j.Element;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

public class Bird extends FBImgIcon implements InXMLAnalysis, Runnable {
    private static final long serialVersionUID = 1L;
    private final ImageIcon[] imgIcons = new ImageIcon[3];
    private int iconIndex = 0;

    private enum Color {
        RED, BLUE, YELLOW
    }

    private Color nowColor = Color.RED;

    public Bird(FBMainFrame frame, String url) {
        super(frame, url);
        confirmColor();
        xmlAnalysis(XMLRoot.getConfigRootElement());
    }

    @Override
    public void drawImage(Graphics g) {
        g.drawImage(imgIcons[iconIndex].getImage(), x, y, null);
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        boolean live = true;
        while (live) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            if (iconIndex == 2) {
                iconIndex = 0;
            } else {
                iconIndex++;
            }
        }
    }

    @Override
    public void xmlAnalysis(Element root) {
        Element birdNode = root.element("FlappyBird").element("model").element("Bird");
        width = Integer.parseInt(birdNode.element("png_bird_width").getText());
        height = Integer.parseInt(birdNode.element("png_bird_height").getText());
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            switch (nowColor) {
                case RED:
                    imgIcons[0] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird20_url").getText()))));
                    imgIcons[1] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird21_url").getText()))));
                    imgIcons[2] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird22_url").getText()))));
                    break;
                case BLUE:
                    imgIcons[0] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird10_url").getText()))));
                    imgIcons[1] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird11_url").getText()))));
                    imgIcons[2] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird12_url").getText()))));
                    break;
                case YELLOW:
                    imgIcons[0] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird00_url").getText()))));
                    imgIcons[1] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird01_url").getText()))));
                    imgIcons[2] = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                            loader.getResourceAsStream(birdNode.element("png_bird02_url").getText()))));
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void confirmColor() {
        Random random = new Random(Calendar.getInstance().getTimeInMillis());
        int a = random.nextInt(3); //0， 1， 2
        switch (a) {
            case 0:
                nowColor = Color.YELLOW;
                break;
            case 1:
                nowColor = Color.BLUE;
                break;
            case 2:
                nowColor = Color.RED;
                break;
            default:
                break;
        }
    }

    public int getBirdShift() {
        return 3;
    }
}

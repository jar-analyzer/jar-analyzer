package me.n1ar4.games.flappy;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class FBImgIcon extends ImageIcon implements InDrawImage {
    private static final long serialVersionUID = 1L;
    protected int x, y;
    protected int width, height;
    protected FBMainFrame frame;

    public FBImgIcon(FBMainFrame frame, URL url, int x, int y, int width, int height) {
        this(frame, url);
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public FBImgIcon(FBMainFrame frame, String url) {
        super(url);
        this.frame = frame;
    }

    public FBImgIcon(FBMainFrame frame, URL url) {
        super(url);
        this.frame = frame;
    }

    @Override
    public void drawImage(Graphics g) {
        g.drawImage(getImage(), x, y, null);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

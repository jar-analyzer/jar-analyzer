package me.n1ar4.games.plane;

import java.awt.*;

abstract public class Location {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Image image;

    public Location(int x, int y, int width, int height, Image breakplane) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = breakplane;
    }

    public void move(long time) {

    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

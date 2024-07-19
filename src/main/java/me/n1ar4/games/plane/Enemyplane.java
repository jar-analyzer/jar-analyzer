package me.n1ar4.games.plane;

import java.awt.*;
import java.awt.image.BufferedImage;

@SuppressWarnings("all")
public class Enemyplane extends Location {

    public Enemyplane(int x, int y, int width, int height, BufferedImage image) {
        super(x, y, width, height, image);
    }

    @Override
    public void move(long time) {
        if (time % 30 == 0) {
            y += 5;
        }
    }

    public Enemybullet fire(int width, int height, BufferedImage image) {
        int x = this.x + 30;
        int y = this.y + 30;
        Enemybullet b = new Enemybullet(x, y, width, height, image);
        return b;
    }

    public Break breakplane(int width, int height, Image image) {
        int x = this.x + 15;
        int y = this.y + 30;
        Break b = new Break(x, y, width, height, image);
        return b;
    }

}

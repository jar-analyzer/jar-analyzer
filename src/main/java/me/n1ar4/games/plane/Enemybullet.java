package me.n1ar4.games.plane;

import java.awt.image.BufferedImage;

@SuppressWarnings("all")
public class Enemybullet extends Location {

    public Enemybullet(int x, int y, int width, int height, BufferedImage image) {
        super(x, y, width, height, image);
    }

    @Override
    public void move(long time) {
        if (time % 30 == 0) {
            y += 10;
        }
    }
}

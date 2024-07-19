package me.n1ar4.games.plane;

@SuppressWarnings("all")
public class Mybullet extends Location {

    public Mybullet(int x, int y) {
        super(x, y, 30, 40, PM.mybulletpng);

    }

    public void move(long time) {
        if (time % 30 == 0) {
            y -= 15;
        }
    }

}
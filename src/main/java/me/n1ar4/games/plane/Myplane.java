package me.n1ar4.games.plane;

@SuppressWarnings("all")
public class Myplane extends Location {

    public Myplane(int x, int y) {
        super(x, y, 70, 100, PM.myplanepng);
    }

    public Mybullet fire() {
        Mybullet bullet = new Mybullet(this.x + this.width - 50, this.y - 10);
        return bullet;
    }

    @Override
    public void move(long time) {
    }

    public void Up(int d) {
        y -= 10d;
        if (y < 0)
            y += 10d;
    }

    public void Down(int d) {
        y += 10d;
        if (y > 650)
            y -= 10d;
    }

    public void Left(int d) {
        x -= 10d;
        if (x < 10)
            x += 10d;
    }

    public void Right(int d) {
        x += 10d;
        if (x > 400)
            x -= 10d;
    }

}

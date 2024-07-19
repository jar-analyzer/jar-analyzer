package me.n1ar4.games.plane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@SuppressWarnings("all")
public class PM {
    public static BufferedImage icon;
    public static BufferedImage myplanepng;
    public static BufferedImage mybulletpng;
    public static BufferedImage enemy1png;
    public static BufferedImage enemy2png;
    public static BufferedImage enemy3png;
    public static BufferedImage enemy4png;
    public static BufferedImage enemy5png;
    public static BufferedImage enemybulletpng;
    public static BufferedImage gameoverjpg;

    static {
        try {
            icon = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/icon.png"));
            myplanepng = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/myplane.png"));
            mybulletpng = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/mybullet.png"));
            enemy1png = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/enemy1.png"));
            enemy2png = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/enemy2.png"));
            enemy3png = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/enemy3.png"));
            enemy4png = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/enemy4.png"));
            enemy5png = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/enemy5.png"));
            enemybulletpng = ImageIO.read(PM.class.getClassLoader().getResourceAsStream(
                    "game/plane/enemybullet.png"));
            gameoverjpg = ImageIO.read(PM.class.getClassLoader().getResourceAsStream("game/plane/gameover.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

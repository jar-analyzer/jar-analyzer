package me.n1ar4.games.pocker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@SuppressWarnings("all")
public class Card extends JLabel implements MouseListener {
    Main main;
    String name;
    boolean up;
    boolean canClick = false;
    boolean clicked = false;

    public Card(Main m, String name, boolean up) {
        this.main = m;
        this.name = name;
        this.up = up;
        if (this.up)
            this.turnFront();
        else {
            this.turnRear();
        }
        this.setSize(71, 96);
        this.setVisible(true);
        this.addMouseListener(this);
    }

    public void turnFront() {
        try {
            this.setIcon(new ImageIcon(ImageIO.read(
                    this.getClass().getClassLoader().getResourceAsStream("game/pocker/" + "images/" + name + ".gif"))));
            this.up = true;
        } catch (Exception ignored) {
        }
    }

    public void turnRear() {
        try {
            this.setIcon(new ImageIcon(ImageIO.read(
                    this.getClass().getClassLoader().getResourceAsStream("game/pocker/" + "images/rear.gif"))));
            this.up = false;
        } catch (Exception ignored) {
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent e) {
        if (canClick) {
            Point from = this.getLocation();
            int step;
            if (clicked)
                step = -20;
            else {
                step = 20;
            }
            clicked = !clicked;
            Common.move(this, from, new Point(from.x, from.y - step));
        }
    }
}

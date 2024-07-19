package me.n1ar4.games.plane;

import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

@SuppressWarnings("all")
public class Home extends JFrame {
    private JLabel background1, start, prompt;
    private Home h;

    public Home() {
        super("雷电");
        setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        setSize(500, 800);
        setIconImage(PM.icon);
        setLayout(null);
        Background();
        Label();
        Keyborad();
        h = this;
    }

    public void Keyborad() {

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                if (key == KeyEvent.VK_ENTER) {
                    Content game = new Content(h);
                    add(game);
                    game.setSize(500, 800);
                    game.setFocusable(true);
                    game.requestFocus();

                    remove(start);
                    remove(prompt);
                }
            }
        });
    }

    private void Background() {


        ImageIcon background = null;
        try {
            background = new ImageIcon(ImageIO.read(
                    this.getClass().getClassLoader().getResourceAsStream("game/plane/Home.jpg")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        background1 = new JLabel(background);

        background1.setBounds(0, 0, 500, 800);

        JPanel j = (JPanel) getContentPane();
        j.setOpaque(false);

        getLayeredPane().add(background1, new Integer(Integer.MIN_VALUE));
    }

    private void Label() {


        ImageIcon icon = null;
        try {
            icon = new ImageIcon(ImageIO.read(
                    this.getClass().getClassLoader().getResourceAsStream("game/plane/prompt.gif")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        start = new JLabel("回车开始游戏");
        start.setFont(new Font("黑体", Font.BOLD, 50));
        start.setForeground(Color.black);
        start.setBounds(135, 500, 400, 120);

        prompt = new JLabel(icon);
        prompt.setBounds(-50, 500, 250, 120);

        add(start);
        add(prompt);

    }
}

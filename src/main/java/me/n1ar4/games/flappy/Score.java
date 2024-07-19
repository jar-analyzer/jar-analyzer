package me.n1ar4.games.flappy;

import org.dom4j.Element;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Score implements InXMLAnalysis, InDrawImage {

    private int score = 0;

    private final String[] numsUrl = new String[10];

    private ImageIcon[] numIcons = new ImageIcon[1];

    public Score() {
        xmlAnalysis(XMLRoot.getConfigRootElement());
    }

    @Override
    public void drawImage(Graphics g) {
        int spp = 0;
        for (int i = 0; i < numIcons.length; i++) {
            if (i > 0) {
                spp += numIcons[i - 1].getIconWidth();
            }
            int x = 20;
            int space = 5;
            int y = 40;
            g.drawImage(numIcons[i].getImage(), x + spp + i * space, y, null);
        }
    }

    @Override
    public void xmlAnalysis(Element root) {
        Element scoreNode = root.element("FlappyBird").element("model").element("Score");
        for (int i = 0; i < numsUrl.length; i++) {
            StringBuilder uuu = new StringBuilder("png_num");
            uuu.append(i);
            uuu.append("_url");
            String uu = new String(uuu);
            numsUrl[i] = scoreNode.element(uu).getText();
        }
        numIcons[0] = new ImageIcon(Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader().getResource(numsUrl[0])));
    }

    private void exchange(char[] cs) {
        if (null != cs && cs.length > 0) {
            numIcons = new ImageIcon[cs.length];
            for (int i = 0; i < cs.length; i++) {
                numIcons[i] = new ImageIcon(
                        Objects.requireNonNull(
                                Thread.currentThread().getContextClassLoader().getResource(numsUrl[cs[i] - 48])));
            }
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        char[] cs = String.valueOf(score).toCharArray();
        exchange(cs);
        this.score = score;
    }

}

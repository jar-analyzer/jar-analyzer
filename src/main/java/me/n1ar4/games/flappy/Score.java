/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

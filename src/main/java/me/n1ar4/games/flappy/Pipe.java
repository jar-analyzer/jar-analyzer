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
import java.net.URL;

public class Pipe extends FBImgIcon implements InXMLAnalysis {
    private static final long serialVersionUID = 1L;
    private ImageIcon upIcon, downIcon;
    private final int verticalSpace = 100;
    private boolean passed = false;
    private int x1;
    private int x2;
    private int x3;
    private int x4;
    private int y2;
    private int y3;
    private int y4;
    private int y5;
    private int bLeftX, bRightX, bUpY, bDownY;

    public Pipe(FBMainFrame frame, String url) {
        super(frame, url);
        xmlAnalysis(XMLRoot.getConfigRootElement());
    }

    @Override
    public void drawImage(Graphics g) {
        g.drawImage(upIcon.getImage(), x, y, null);
        g.drawImage(downIcon.getImage(), x, y + height + verticalSpace, null);
        x1 = x;
        x2 = x1 + 3;
        x3 = x1 + width;
        x4 = x3 - 3;
        y3 = y + height;
        y2 = y3 - 24;
        y4 = y3 + verticalSpace;
        y5 = y4 + 24;
    }

    public boolean crash() {
        calcBird();
        if (bDownY < y2 && (bRightX < x2 || bLeftX > x4))
            return false;
        if (bDownY >= y2 && bDownY < y3 && (bRightX < x1 || bLeftX > x3))
            return false;
        if (bUpY >= y2 && bUpY <= y3 && (bRightX < x1 || bLeftX > x3))
            return false;
        if (bUpY > y3 && bDownY < y4)
            return false;
        if (bDownY >= y4 && bDownY < y5 && (bRightX < x1 || bLeftX > x3))
            return false;
        if (bUpY >= y4 && bUpY <= y5 && (bRightX < x1 || bLeftX > x3))
            return false;
        return bUpY <= y5 || (bRightX >= x2 && bLeftX <= x4);
    }

    public boolean doCrashDetection() {
        Bird bird = frame.getBird();
        if (!passed) {
            if (x < bird.getX() - width) {
                passed = true;
                Score score = frame.getScore();
                int a = score.getScore();
                score.setScore(a + 1);
            }
        }
        return x < bird.getWidth() + bird.getX() + 1 && x > bird.getX() - width - 1;
    }

    @Override
    public void xmlAnalysis(Element root) {
        Element pipe = root.element("FlappyBird").element("model").element("Pipe");
        width = Integer.parseInt(pipe.element("png_pipe_width").getText());
        height = Integer.parseInt(pipe.element("png_pipe_height").getText());
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL upUrl = loader.getResource(pipe.element("pipe_up_url").getText());
        URL downUrl = loader.getResource(pipe.element("pipe_down_url").getText());
        if (upUrl == null || downUrl == null) {
            return;
        }
        upIcon = new ImageIcon(upUrl);
        downIcon = new ImageIcon(downUrl);
    }

    private void calcBird() {
        Bird bird = frame.getBird();
        bLeftX = bird.getX();
        bRightX = bird.getX() + bird.getWidth();
        bUpY = bird.getY();
        bDownY = bird.getY() + bird.getHeight();
    }

    public int getVerticalSpace() {
        return verticalSpace;
    }
}

package me.n1ar4.games.flappy;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

public class FBListener implements MouseListener, MouseMotionListener, Runnable {
    private final FBMainFrame frame;
    private int x1, x2, x3, x4, y1, y2;
    private int x5, x6, y3, y4;
    private boolean drop = true;
    private long jumpTime = 0L;
    private Long lastJumpTime = null;
    private Long lastDropTime = null;
    private int lastDropY = 0;
    private boolean jumpFinished = true;

    public FBListener(FBMainFrame frame) {
        this.frame = frame;
        confirmRange();
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        while (true) {
            frame.repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            move();
        }
    }

    private void move() {
        Bird bird = frame.getBird();
        if (frame.isGamePrepared()) {
            if (bird.getX() >= frame.getContentPane().getWidth()) {
                bird.setX(-bird.getWidth());
            } else {
                bird.setX(bird.getX() + bird.getBirdShift());
            }
            return;
        }
        if (frame.isGameStart()) {
            moveBgLand();
            return;
        }
        if (!frame.isGameOver()) {
            moveBgLand();
            if (drop) {
                birdDrop();
            } else {
                birdJump();
            }
            createPipe();
            crashDetection();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (frame.isGamePrepared()) {
            boolean canPlay = isInPlayRange(x, y);
            if (canPlay) {
                gamePrepare();
            }
            return;
        }
        if (frame.isGameStart()) {
            if (isInTabRange(x, y)) {
                gameStart();
            }
            return;
        }
        if (!frame.isGameOver()) {
            bJump();
            return;
        }
        boolean canPlay = isInPlayRange(x, y);
        if (canPlay) {         // 结束 -> 准备
            frame.setGameStart(false);
            frame.setGameOver(false);
            gamePrepare();
        }
    }

    private void crashDetection() {
        LinkedList<Pipe> pipes = frame.getPipes();
        for (Pipe pipe : pipes) {
            if (pipe.doCrashDetection()) {
                if (pipe.crash()) {
                    frame.setGameOver(true);
                    break;
                }
            }
        }
    }

    private void bJump() {
        if (!drop) {
            lastDropTime = Calendar.getInstance().getTimeInMillis();
        } else {
            drop = false;
        }
    }

    private void bDrop() {
        drop = true;
    }

    private void birdJump() {
        long nowTime = Calendar.getInstance().getTimeInMillis();
        long ti = 0L; //上升持续的时间
        if (null != lastJumpTime && !jumpFinished) { // 纵坐标发生改变
            ti = nowTime - lastJumpTime;
            Bird bird = frame.getBird();
            int tii = ((int) ti) / 100;
            bird.setY(bird.getY() - (5 * tii - tii * tii / 2));
            insureBirdInRange();
        } else {
            jumpFinished = false;
            lastJumpTime = nowTime;
            jumpTime = 400L;
        }
        if (ti > jumpTime) {
            jumpFinished = true;
            bDrop();
            lastDropTime = Calendar.getInstance().getTimeInMillis();
            lastDropY = frame.getBird().getY();
        }
    }

    private void birdDrop() {
        long nowTime = Calendar.getInstance().getTimeInMillis();
        if (null == lastDropTime) {
            lastDropTime = nowTime;
        }
        int ti = (int) (nowTime - lastDropTime) / 100;
        Bird bird = frame.getBird();
        bird.setY(lastDropY + ti * ti / 2);
        insureBirdInRange();
    }

    private void gameStart() {
        cursorHand(); // 用户鼠标指针变为手型
        initBird();

        LinkedList<Pipe> pipes = frame.getPipes();
        while (!pipes.isEmpty()) {
            pipes.remove(0);
        }
        frame.setGameStart(true);
        addPipe(pipes); //添加第一个管道对
        bJump(); // 开始时小鸟是上升一次
    }

    private void gamePrepare() {
        initBird();
        frame.getScore().setScore(0); //分数重置
        frame.setGamePrepared(true); // 游戏准备就绪
    }

    private void initBird() {
        //改变小鸟位置
        Bird bird = frame.getBird();
        bird.setX(frame.getFrameWidth() / 4 - bird.getWidth() / 2);
        bird.setY(frame.getFrameHeight() / 2 - bird.getHeight());
    }

    private void createPipe() {
        LinkedList<Pipe> pipes = frame.getPipes();
        if (!pipes.isEmpty()) {
            Pipe last = pipes.get(pipes.size() - 1);
            // 管道对之间的距离
            int gapLength = 100;
            if (frame.getWidth() - (last.getX() + last.getWidth()) >= gapLength) {
                addPipe(pipes);
            }
        }
    }

    private void addPipe(LinkedList<Pipe> pipes) {
        Pipe pipe = new Pipe(frame, null);
        pipe.setX(frame.getWidth());
        // 最大y值
        int y1 = 20;
        // 最小y值
        int y2 = -(2 * pipe.getHeight() + pipe.getVerticalSpace() - frame.getHeight()
                + frame.getLandImages()[0].getHeight());
        Random random = new Random(Calendar.getInstance().getTimeInMillis());
        int value = random.nextInt(y1 - y2 + 1);
        while (value < y1) {
            value = random.nextInt(y1 - y2 + 1);
        }
        pipe.setY(value + y2);
        pipes.add(pipe);
    }

    private void moveBgLand() {
        FBImgIcon[] bgImages = frame.getBgImages();
        FBImgIcon[] landImages = frame.getLandImages();
        for (FBImgIcon bgImage : bgImages) {
            int bgShift = 2;
            bgImage.setX(bgImage.getX() - bgShift);
            if (bgImage.getX() <= -(bgImage.getWidth())) {
                bgImage.setX(2 * bgImage.getWidth());
            }
        }
        for (int i = 0; i < bgImages.length; i++) {
            int landShift = 2;
            landImages[i].setX(landImages[i].getX() - landShift);
            if (landImages[i].getX() <= -(landImages[i].getWidth())) {
                landImages[i].setX(2 * landImages[i].getWidth());
            }
        }
        LinkedList<Pipe> pipes = frame.getPipes();
        Pipe delPipe = null;
        for (Pipe pipe : pipes) {
            int pipeShift = 2;
            pipe.setX(pipe.getX() - pipeShift);
            if (pipe.getX() <= -pipe.getWidth()) {
                delPipe = pipe;
            }
        }
        pipes.remove(delPipe);
    }

    private void confirmRange() {
        FBImgIcon play = frame.getPlayImage();
        FBImgIcon rank = frame.getRankImage();
        FBImgIcon tab = frame.getTabImage();
        y1 = play.getY();
        y2 = y1 + play.getHeight();
        x1 = play.getX();
        x2 = x1 + play.getWidth();
        x3 = rank.getX();
        x4 = x3 + rank.getWidth();
        x5 = tab.getX();
        x6 = x5 + tab.getWidth();
        y3 = tab.getY();
        y4 = y3 + tab.getHeight();
    }

    private boolean isInPlayRange(int x, int y) {
        return x > x1 && x < x2 && y > y1 && y < y2;
    }

    private boolean isInRankRange(int x, int y) {
        return x > x3 && x < x4 && y > y1 && y < y2;
    }

    private boolean isInTabRange(int x, int y) {
        return x > x5 && x < x6 && y > y3 && y < y4;
    }

    private void cursorHand() {
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void cursorDefault() {
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void insureBirdInRange() {
        Bird bird = frame.getBird();
        if (bird.getY() < 25) {
            bird.setY(25);
        }
        if (bird.getY() + bird.getHeight() > frame.getLandImages()[0].getY()) {
            bird.setY(frame.getLandImages()[0].getY() - bird.getHeight());
            frame.setGameOver(true);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (frame.isGamePrepared()) {
            boolean canPlay = isInPlayRange(x, y);
            boolean canRank = isInRankRange(x, y);
            if (canPlay || canRank) {
                cursorHand();
            } else {
                cursorDefault();
            }
            return;
        }
        if (frame.isGameStart()) {
            if (isInTabRange(x, y)) {
                cursorHand();
            } else {
                cursorDefault();
            }
            return;
        }
        if (frame.isGameOver()) {
            boolean canPlay = isInPlayRange(x, y);
            boolean canRank = isInRankRange(x, y);
            if (canPlay || canRank) {
                cursorHand();
            } else {
                cursorDefault();
            }
        }
    }
}

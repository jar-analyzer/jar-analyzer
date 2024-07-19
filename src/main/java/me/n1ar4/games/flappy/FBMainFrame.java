package me.n1ar4.games.flappy;

import org.dom4j.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.LinkedList;

public class FBMainFrame extends JFrame implements InXMLAnalysis {

    private static final long serialVersionUID = 1L;

    private String frameTitle;

    private int frameWidth, frameHeight;

    private final FBImgIcon[] bgImages = new FBImgIcon[3];

    private final FBImgIcon[] landImages = new FBImgIcon[3];

    private FBImgIcon titleImage, playImage, rankImage;

    private FBImgIcon readyImage, tabImage;

    private FBImgIcon overImage;

    private final Bird bird = new Bird(this, null);

    private final LinkedList<Pipe> pipes = new LinkedList<>();

    private boolean gamePrepared = false;
    private boolean gameStart = false;
    private boolean gameOver = false;
    private final Score score = new Score();

    public FBMainFrame() {
        initMainFrame();
    }

    public void startGame() {
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        BufferedImage buffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g2 = buffer.getGraphics();
        for (FBImgIcon icon : bgImages) {
            icon.drawImage(g2);
        }
        if (!gamePrepared) {
            titleImage.drawImage(g2);
            playImage.drawImage(g2);
            rankImage.drawImage(g2);
        } else if (!gameStart) {
            readyImage.drawImage(g2);
            tabImage.drawImage(g2);
            score.drawImage(g2);
        } else {
            for (Pipe pipe : pipes) {
                pipe.drawImage(g2);
            }
            score.drawImage(g2);
        }
        bird.drawImage(g2);
        if (gameOver) {
            overImage.drawImage(g2);
            playImage.drawImage(g2);
            rankImage.drawImage(g2);
        }
        for (FBImgIcon icon : landImages) {
            icon.drawImage(g2);
        }
        g.drawImage(buffer, 0, 0, null);
    }

    @Override
    public void xmlAnalysis(Element root) {
        Element fbMainFrameNode = root.element("FlappyBird").element("view").element("FBMainFrame");
        frameTitle = fbMainFrameNode.element("frame_title").getText();
        frameWidth = Integer.parseInt(fbMainFrameNode.element("frame_width").getText());
        frameHeight = Integer.parseInt(fbMainFrameNode.element("frame_height").getText());
        int bgWidth = Integer.parseInt(fbMainFrameNode.element("png_bg_width").getText());
        int bgHeight = Integer.parseInt(fbMainFrameNode.element("png_bg_height").getText());
        int landWidth = Integer.parseInt(fbMainFrameNode.element("png_land_width").getText());
        int landHeight = Integer.parseInt(fbMainFrameNode.element("png_land_height").getText());
        int titleWidth = Integer.parseInt(fbMainFrameNode.element("png_title_width").getText());
        int titleHeight = Integer.parseInt(fbMainFrameNode.element("png_title_height").getText());
        int playWidth = Integer.parseInt(fbMainFrameNode.element("png_play_width").getText());
        int playHeight = Integer.parseInt(fbMainFrameNode.element("png_play_height").getText());
        int rankWidth = Integer.parseInt(fbMainFrameNode.element("png_rank_width").getText());
        int rankHeight = Integer.parseInt(fbMainFrameNode.element("png_rank_height").getText());
        int readyWidth = Integer.parseInt(fbMainFrameNode.element("png_ready_width").getText());
        int readyHeight = Integer.parseInt(fbMainFrameNode.element("png_ready_height").getText());
        int overWidth = Integer.parseInt(fbMainFrameNode.element("png_over_width").getText());
        int overHeight = Integer.parseInt(fbMainFrameNode.element("png_over_height").getText());
        int tabWidth = Integer.parseInt(fbMainFrameNode.element("png_tab_width").getText());
        int tabHeight = Integer.parseInt(fbMainFrameNode.element("png_tab_height").getText());
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL bgUrl = loader.getResource(fbMainFrameNode.element("png_bg_url").getText());
        URL landUrl = loader.getResource(fbMainFrameNode.element("png_land_url").getText());
        URL titleUrl = loader.getResource(fbMainFrameNode.element("png_title_url").getText());
        URL playUrl = loader.getResource(fbMainFrameNode.element("png_play_url").getText());
        URL rankUrl = loader.getResource(fbMainFrameNode.element("png_rank_url").getText());
        URL readyUrl = loader.getResource(fbMainFrameNode.element("png_ready_url").getText());
        URL overUrl = loader.getResource(fbMainFrameNode.element("png_over_url").getText());
        URL tabUrl = loader.getResource(fbMainFrameNode.element("png_tab_url").getText());
        for (int i = 0; i < bgImages.length; i++) {
            bgImages[i] = new FBImgIcon(this, bgUrl, bgWidth * i, 0, bgWidth, bgHeight);
        }
        for (int i = 0; i < landImages.length; i++) {
            landImages[i] = new FBImgIcon(this, landUrl, landWidth * i, frameHeight - landHeight
                    , landWidth, landHeight);
        }
        int aa = frameHeight / 4;
        titleImage = new FBImgIcon(this, titleUrl, (frameWidth - titleWidth) / 2, aa - titleHeight / 2
                , titleWidth, titleHeight);
        playImage = new FBImgIcon(this, playUrl, frameWidth / 2 - playWidth - 1, aa * 2 - playHeight / 2
                , playWidth, playHeight);
        rankImage = new FBImgIcon(this, rankUrl, frameWidth / 2 + 1, aa * 2 - rankHeight / 2
                , rankWidth, rankHeight);
        readyImage = new FBImgIcon(this, readyUrl, (frameWidth - readyWidth) / 2, aa - readyHeight / 2
                , readyWidth, readyHeight);
        overImage = new FBImgIcon(this, overUrl, (frameWidth - overWidth) / 2, aa
                , overWidth, overHeight);
        tabImage = new FBImgIcon(this, tabUrl, (frameWidth - tabWidth) / 2, aa + readyHeight
                , tabWidth, tabHeight);
    }

    private void addListener() {
        FBListener listener = new FBListener(this);
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
        new Thread(listener).start();
    }

    private void move() {
        bird.setX((frameWidth - bird.getWidth()) / 2);
        bird.setY(frameHeight / 4 + titleImage.getHeight());
        new Thread(bird).start();
    }

    private void initMainFrame() {
        xmlAnalysis(XMLRoot.getConfigRootElement());
        move();
        addListener();
        this.setTitle(frameTitle);
        this.setSize(new Dimension(frameWidth, frameHeight));
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    public boolean isGamePrepared() {
        return !gamePrepared;
    }

    public void setGamePrepared(boolean gamePrepared) {
        this.gamePrepared = gamePrepared;
    }

    public boolean isGameStart() {
        return !gameStart;
    }

    public void setGameStart(boolean gameStart) {
        this.gameStart = gameStart;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public FBImgIcon getPlayImage() {
        return playImage;
    }

    public FBImgIcon getRankImage() {
        return rankImage;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public FBImgIcon[] getBgImages() {
        return bgImages;
    }

    public FBImgIcon[] getLandImages() {
        return landImages;
    }

    public Bird getBird() {
        return bird;
    }

    public FBImgIcon getTabImage() {
        return tabImage;
    }

    public LinkedList<Pipe> getPipes() {
        return pipes;
    }

    public Score getScore() {
        return score;
    }
}

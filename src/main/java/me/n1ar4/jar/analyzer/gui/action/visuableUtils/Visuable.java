package me.n1ar4.jar.analyzer.gui.action.visuableUtils;

import javax.swing.*;

/**
 * 所有自定义弹出窗口的父类
 */
public abstract class Visuable {
    public JFrame frame = new JFrame();
    public String[] items;

    public String[] getItems() {
        return items;
    }

    public void setItems(String[] items) {
        this.items = items;
    }
    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public Visuable(String title, String[] configurables){
        frame.setTitle(title);
        this.items =configurables;
        visualize();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public abstract void visualize();
}

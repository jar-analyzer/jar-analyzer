package me.n1ar4.jar.analyzer.gui.adapter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SearchTextFieldKeyAdapter extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            SearchInputListener.search(null, false);
        }
    }
}

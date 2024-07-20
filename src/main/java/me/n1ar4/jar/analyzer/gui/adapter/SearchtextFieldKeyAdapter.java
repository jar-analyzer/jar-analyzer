package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SearchtextFieldKeyAdapter extends KeyAdapter {
    private final JPanel fileTreeSearchPanel = MainForm.getInstance().getFileTreeSearchPanel();
    private final JTextField fileTreeSearchtextField = MainForm.getInstance().getFileTreeSearchtextField();

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            SearchInputListener.search(null, false);
        }
    }
}

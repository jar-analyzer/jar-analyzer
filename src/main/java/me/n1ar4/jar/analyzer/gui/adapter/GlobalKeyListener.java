package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.SearchForm;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GlobalKeyListener extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
            if (e.getKeyCode() == KeyEvent.VK_X) {
                if (MainForm.getCurMethod() == null) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "<html>ctrl+x<br>" +
                                    "<b>you should select a method first</b></html>");
                    return;
                }
                MainForm.getInstance().getTabbedPanel().setSelectedIndex(2);
            }
        }
        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
            if (e.getKeyCode() == KeyEvent.VK_F) {
                SearchForm.start();
            }
        }
    }
}

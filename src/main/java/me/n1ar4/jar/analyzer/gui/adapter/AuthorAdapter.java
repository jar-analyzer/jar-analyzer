package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.starter.Const;

import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class AuthorAdapter extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent evt) {
        try {
            Desktop desktop = Desktop.getDesktop();
            URI oURL = new URI(Const.authorUrl);
            desktop.browse(oURL);
        } catch (Exception ignored) {
        }
    }
}

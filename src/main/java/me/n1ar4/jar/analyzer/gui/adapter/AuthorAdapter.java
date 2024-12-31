/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.starter.Const;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

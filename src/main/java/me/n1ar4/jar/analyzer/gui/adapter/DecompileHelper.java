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

package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DecompileHelper {
    public static void decompile(TreePath selPath) {
        String sel = selPath.toString();
        sel = sel.substring(1, sel.length() - 1);
        String[] selArray = sel.split(",");
        ArrayList<String> pathList = new ArrayList<>();
        for (String s : selArray) {
            s = s.trim();
            pathList.add(s);
        }

        String[] path = pathList.toArray(new String[0]);
        String filePath = String.join(File.separator, path);

        if (!filePath.endsWith(".class")) {
            return;
        }

        Path thePath = Paths.get(filePath);
        if (!Files.exists(thePath)) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "file not exist");
            return;
        }

        String code = DecompileEngine.decompile(thePath);

        // SET FILE TREE HIGHLIGHT

        MainForm.getCodeArea().setText(code);
        MainForm.getCodeArea().setCaretPosition(0);

        StringBuilder classNameBuilder = new StringBuilder();
        for (int i = 1; i < path.length; i++) {
            classNameBuilder.append(path[i]).append("/");
        }
        String className = classNameBuilder.toString();
        int i = className.indexOf("classes");

        if (className.contains("BOOT-INF") || className.contains("WEB-INF")) {
            className = className.substring(i + 8, className.length() - 7);
        } else {
            className = className.substring(0, className.length() - 7);
        }

        CoreHelper.refreshAllMethods(className);

        MainForm.setCurClass(className);

        MainForm.getInstance().getCurClassText().setText(className);
        String jarName = MainForm.getEngine().getJarByClass(className);
        MainForm.getInstance().getCurJarText().setText(jarName);
        MainForm.getInstance().getCurMethodText().setText(null);
        MainForm.setCurMethod(null);
    }
}

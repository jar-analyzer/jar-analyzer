package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.core.Env;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import javax.swing.*;

public class JarsInJarAction {
    public static void run() {
        JCheckBox jarsInJar = MainForm.getInstance().getResolveJarsInJarCheckBox();
        jarsInJar.addActionListener(e -> {
            if (!jarsInJar.isSelected()) {
                LogUtil.log("not use jars in jar");
                Env.jarsInJar = false;
            } else {
                LogUtil.log("use jars in jar");
                Env.jarsInJar = true;
            }
        });
    }
}

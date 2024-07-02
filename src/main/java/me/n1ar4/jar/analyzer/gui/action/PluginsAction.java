package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.el.ELForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.plugins.encoder.EncodeUtilForm;
import me.n1ar4.jar.analyzer.plugins.listener.ListenUtilForm;
import me.n1ar4.jar.analyzer.plugins.obfuscate.ObfuscateForm;
import me.n1ar4.jar.analyzer.plugins.repeater.HttpUtilForm;
import me.n1ar4.jar.analyzer.plugins.serutil.SerUtilForm;
import me.n1ar4.jar.analyzer.plugins.sqlite.SQLiteForm;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;

public class PluginsAction {
    public static void startELForm() {
        JFrame frame = new JFrame(Const.SPELSearch);
        frame.setContentPane(new ELForm().elPanel);
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public static void run() {
        MainForm.getInstance().getSqliteButton().addActionListener(e -> SQLiteForm.start());

        MainForm.getInstance().getEncoderBtn().addActionListener(e -> EncodeUtilForm.start());

        MainForm.getInstance().getRepeaterBtn().addActionListener(e -> HttpUtilForm.start());

        MainForm.getInstance().getListenerBtn().addActionListener(e -> ListenUtilForm.start());

        MainForm.getInstance().getSpringELButton().addActionListener(e -> startELForm());

        MainForm.getInstance().getStartELSearchButton().addActionListener(e -> startELForm());

        MainForm.getInstance().getObfBtn().addActionListener(e -> ObfuscateForm.start());

        MainForm.getInstance().getSerUtilBtn().addActionListener(e -> SerUtilForm.start());
    }
}

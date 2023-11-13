package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.plugins.encoder.EncodeUtilForm;
import me.n1ar4.jar.analyzer.plugins.listener.ListenUtilForm;
import me.n1ar4.jar.analyzer.plugins.repeater.HttpUtilForm;

public class PluginsAction {
    public static void run() {
        MainForm.getInstance().getEncoderBtn().addActionListener(e -> EncodeUtilForm.start());

        MainForm.getInstance().getRepeaterBtn().addActionListener(e -> HttpUtilForm.start());

        MainForm.getInstance().getListenerBtn().addActionListener(e -> ListenUtilForm.start());
    }
}

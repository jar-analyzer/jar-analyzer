package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.analyze.asm.ASMPrint;
import me.n1ar4.jar.analyzer.analyze.asm.IdentifyCallEngine;
import me.n1ar4.jar.analyzer.analyze.cfg.CFGForm;
import me.n1ar4.jar.analyzer.analyze.frame.FrameForm;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.OpcodeForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("all")
public class ASMAction {
    public static void run() {
        MainForm instance = MainForm.getInstance();
        JButton opcodeBtn = instance.getOpcodeBtn();
        JButton asmBtn = instance.getJavaAsmBtn();
        JButton cfgBtn = instance.getCfgBtn();
        JButton frameBtn = instance.getFrameBtn();
        JButton simpleFrameBtn = instance.getSimpleFrameButton();

        cfgBtn.addActionListener(e -> {
            MethodResult curMethod = MainForm.getCurMethod();
            if (curMethod == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                return;
            }
            if (StringUtil.isNull(curMethod.getMethodName()) ||
                    StringUtil.isNull(curMethod.getMethodDesc()) ||
                    StringUtil.isNull(curMethod.getClassName())) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method data error");
                return;
            }
            CFGForm.start();
        });

        frameBtn.addActionListener(e -> {
            MethodResult curMethod = MainForm.getCurMethod();
            if (curMethod == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                return;
            }
            if (StringUtil.isNull(curMethod.getMethodName()) ||
                    StringUtil.isNull(curMethod.getMethodDesc()) ||
                    StringUtil.isNull(curMethod.getClassName())) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method data error");
                return;
            }
            FrameForm.start(true);
        });

        simpleFrameBtn.addActionListener(e -> {
            MethodResult curMethod = MainForm.getCurMethod();
            if (curMethod == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                return;
            }
            if (StringUtil.isNull(curMethod.getMethodName()) ||
                    StringUtil.isNull(curMethod.getMethodDesc()) ||
                    StringUtil.isNull(curMethod.getClassName())) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method data error");
                return;
            }
            FrameForm.start(false);
        });

        opcodeBtn.addActionListener(e -> {
            try {
                MethodResult curMethod = MainForm.getCurMethod();
                CoreEngine engine = MainForm.getEngine();

                if (curMethod == null) {
                    JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                    return;
                }
                if (StringUtil.isNull(curMethod.getMethodName()) ||
                        StringUtil.isNull(curMethod.getMethodDesc()) ||
                        StringUtil.isNull(curMethod.getClassName())) {
                    JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method data error");
                    return;
                }
                String absPath = curMethod.getClassPath().toAbsolutePath().toString();

                String test = IdentifyCallEngine.run(
                        absPath, curMethod.getMethodName(), curMethod.getMethodDesc());

                OpcodeForm.start(test);
            } catch (Exception ex) {
                LogUtil.warn("parse opcode error");
            }
        });

        asmBtn.addActionListener(e -> {
            try {
                MethodResult curMethod = MainForm.getCurMethod();
                CoreEngine engine = MainForm.getEngine();

                if (curMethod == null) {
                    JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                    return;
                }
                if (StringUtil.isNull(curMethod.getMethodName()) ||
                        StringUtil.isNull(curMethod.getMethodDesc()) ||
                        StringUtil.isNull(curMethod.getClassName())) {
                    JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method data error");
                    return;
                }
                String absPath = curMethod.getClassPath().toAbsolutePath().toString();

                InputStream is = Files.newInputStream(Paths.get(absPath));
                String data = ASMPrint.getPrint(is, true);

                OpcodeForm.start(data);
            } catch (Exception ex) {
                LogUtil.warn("parse opcode error");
            }
        });
    }
}

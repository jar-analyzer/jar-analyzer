package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.core.FinderRunner;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.state.State;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import org.objectweb.asm.Type;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FavMouseAdapter extends MouseAdapter {
    @SuppressWarnings("all")
    public void mouseClicked(MouseEvent evt) {
        JList<?> list = (JList<?>) evt.getSource();
        // 左键双击
        if (evt.getClickCount() == 2) {
            int index = list.locationToIndex(evt.getPoint());
            MethodResult res = null;
            try {
                res = (MethodResult) list.getModel().getElementAt(index);
            } catch (Exception ignored) {
            }
            if (res == null) {
                return;
            }
            String className = res.getClassName();
            String tempPath = className.replace("/", File.separator);
            String classPath;

            classPath = String.format("%s%s%s.class", Const.tempDir, File.separator, tempPath);
            if (!Files.exists(Paths.get(classPath))) {
                classPath = String.format("%s%sBOOT-INF%sclasses%s%s.class",
                        Const.tempDir, File.separator, File.separator, File.separator, tempPath);
                if (!Files.exists(Paths.get(classPath))) {
                    classPath = String.format("%s%sWEB-INF%sclasses%s%s.class",
                            Const.tempDir, File.separator, File.separator, File.separator, tempPath);
                    if (!Files.exists(Paths.get(classPath))) {
                        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                                "<html>" +
                                        "<p>need dependency or class file not found</p>" +
                                        "<p>缺少依赖或者文件找不到（考虑加载 rt.jar 并检查你的 JAR 是否合法）</p>" +
                                        "<p>默认以三种方式找类：</p>" +
                                        "<p>1.根据类名直接从根目录找（例如 <strong>com/a/b/Demo</strong> ）</p>" +
                                        "<p>2.从 <strong>BOOT-INF</strong> 找（" +
                                        "例如 <strong>BOOT-INF/classes/com/a/Demo</strong> ）</p>" +
                                        "<p>3.从 <strong>WEB-INF</strong> 找（" +
                                        "例如 <strong>WEB-INF/classes/com/a/Demo</strong> ）<p>" +
                                        "</html>");
                        return;
                    }
                }
            }

            String finalClassPath = classPath;

            MethodResult finalRes = res;
            new Thread(() -> {
                String code = DecompileEngine.decompile(Paths.get(finalClassPath));
                String methodName = finalRes.getMethodName();
                if (methodName.equals("<init>")) {
                    String[] c = finalRes.getClassName().split("/");
                    methodName = c[c.length - 1];
                }
                int paramNum = Type.getMethodType(
                        finalRes.getMethodDesc()).getArgumentTypes().length;
                int pos = FinderRunner.find(code, methodName, paramNum);

                // SET FILE TREE HIGHLIGHT
                SearchInputListener.getFileTree().searchPathTarget(className);

                MainForm.getCodeArea().setText(code);
                MainForm.getCodeArea().setCaretPosition(pos + 1);
            }).start();

            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            MethodResult refreshRes = res;
            new Thread() {
                @Override
                public void run() {
                    CoreHelper.refreshAllMethods(className);
                    CoreHelper.refreshCallers(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    CoreHelper.refreshCallee(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    CoreHelper.refreshHistory(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    CoreHelper.refreshImpls(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    CoreHelper.refreshSuperImpls(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    dialog.dispose();
                }
            }.start();

            MainForm.getInstance().getCurClassText().setText(className);
            String jarName = res.getJarName();
            if (StringUtil.isNull(jarName)) {
                jarName = MainForm.getEngine().getJarByClass(className);
            }
            MainForm.getInstance().getCurJarText().setText(jarName);
            MainForm.getInstance().getCurMethodText().setText(res.getMethodName());
            res.setClassPath(Paths.get(finalClassPath));
            MainForm.setCurMethod(res);

            State newState = new State();
            newState.setClassPath(Paths.get(finalClassPath));
            newState.setJarName(jarName);
            newState.setClassName(res.getClassName());
            newState.setMethodDesc(res.getMethodDesc());
            newState.setMethodName(res.getMethodName());

            MainForm.setPrevState(MainForm.getCurState());
            MainForm.setCurState(newState);
            MainForm.setNextState(null);
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem cleanAllFavorite = new JMenuItem("clean all favorite");
            JMenuItem cleanCurItems = new JMenuItem("clean this favorite");
            popupMenu.add(cleanAllFavorite);
            popupMenu.add(cleanCurItems);
            cleanAllFavorite.addActionListener(e -> {
                MainForm.getFavData().clear();
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "CLEAN ALL FAVORIATES FINISH");
            });
            cleanCurItems.addActionListener(e -> {
                MethodResult selectedItem = (MethodResult) list.getSelectedValue();
                if (MainForm.getFavData().removeElement(selectedItem)) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "CLEAN FAVORIATE " + selectedItem.getMethodName() + " FINISH");
                }
            });
            int index = list.locationToIndex(evt.getPoint());
            list.setSelectedIndex(index);
            popupMenu.show(list, evt.getX(), evt.getY());
        }
    }
}

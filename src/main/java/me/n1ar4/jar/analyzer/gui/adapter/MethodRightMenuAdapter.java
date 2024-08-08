package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 现在不支持改方法名的操作了
 * 但是先保留 说不定未来某天会遇到
 */
@SuppressWarnings("all")
public class MethodRightMenuAdapter extends MouseAdapter {
    private static final Logger logger = LogManager.getLogger();
    private final JList<MethodResult> list;
    private final JPopupMenu popupMenu;

    public static byte[] renameMethod(String className,
                                      String methodName,
                                      String methodDesc,
                                      String newMethodName) {
        try {
            Path finalFile = Paths.get(Const.tempDir).resolve(Paths.get(className + ".class"));
            ClassReader classReader = new ClassReader(Files.readAllBytes(finalFile));
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ClassVisitor(Const.ASMVersion, classWriter) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String signature, String[] exceptions) {
                    if (name.equals(methodName) && desc.equals(methodDesc)) {
                        name = newMethodName;
                    }
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            };
            classReader.accept(classVisitor, 0);
            return classWriter.toByteArray();
        } catch (Exception ex) {
            logger.error("rename method error: {}", ex.toString());
            return new byte[]{};
        }
    }


    @SuppressWarnings("all")
    public MethodRightMenuAdapter() {
        list = MainForm.getInstance().getAllMethodList();
        popupMenu = new JPopupMenu();
        JMenuItem renameItem = new JMenuItem("rename");
        popupMenu.add(renameItem);

        renameItem.addActionListener(e -> {
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex != -1) {
                DefaultListModel<MethodResult> model = (DefaultListModel<MethodResult>) list.getModel();
                MethodResult currentItem = model.getElementAt(selectedIndex);
                String newItem = JOptionPane.showInputDialog(MainForm.getInstance().getMasterPanel(),
                        "rename method: ", currentItem.getMethodName());
                if (newItem != null && !newItem.isEmpty()) {
                    // change database
                    int res = MainForm.getEngine().updateMethod(currentItem.getClassName(),
                            currentItem.getMethodName(),
                            currentItem.getMethodDesc(), newItem);
                    if (res == 0) {
                        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                                "update database error");
                        return;
                    }
                    byte[] modifiedClass = renameMethod(currentItem.getClassName(),
                            currentItem.getMethodName(), currentItem.getMethodDesc(), newItem);
                    try {
                        String originClass = currentItem.getClassName();
                        Path finalFile = Paths.get(Const.tempDir).resolve(Paths.get(originClass + ".class"));
                        Files.delete(finalFile);
                        Files.write(finalFile, modifiedClass);
                        DecompileEngine.cleanCache();
                        String code = DecompileEngine.decompile(finalFile);
                        MainForm.getCodeArea().setText(code);
                        logger.info("refresh bytecode");
                    } catch (Exception ignored) {
                        logger.error("write bytecode error");
                    }
                    currentItem.setMethodName(newItem);
                    model.setElementAt(currentItem, selectedIndex);
                }
            }
        });
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (e.isPopupTrigger()) {
                int index = list.locationToIndex(e.getPoint());
                list.setSelectedIndex(index);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}

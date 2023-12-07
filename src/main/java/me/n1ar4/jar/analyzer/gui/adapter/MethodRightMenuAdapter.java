package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MethodRightMenuAdapter extends MouseAdapter {
    private static final String TIPS = "<html>" +
            "super class is missing (need <b>rt.jar</b>)<br>" +
            "maybe super class is <b>java.lang.Object</b> from rt.jar" +
            "</html>";
    private final JList<MethodResult> list;
    private final JPopupMenu popupMenu;

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

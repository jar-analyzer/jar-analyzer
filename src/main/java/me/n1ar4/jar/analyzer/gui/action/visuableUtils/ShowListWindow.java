package me.n1ar4.jar.analyzer.gui.action.visuableUtils;

import me.n1ar4.jar.analyzer.gui.action.Visuable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 显示列表窗口--预备作为工作空间（项目选择）窗口
 */
public class ShowListWindow extends Visuable {

    public ShowListWindow(String title,String... configurables) {
        super(title,configurables);
    }

    @Override
    public void visualize() {
        JList<String> list = new JList<>(items);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 检查是否是双击事件
                if (e.getClickCount() == 2) {
                    // 获取双击时选中的列表项索引
                    int selectedIndex = list.locationToIndex(e.getPoint());
                    if (selectedIndex != -1) {
                        // 获取选中的列表项
                        String selectedValue = list.getModel().getElementAt(selectedIndex);
                        System.out.println("双击选中的项: " + selectedValue);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.setPreferredSize(new Dimension(500, 300));
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取选中的列表项
                int selectedIndex = list.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedValue = list.getModel().getElementAt(selectedIndex);
                    System.out.println("你选择了: " + selectedValue);
                }
                frame.dispose(); // 关闭对话框
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // 关闭对话框
            }
        });
    };
}

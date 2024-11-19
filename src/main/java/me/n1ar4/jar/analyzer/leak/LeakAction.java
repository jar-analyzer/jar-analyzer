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

package me.n1ar4.jar.analyzer.leak;

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.LeakResult;
import me.n1ar4.jar.analyzer.entity.MemberEntity;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.util.*;

public class LeakAction {
    private static final Logger logger = LogManager.getLogger();

    private static void log(String msg) {
        msg = "[LOG] " + msg + "\n";
        MainForm.getInstance().getLeakLogArea().append(msg);
        MainForm.getInstance().getLeakLogArea().setCaretPosition(
                MainForm.getInstance().getLeakLogArea().getDocument().getLength()
        );
    }

    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null) {
            return;
        }

        JCheckBox jwtBox = instance.getLeakJWTBox();
        JCheckBox idCardBox = instance.getLeakIdBox();
        JCheckBox ipAddrBox = instance.getLeakIpBox();
        JCheckBox emailBox = instance.getLeakEmailBox();
        JCheckBox urlBox = instance.getLeakUrlBox();
        JCheckBox jdbcBox = instance.getLeakJdbcBox();
        JCheckBox filePathBox = instance.getLeakFileBox();
        JCheckBox macAddrBox = instance.getLeakMacBox();
        JCheckBox phoneBox = instance.getLeakPhoneBox();

        jwtBox.setSelected(true);
        idCardBox.setSelected(true);
        ipAddrBox.setSelected(true);
        emailBox.setSelected(true);
        urlBox.setSelected(true);
        jdbcBox.setSelected(true);
        filePathBox.setSelected(true);
        filePathBox.setSelected(true);
        macAddrBox.setSelected(true);
        phoneBox.setSelected(true);

        JList<LeakResult> leakList = instance.getLeakResultList();

        logger.info("registering leak action");
        instance.getLeakStartBtn().addActionListener(e -> new Thread(() -> {
            CoreEngine engine = MainForm.getEngine();
            List<MemberEntity> members = engine.getAllMembersInfo();
            Map<String, String> stringMap = engine.getStringMap();

            Set<LeakResult> results = new LinkedHashSet<>();

            if (jwtBox.isSelected()) {
                log("jwt-token leak start");
                for (MemberEntity member : members) {
                    List<String> data = JWTRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("JWT-TOKEN");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = JWTRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("JWT-TOKEN");
                        results.add(leakResult);
                    }
                }
                log("jwt-token leak finish");
            }

            if (idCardBox.isSelected()) {
                log("id-card leak start");
                for (MemberEntity member : members) {
                    List<String> data = IDCardRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("ID-CARD");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = IDCardRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("ID-CARD");
                        results.add(leakResult);
                    }
                }
                log("id-card leak finish");
            }

            if (ipAddrBox.isSelected()) {
                log("ip-addr leak start");
                for (MemberEntity member : members) {
                    List<String> data = IPAddressRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("IP-ADDR");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = IPAddressRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("IP-ADDR");
                        results.add(leakResult);
                    }
                }
                log("ip-addr leak finish");
            }

            if (emailBox.isSelected()) {
                log("email leak start");
                for (MemberEntity member : members) {
                    List<String> data = EmailRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("EMAIL");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = EmailRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("EMAIL");
                        results.add(leakResult);
                    }
                }
                log("email leak finish");
            }

            if (urlBox.isSelected()) {
                log("url leak start");
                for (MemberEntity member : members) {
                    List<String> data = UrlRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("URL");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = UrlRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("URL");
                        results.add(leakResult);
                    }
                }
                log("url leak finish");
            }

            if (jdbcBox.isSelected()) {
                log("jdbc leak start");
                for (MemberEntity member : members) {
                    List<String> data = JDBCRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("JDBC");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = JDBCRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("JDBC");
                        results.add(leakResult);
                    }
                }
                log("jdbc leak finish");
            }

            if (filePathBox.isSelected()) {
                log("file-path leak start");
                for (MemberEntity member : members) {
                    List<String> data = FilePathRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("FILE-PATH");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = FilePathRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("FILE-PATH");
                        results.add(leakResult);
                    }
                }
                log("file-path leak finish");
            }

            if (macAddrBox.isSelected()) {
                log("mac-addr leak start");
                for (MemberEntity member : members) {
                    List<String> data = MacAddressRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("MAC-ADDR");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = MacAddressRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("MAC-ADDR");
                        results.add(leakResult);
                    }
                }
                log("mac-addr leak finish");
            }

            if (phoneBox.isSelected()) {
                log("phone leak start");
                for (MemberEntity member : members) {
                    List<String> data = PhoneRule.match(member.getValue());
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("PHONE");
                        results.add(leakResult);
                    }
                }
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String className = entry.getKey();
                    String value = entry.getValue();
                    List<String> data = PhoneRule.match(value);
                    if (data.isEmpty()) {
                        continue;
                    }
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName("PHONE");
                        results.add(leakResult);
                    }
                }
                log("phone leak finish");
            }

            DefaultListModel<LeakResult> model = new DefaultListModel<>();
            for (LeakResult leakResult : results) {
                model.addElement(leakResult);
            }
            leakList.setModel(model);
        }).start());
        instance.getLeakCleanBtn().addActionListener(e -> {
            leakList.setModel(new DefaultListModel<>());
            JOptionPane.showMessageDialog(instance.getMasterPanel(), "clean data finish");
        });
    }
}

/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.serutil;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.n1ar4.dbg.utils.HexUtil;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class SerUtilForm {
    private static final Logger logger = LogManager.getLogger();

    private static final String TEMPLATES = "ACED0005737200176A6176612E7574696C2E5072696F72697479517565756594DA30" +
            "B4FB3F82B103000249000473697A654C000A636F6D70617261746F727400164C6A6176612F7574696" +
            "C2F436F6D70617261746F723B7870000000027372002B6F72672E6170616368652E636F6D6D6F6E73" +
            "2E6265616E7574696C732E4265616E436F6D70617261746F72E3A188EA7322A4480200024C000A636" +
            "F6D70617261746F7271007E00014C000870726F70657274797400124C6A6176612F6C616E672F5374" +
            "72696E673B78707372003F6F72672E6170616368652E636F6D6D6F6E732E636F6C6C656374696F6E7" +
            "32E636F6D70617261746F72732E436F6D70617261626C65436F6D70617261746F72FBF49925B86EB1" +
            "3702000078707400106F757470757450726F706572746965737704000000037372003A636F6D2E737" +
            "56E2E6F72672E6170616368652E78616C616E2E696E7465726E616C2E78736C74632E747261782E54" +
            "656D706C61746573496D706C09574FC16EACAB3303000649000D5F696E64656E744E756D626572490" +
            "00E5F7472616E736C6574496E6465785B000A5F62797465636F6465737400035B5B425B00065F636C" +
            "6173737400125B4C6A6176612F6C616E672F436C6173733B4C00055F6E616D6571007E00044C00115" +
            "F6F757470757450726F706572746965737400164C6A6176612F7574696C2F50726F70657274696573" +
            "3B787000000000FFFFFFFF757200035B5B424BFD19156767DB37020000787000000002757200025B4" +
            "2ACF317F8060854E00200007870000006AACAFEBABE0000003200390A000300220700370700250700" +
            "2601001073657269616C56657273696F6E5549440100014A01000D436F6E7374616E7456616C75650" +
            "5AD2093F391DDEF3E0100063C696E69743E010003282956010004436F646501000F4C696E654E756D" +
            "6265725461626C650100124C6F63616C5661726961626C655461626C6501000474686973010013537" +
            "475625472616E736C65745061796C6F616401000C496E6E6572436C61737365730100354C79736F73" +
            "657269616C2F7061796C6F6164732F7574696C2F4761646765747324537475625472616E736C65745" +
            "061796C6F61643B0100097472616E73666F726D010072284C636F6D2F73756E2F6F72672F61706163" +
            "68652F78616C616E2F696E7465726E616C2F78736C74632F444F4D3B5B4C636F6D2F73756E2F6F726" +
            "72F6170616368652F786D6C2F696E7465726E616C2F73657269616C697A65722F53657269616C697A" +
            "6174696F6E48616E646C65723B2956010008646F63756D656E7401002D4C636F6D2F73756E2F6F726" +
            "72F6170616368652F78616C616E2F696E7465726E616C2F78736C74632F444F4D3B01000868616E64" +
            "6C6572730100425B4C636F6D2F73756E2F6F72672F6170616368652F786D6C2F696E7465726E616C2" +
            "F73657269616C697A65722F53657269616C697A6174696F6E48616E646C65723B01000A4578636570" +
            "74696F6E730700270100A6284C636F6D2F73756E2F6F72672F6170616368652F78616C616E2F696E7" +
            "465726E616C2F78736C74632F444F4D3B4C636F6D2F73756E2F6F72672F6170616368652F786D6C2F" +
            "696E7465726E616C2F64746D2F44544D417869734974657261746F723B4C636F6D2F73756E2F6F726" +
            "72F6170616368652F786D6C2F696E7465726E616C2F73657269616C697A65722F53657269616C697A" +
            "6174696F6E48616E646C65723B29560100086974657261746F720100354C636F6D2F73756E2F6F726" +
            "72F6170616368652F786D6C2F696E7465726E616C2F64746D2F44544D417869734974657261746F72" +
            "3B01000768616E646C65720100414C636F6D2F73756E2F6F72672F6170616368652F786D6C2F696E7" +
            "465726E616C2F73657269616C697A65722F53657269616C697A6174696F6E48616E646C65723B0100" +
            "0A536F7572636546696C6501000C476164676574732E6A6176610C000A000B07002801003379736F7" +
            "3657269616C2F7061796C6F6164732F7574696C2F4761646765747324537475625472616E736C6574" +
            "5061796C6F6164010040636F6D2F73756E2F6F72672F6170616368652F78616C616E2F696E7465726" +
            "E616C2F78736C74632F72756E74696D652F41627374726163745472616E736C65740100146A617661" +
            "2F696F2F53657269616C697A61626C65010039636F6D2F73756E2F6F72672F6170616368652F78616" +
            "C616E2F696E7465726E616C2F78736C74632F5472616E736C6574457863657074696F6E01001F7973" +
            "6F73657269616C2F7061796C6F6164732F7574696C2F476164676574730100083C636C696E69743E0" +
            "100116A6176612F6C616E672F52756E74696D6507002A01000A67657452756E74696D650100152829" +
            "4C6A6176612F6C616E672F52756E74696D653B0C002C002D0A002B002E010012687474703A2F2F657" +
            "8616D706C652E636F6D08003001000465786563010027284C6A6176612F6C616E672F537472696E67" +
            "3B294C6A6176612F6C616E672F50726F636573733B0C003200330A002B003401000D537461636B4D6" +
            "1705461626C6501001F79736F73657269616C2F50776E657231363639383736373938373631393031" +
            "0100214C79736F73657269616C2F50776E6572313636393837363739383736313930313B002100020" +
            "003000100040001001A000500060001000700000002000800040001000A000B0001000C0000002F00" +
            "010001000000052AB70001B100000002000D0000000600010000002F000E0000000C0001000000050" +
            "00F003800000001001300140002000C0000003F0000000300000001B100000002000D000000060001" +
            "00000034000E00000020000300000001000F003800000000000100150016000100000001001700180" +
            "0020019000000040001001A00010013001B0002000C000000490000000400000001B100000002000D" +
            "00000006000100000038000E0000002A000400000001000F003800000000000100150016000100000" +
            "001001C001D000200000001001E001F00030019000000040001001A00080029000B0001000C000000" +
            "24000300020000000FA70003014CB8002F1231B6003557B1000000010036000000030001030002002" +
            "000000002002100110000000A000100020023001000097571007E0010000001D4CAFEBABE00000032" +
            "001B0A0003001507001707001807001901001073657269616C56657273696F6E5549440100014A010" +
            "00D436F6E7374616E7456616C75650571E669EE3C6D47180100063C696E69743E0100032829560100" +
            "04436F646501000F4C696E654E756D6265725461626C650100124C6F63616C5661726961626C65546" +
            "1626C6501000474686973010003466F6F01000C496E6E6572436C61737365730100254C79736F7365" +
            "7269616C2F7061796C6F6164732F7574696C2F4761646765747324466F6F3B01000A536F757263654" +
            "6696C6501000C476164676574732E6A6176610C000A000B07001A01002379736F73657269616C2F70" +
            "61796C6F6164732F7574696C2F4761646765747324466F6F0100106A6176612F6C616E672F4F626A6" +
            "563740100146A6176612F696F2F53657269616C697A61626C6501001F79736F73657269616C2F7061" +
            "796C6F6164732F7574696C2F47616467657473002100020003000100040001001A000500060001000" +
            "700000002000800010001000A000B0001000C0000002F00010001000000052AB70001B10000000200" +
            "0D0000000600010000003C000E0000000C000100000005000F0012000000020013000000020014001" +
            "10000000A000100020016001000097074000450776E72707701007871007E000D78";

    private JPanel masterPanel;
    private JTextField serFileText;
    private JButton fileBtn;
    private JTextArea serArea;
    private JRadioButton hexRadio;
    private JRadioButton baseRadio;
    private JButton analyzeBtn;
    private JLabel serFileLabel;
    private JLabel serHexBaseLabel;
    private JPanel serHexBasePanel;
    private JScrollPane serScroll;
    private JLabel actionLabel;

    private static SerUtilForm instance;

    public SerUtilForm() {
        this.serArea.setText(TEMPLATES);
        this.hexRadio.setSelected(true);
        this.fileBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setDialogTitle("select a serialization file");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this.masterPanel);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                serFileText.setText(selectedFile.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this.masterPanel, "please select a file");
            }
        });
        this.analyzeBtn.addActionListener(e -> {
            String fileName = serFileText.getText();
            byte[] serData;
            if (fileName != null && !fileName.isEmpty()) {
                try {
                    serData = Files.readAllBytes(Paths.get(fileName));
                    if (hexRadio.isSelected()) {
                        serArea.setText(HexUtil.bytesToHex(serData));
                    } else {
                        serArea.setText(Base64.getEncoder().encodeToString(serData));
                    }
                } catch (Exception ex) {
                    logger.error("read file error: {}", ex.toString());
                    JOptionPane.showMessageDialog(this.masterPanel, "read file error");
                    return;
                }
            } else {
                String areaData = serArea.getText();
                if (areaData == null || areaData.isEmpty()) {
                    logger.error("data is null");
                    JOptionPane.showMessageDialog(this.masterPanel, "data is null");
                    return;
                }
                areaData = areaData.trim();
                if (hexRadio.isSelected()) {
                    serData = HexUtil.hexStringToBytes(areaData);
                } else if (baseRadio.isSelected()) {
                    serData = Base64.getDecoder().decode(areaData);
                } else {
                    return;
                }
            }
            SerUtil.show(serData);
        });
    }

    public static void start() {
        JFrame frame = new JFrame(Const.SerUtilForm);
        instance = new SerUtilForm();
        frame.setContentPane(instance.masterPanel);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        masterPanel = new JPanel();
        masterPanel.setLayout(new GridLayoutManager(4, 3, new Insets(3, 3, 3, 3), -1, -1));
        serFileLabel = new JLabel();
        serFileLabel.setText("Serialization Data File");
        masterPanel.add(serFileLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final Spacer spacer1 = new Spacer();
        masterPanel.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        serFileText = new JTextField();
        masterPanel.add(serFileText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        fileBtn = new JButton();
        fileBtn.setText("Chose File");
        masterPanel.add(fileBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serHexBaseLabel = new JLabel();
        serHexBaseLabel.setText("Serializatrion Hex/Base64");
        masterPanel.add(serHexBaseLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        serHexBasePanel = new JPanel();
        serHexBasePanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        masterPanel.add(serHexBasePanel, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        serScroll = new JScrollPane();
        serHexBasePanel.add(serScroll, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(400, 200), null, null, 0, false));
        serArea = new JTextArea();
        serArea.setLineWrap(true);
        serScroll.setViewportView(serArea);
        hexRadio = new JRadioButton();
        hexRadio.setText("HEX DATA");
        serHexBasePanel.add(hexRadio, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        baseRadio = new JRadioButton();
        baseRadio.setText("BASE64 DATA");
        serHexBasePanel.add(baseRadio, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        actionLabel = new JLabel();
        actionLabel.setText("Actions");
        masterPanel.add(actionLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        analyzeBtn = new JButton();
        analyzeBtn.setText("Analyze");
        masterPanel.add(analyzeBtn, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(hexRadio);
        buttonGroup.add(baseRadio);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return masterPanel;
    }

}

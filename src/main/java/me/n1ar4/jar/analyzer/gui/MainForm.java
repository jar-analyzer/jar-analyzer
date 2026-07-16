/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui;

import me.n1ar4.jar.analyzer.chains.ChainsBuilder;
import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.config.UIPrefs;
import me.n1ar4.jar.analyzer.dfs.DFSEngine;
import me.n1ar4.jar.analyzer.dfs.DFSResult;
import me.n1ar4.jar.analyzer.dfs.DFSUtil;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.LeakResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.exporter.*;
import me.n1ar4.jar.analyzer.gadget.GadgetUIBuilder;
import me.n1ar4.jar.analyzer.graph.HtmlGraph;
import me.n1ar4.jar.analyzer.gui.action.*;
import me.n1ar4.jar.analyzer.gui.adapter.*;
import me.n1ar4.jar.analyzer.gui.font.FontHelper;
import me.n1ar4.jar.analyzer.gui.render.*;
import me.n1ar4.jar.analyzer.gui.tree.FileTree;
import me.n1ar4.jar.analyzer.gui.update.UpdateChecker;
import me.n1ar4.jar.analyzer.gui.util.*;
import me.n1ar4.jar.analyzer.gui.vul.VulnerabilityBuilder;
import me.n1ar4.jar.analyzer.leak.LeakAction;
import me.n1ar4.jar.analyzer.plugins.jd.JDGUIStarter;
import me.n1ar4.jar.analyzer.sca.SCAAction;
import me.n1ar4.jar.analyzer.starter.Application;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.taint.TaintAnalyzer;
import me.n1ar4.jar.analyzer.taint.TaintCache;
import me.n1ar4.jar.analyzer.taint.TaintResult;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class MainForm {
    private static final Logger logger = LogManager.getLogger();
    // FOR CLI
    private static final MainForm fakeInstance = new MainForm(true);
    private static MainForm instance;
    private static ConfigFile config;
    private static CoreEngine engine;
    private static JTextArea codeArea;
    private static CodeTabPanel codeTabPanel;
    private static MethodResult curMethod;
    private static String curClass;
    private static DefaultListModel<MethodResult> historyListData;
    private JPanel masterPanel;
    private JTabbedPane tabbedPanel;
    private JPanel codePanel;
    private JButton choseBtn;
    private JTextField fileText;
    private JButton startEngineButton;
    private JCheckBox resolveJarsInJarCheckBox;
    private JPanel chosePanel;
    private JRadioButton fernRadio;
    private JPanel decompilerPanel;
    private JProgressBar buildBar;
    private JPanel infoPanel;
    private JLabel totalClassLabel;
    private JLabel totalClassVal;
    private JLabel totalMethodLabel;
    private JLabel totalMethodVal;
    private JLabel totalJarLabel;
    private JLabel totalJarVal;
    private JLabel databaseSizeLabel;
    private JLabel databaseSizeVal;
    private JRadioButton methodDefinitionRadioButton;
    private JRadioButton methodCallRadioButton;
    private JButton startSearchButton;
    private JRadioButton stringContainsRadioButton;
    private JRadioButton binarySearchRadioButton;
    private JTextField searchClassText;
    private FileTree fileTree;
    private JTextPane logArea;
    private JList<MethodResult> allMethodList;
    private JList<MethodResult> callerList;
    private JList<MethodResult> calleeList;
    private JList<MethodResult> historyList;
    private JTextField loadDBText;
    private JLabel dbPathLabel;
    private JLabel engineVal;
    private JLabel engineLabel;
    private JCheckBox autoSaveCheckBox;
    private JTextField curClassText;
    private JTextField curMethodText;
    private JLabel curClassLabel;
    private JLabel curMethodLabel;
    private JTextField searchMethodText;
    private JTextField searchStrText;
    private JList<MethodResult> searchList;
    private JButton exportSearchJsonBtn;
    private JTextField curJarText;
    private JLabel curJarLabel;
    private JTextField rtText;
    private JLabel jreRuntimeLabel;
    private JCheckBox autoFindRtJarCheckBox;
    private JCheckBox addRtJarWhenCheckBox;
    private JButton opcodeBtn;
    private JButton javaAsmBtn;
    private JPanel javaVulSearchPanel;
    private JLabel javaVulLabel;
    private JLabel logoLabel;
    private JLabel authorLabel;
    private JLabel authorTextLabel;
    private JPanel curPanel;
    private JList<MethodResult> methodImplList;
    private JCheckBox deleteTempCheckBox;
    private JList<MethodResult> superImplList;
    private JPanel analysis;
    private JButton cfgBtn;
    private JButton frameBtn;
    private JButton encoderBtn;
    private JButton listenerBtn;
    private JList<ClassResult> springCList;
    private JList<MethodResult> springMList;
    private JLabel encoderLabel;
    private JLabel listenerLabel;
    private JRadioButton likeSearchRadioButton;
    private JRadioButton equalsSearchRadioButton;
    private JTextArea blackArea;
    private JTextArea classBlackArea;
    private JLabel classBlackListLabel;
    private JLabel classBlackLabel;
    private JButton simpleFrameButton;
    private JButton refreshButton;
    private JLabel springLabel;
    private JButton sqliteButton;
    private JLabel sqliteLabel;
    private JButton cleanButton;
    private JButton showStringListButton;
    private JButton springELStartButton;
    @SuppressWarnings("all")
    private JLabel spelLabel;
    private JButton startELSearchButton;
    private JButton serUtilBtn;
    private JLabel serUtilLabel;
    private JTextArea classWhiteArea;
    private JLabel classWhiteListLabel;
    private JButton pathSearchButton;
    private JLabel pathSearchLabel;
    private JTextField pathSearchTextField;
    private JTextField fileTreeSearchTextField;
    private JPanel fileTreeSearchPanel;
    private JLabel fileTreeSearchLabel;
    private JCheckBox scaLog4jBox;
    private JCheckBox scaShiroBox;
    private JCheckBox scaFastjsonBox;
    private JTextField scaFileText;
    private JButton scaOpenBtn;
    private JTextArea scaConsoleArea;
    private JRadioButton scaOutConsoleRadio;
    private JRadioButton scaOutHtmlRadio;
    private JButton scaStartBtn;
    private JLabel scaOutLabel;
    private JLabel scaFileLabel;
    private JLabel scaTipLabel;
    private JLabel outputFileLabel;
    private JTextField outputFileText;
    private JButton scaResultOpenBtn;
    private JButton htmlGraphBtn;
    private JList<MethodResult> favList;
    private JButton addToFavoritesButton;
    private JButton bcelBtn;
    private JLabel bcelLabel;
    private JCheckBox nullParamBox;
    private JCheckBox leakUrlBox;
    private JCheckBox leakJdbcBox;
    private JCheckBox leakFileBox;
    private JCheckBox leakJWTBox;
    private JCheckBox leakMacBox;
    private JCheckBox leakIpBox;
    private JCheckBox leakPhoneBox;
    private JCheckBox leakIdBox;
    private JCheckBox leakEmailBox;
    private JCheckBox leakDetBase64Box;
    private JCheckBox leakAITriageBox;
    private JButton leakAITriageViewBtn;
    private JList<LeakResult> leakResultList;
    private JButton leakCleanBtn;
    private JButton leakStartBtn;
    private JProgressBar leakProgressBar;
    private JButton openJDBtn;
    private JTabbedPane webTabbed;
    private JPanel leftPanel;
    private JScrollPane treeScrollPanel;
    private JPanel searchFileNamePanel;
    private JPanel startPanel;
    private JScrollPane classBlackPanel;
    private JScrollPane classWhitePanel;
    private JPanel enginePanel;
    private JPanel authorPanel;
    private JPanel actionPanel;
    private JPanel searchResPanel;
    private JScrollPane searchScroll;
    private JPanel searchOptionsPanel;
    private JPanel searchInnerPanel;
    private JLabel searchClassLabel;
    private JLabel searchMethodLabel;
    private JLabel searchStrLabel;
    private JPanel soPanel;
    private JPanel blackListPanel;
    private JScrollPane blackScroll;
    private JComboBox<String> filterModeCombo;
    private JPanel npbPanel;
    private JPanel callPanel;
    private JScrollPane callerScroll;
    private JScrollPane calleeScroll;
    private JPanel methodImplPanel;
    private JScrollPane implScroll;
    private JScrollPane superImplScroll;
    private JPanel webPanel;
    private JPanel springPanel;
    private JPanel springCPanel;
    private JScrollPane scScroll;
    private JPanel springMPanel;
    private JScrollPane smScroll;
    private JPanel springIPanel;
    private JPanel servletPanel;
    private JPanel filterPanel;
    private JPanel listenerPanel;
    private JPanel notePanel;
    private JScrollPane hisScroll;
    private JScrollPane favScroll;
    private JPanel scaPanel;
    private JPanel modulePanel;
    private JPanel scaTipPanel;
    private JPanel scaActionPanel;
    private JScrollPane scanConsoleScroll;
    private JPanel scaOutPanel;
    private JPanel leakPanel;
    private JPanel leakRulesPanel;
    private JPanel leakConfigPanel;
    private JPanel leakResultPanel;
    private JScrollPane leakResultScroll;
    private JPanel advancePanel;
    private JPanel piPanel;
    private JPanel logPanel;
    private JScrollPane logScroll;
    private JPanel curMethodPanel;
    private JScrollPane allMethodScroll;
    private JSplitPane rootSplit;
    private JPanel coreRightSplit;
    private JSplitPane coreSplit;
    private JSplitPane treeContentSplit;
    private JList<ClassResult> springIList;
    private JScrollPane springIScroll;
    private JScrollPane servletScroll;
    private JList<ClassResult> servletList;
    private JScrollPane filterScroll;
    private JList<ClassResult> filterList;
    private JScrollPane listenerScroll;
    private JList<ClassResult> listenerList;
    private JButton exportJsonBtn;
    private JButton exportTxtBtn;
    private JButton exportCsvBtn;
    private JPanel exportPanel;
    private JLabel exportAllLabel;
    private JButton gadgetChoseBtn;
    private JTextField gadgetInputText;
    private JTable gadgetResultTable;
    private JPanel gadgetPanel;
    private JPanel gadgetInputPanel;
    private JLabel gadgetDirLabel;
    private JLabel gadgetDescLabel;
    private JLabel gadgetDescValueLabel;
    private JPanel gadgetResultPanel;
    private JScrollPane gadgetResultScroll;
    private JPanel gadgetOpPanel;
    private JButton gadgetStartBtn;
    private JCheckBox gadgetNativeBox;
    private JCheckBox gadgetHessianBox;
    private JCheckBox gadgetJdbcBox;
    private JCheckBox gadgetFastjsonBox;
    private JPanel chainsPanel;
    private JTextField sinkClassText;
    private JTextField sinkMethodText;
    private JTextField sinkDescText;
    private JTextField sourceClassText;
    private JTextField sourceMethodText;
    private JTextField sourceDescText;
    private JPanel chainsSinkPanel;
    private JPanel chainsSourcePanel;
    private JPanel chainsDescPanel;
    private JLabel chainsLabel;
    private JLabel sinkClassLabel;
    private JLabel sinkMethodLabel;
    private JLabel sinkDescLabel;
    private JLabel sourceClassLabel;
    private JLabel sourceMethodLabel;
    private JLabel sourceDescLabel;
    private JPanel chainsOpPanel;
    private JButton startChainsBtn;
    private JLabel sinkTipLabel;
    private JLabel sourceTipLabel;
    private JRadioButton sourceRadio;
    private JRadioButton sinkRadio;
    private JSpinner maxDepthSpin;
    private JLabel maxDepthLabel;
    private JRadioButton sourceNullRadio;
    private JRadioButton sourceEnableRadio;
    private JPanel sourcePanel;
    private JLabel sourceLabel;
    private JButton clearBtn;
    private JScrollPane javaVulScroll;
    private JPanel vulOpPanel;
    private JRadioButton showLowRadio;
    private JRadioButton showMediumRadio;
    private JRadioButton showHighRadio;
    private JRadioButton showAllRadio;
    private JComboBox<String> sinkBox;
    private JPanel chainsResult;
    private JCheckBox taintBox;
    private JCheckBox AKSKCheckBox;
    private JCheckBox bankCardCheckBox;
    private JCheckBox APIKeyCheckBox;
    private JCheckBox cryptoKeyCheckBox;
    private JCheckBox AIKeyCheckBox;
    private JCheckBox passwordCheckBox;
    private JButton exportLeakBtn;
    private JButton startTaintBtn;
    private JCheckBox sourceOnlyWebBox;
    private JLabel sinkJarLabel;
    private JButton loadFavBtn;
    private JPanel favHisPanel;
    private JButton dfsAdvanceBtn;
    private JButton quickSinkBtn;
    private JPanel springConfigPanel;
    private JScrollPane springConfigScroll;
    private JList<ClassResult> springConfigList;
    private static DefaultListModel<MethodResult> favData;
    private static int dfsMaxLimit = 30;
    private static String dfsBlacklist = "";

    public JButton getDfsAdvanceBtn() {
        return dfsAdvanceBtn;
    }

    public JCheckBox getTaintBox() {
        return taintBox;
    }

    public JScrollPane getJavaVulSearchPanel() {
        return javaVulScroll;
    }

    public JCheckBox getLeakUrlBox() {
        return leakUrlBox;
    }

    public JCheckBox getLeakJdbcBox() {
        return leakJdbcBox;
    }

    public JCheckBox getLeakFileBox() {
        return leakFileBox;
    }

    public JCheckBox getLeakJWTBox() {
        return leakJWTBox;
    }

    public JCheckBox getLeakMacBox() {
        return leakMacBox;
    }

    public JCheckBox getLeakIpBox() {
        return leakIpBox;
    }

    public JCheckBox getLeakPhoneBox() {
        return leakPhoneBox;
    }

    public JCheckBox getLeakIdBox() {
        return leakIdBox;
    }

    public JCheckBox getLeakEmailBox() {
        return leakEmailBox;
    }

    public JCheckBox getLeakDetBase64Box() {
        return leakDetBase64Box;
    }

    public JCheckBox getLeakAITriageBox() {
        return leakAITriageBox;
    }

    public JButton getLeakAITriageViewBtn() {
        return leakAITriageViewBtn;
    }

    public JList<LeakResult> getLeakResultList() {
        return leakResultList;
    }

    public JButton getLeakCleanBtn() {
        return leakCleanBtn;
    }

    public JButton getLeakStartBtn() {
        return leakStartBtn;
    }

    public JProgressBar getLeakProgressBar() {
        return leakProgressBar;
    }

    public static String getCurClass() {
        return curClass;
    }

    public static void setCurClass(String curClass) {
        MainForm.curClass = curClass;
    }

    public static DefaultListModel<MethodResult> getFavData() {
        return favData;
    }

    public JButton getAddToFavoritesButton() {
        return addToFavoritesButton;
    }

    public JButton getHtmlGraphBtn() {
        return htmlGraphBtn;
    }

    public JCheckBox getScaLog4jBox() {
        return scaLog4jBox;
    }

    public JCheckBox getScaShiroBox() {
        return scaShiroBox;
    }

    public JCheckBox getScaFastjsonBox() {
        return scaFastjsonBox;
    }

    public JTextField getScaFileText() {
        return scaFileText;
    }

    public JButton getScaOpenBtn() {
        return scaOpenBtn;
    }

    public JTextArea getScaConsoleArea() {
        return scaConsoleArea;
    }

    public JRadioButton getScaOutConsoleRadio() {
        return scaOutConsoleRadio;
    }

    public JRadioButton getScaOutHtmlRadio() {
        return scaOutHtmlRadio;
    }

    public JButton getScaStartBtn() {
        return scaStartBtn;
    }

    public JLabel getFileTreeSearchLabel() {
        return fileTreeSearchLabel;
    }

    public JButton getPathSearchButton() {
        return pathSearchButton;
    }

    public JTextField getPathSearchTextField() {
        return pathSearchTextField;
    }

    public JTextField getFileTreeSearchTextField() {
        return fileTreeSearchTextField;
    }

    public JPanel getFileTreeSearchPanel() {
        return fileTreeSearchPanel;
    }

    public JList<ClassResult> getSpringCList() {
        return springCList;
    }

    public JList<MethodResult> getSpringMList() {
        return springMList;
    }

    public JButton getEncoderBtn() {
        return encoderBtn;
    }

    public JButton getListenerBtn() {
        return listenerBtn;
    }

    public FileTree getFileTree() {
        return fileTree;
    }

    public JPanel getMasterPanel() {
        return masterPanel;
    }

    public JButton getChoseBtn() {
        return choseBtn;
    }

    public JButton getStartBuildDatabaseButton() {
        return startEngineButton;
    }

    public JTextField getFileText() {
        return fileText;
    }

    public JProgressBar getBuildBar() {
        return buildBar;
    }

    public JCheckBox getResolveJarsInJarCheckBox() {
        return resolveJarsInJarCheckBox;
    }

    public JLabel getTotalClassVal() {
        return totalClassVal;
    }

    public JLabel getTotalMethodVal() {
        return totalMethodVal;
    }

    public JLabel getTotalJarVal() {
        return totalJarVal;
    }

    public JLabel getDatabaseSizeVal() {
        return databaseSizeVal;
    }

    public JLabel getEngineVal() {
        return engineVal;
    }

    public JCheckBox getAutoSaveCheckBox() {
        return autoSaveCheckBox;
    }

    public JTextField getLoadDBText() {
        return loadDBText;
    }

    public JList<MethodResult> getAllMethodList() {
        return allMethodList;
    }

    public JTextField getCurClassText() {
        return curClassText;
    }

    public JTextField getCurJarText() {
        return curJarText;
    }

    public JTextField getCurMethodText() {
        return curMethodText;
    }

    public JTextField getSearchClassText() {
        return searchClassText;
    }

    public JButton getStartSearchButton() {
        return startSearchButton;
    }

    public JTextField getSearchMethodText() {
        return searchMethodText;
    }

    public JTextField getSearchStrText() {
        return searchStrText;
    }

    public JRadioButton getMethodDefinitionRadioButton() {
        return methodDefinitionRadioButton;
    }

    public JRadioButton getMethodCallRadioButton() {
        return methodCallRadioButton;
    }

    public JRadioButton getStringContainsRadioButton() {
        return stringContainsRadioButton;
    }

    public JRadioButton getBinarySearchRadioButton() {
        return binarySearchRadioButton;
    }

    public JList<MethodResult> getCallerList() {
        return callerList;
    }

    public JList<MethodResult> getMethodImplList() {
        return methodImplList;
    }

    public JList<MethodResult> getCalleeList() {
        return calleeList;
    }

    public JList<MethodResult> getSearchList() {
        return searchList;
    }

    public JTabbedPane getTabbedPanel() {
        return tabbedPanel;
    }

    public JList<MethodResult> getHistoryList() {
        return historyList;
    }

    public JTextField getRtText() {
        return rtText;
    }

    public JCheckBox getAutoFindRtJarCheckBox() {
        return autoFindRtJarCheckBox;
    }

    public JCheckBox getAddRtJarWhenCheckBox() {
        return addRtJarWhenCheckBox;
    }

    public JRadioButton getFernRadio() {
        return fernRadio;
    }

    public JButton getOpcodeBtn() {
        return opcodeBtn;
    }

    public JButton getJavaAsmBtn() {
        return javaAsmBtn;
    }

    public JButton getCfgBtn() {
        return cfgBtn;
    }

    public JButton getFrameBtn() {
        return frameBtn;
    }

    public JButton getSimpleFrameButton() {
        return simpleFrameButton;
    }

    public JCheckBox getDeleteTempCheckBox() {
        return deleteTempCheckBox;
    }

    public JList<MethodResult> getSuperImplList() {
        return superImplList;
    }

    public JRadioButton getLikeSearchRadioButton() {
        return likeSearchRadioButton;
    }

    public JRadioButton getEqualsSearchRadioButton() {
        return equalsSearchRadioButton;
    }

    public JTextArea getBlackArea() {
        return blackArea;
    }

    public JButton getQuickSinkBtn() {
        return quickSinkBtn;
    }

    /**
     * 获取当前搜索过滤模式
     * 根据 filterModeCombo 的选择返回对应的过滤模式
     *
     * @return 黑名单模式或白名单模式
     */
    public SearchFilterHelper.FilterMode getFilterMode() {
        if (filterModeCombo != null && filterModeCombo.getSelectedIndex() == 1) {
            return SearchFilterHelper.FilterMode.WHITELIST;
        }
        return SearchFilterHelper.FilterMode.BLACKLIST;
    }

    public JTextArea getClassBlackArea() {
        return classBlackArea;
    }

    public JTextArea getClassWhiteArea() {
        return classWhiteArea;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getSqliteButton() {
        return sqliteButton;
    }

    public JButton getSpringELButton() {
        return springELStartButton;
    }

    public JButton getCleanButton() {
        return cleanButton;
    }

    public JButton getShowStringListButton() {
        return showStringListButton;
    }

    public JButton getStartELSearchButton() {
        return startELSearchButton;
    }

    public JButton getSerUtilBtn() {
        return serUtilBtn;
    }

    public JButton getBcelBtn() {
        return bcelBtn;
    }

    public JTextField getOutputFileText() {
        return outputFileText;
    }

    public JButton getScaResultOpenBtn() {
        return scaResultOpenBtn;
    }

    public static MainForm getInstance() {
        if (instance == null) {
            return fakeInstance;
        } else {
            return instance;
        }
    }

    public JTextPane getLogArea() {
        return logArea;
    }

    public JCheckBox getNullParamBox() {
        return nullParamBox;
    }

    public static MethodResult getCurMethod() {
        return curMethod;
    }

    public static void setCurMethod(MethodResult curMethod) {
        MainForm.curMethod = curMethod;
    }

    public static JTextArea getCodeArea() {
        return codeArea;
    }

    public static void setCodeArea(JTextArea codeArea) {
        MainForm.codeArea = codeArea;
    }

    public static CodeTabPanel getCodeTabPanel() {
        return codeTabPanel;
    }

    public static void setCodeTabPanel(CodeTabPanel codeTabPanel) {
        MainForm.codeTabPanel = codeTabPanel;
    }

    public static CoreEngine getEngine() {
        return engine;
    }

    public static void setEngine(CoreEngine engine) {
        MainForm.engine = engine;
    }

    public static ConfigFile getConfig() {
        return config;
    }

    public static void setConfig(ConfigFile config) {
        MainForm.config = config;
    }

    public static DefaultListModel<MethodResult> getHistoryListData() {
        return historyListData;
    }

    public JList<ClassResult> getSpringIList() {
        return springIList;
    }

    public JList<ClassResult> getSpringConfigList() {
        return springConfigList;
    }

    public JList<ClassResult> getServletList() {
        return servletList;
    }

    public JList<ClassResult> getListenerList() {
        return listenerList;
    }

    public JList<ClassResult> getFilterList() {
        return filterList;
    }

    public JTextField getGadgetInputText() {
        return gadgetInputText;
    }

    public JButton getGadgetChoseBtn() {
        return gadgetChoseBtn;
    }

    public JTable getGadgetResultTable() {
        return gadgetResultTable;
    }

    public JLabel getGadgetDescValueLabel() {
        return gadgetDescValueLabel;
    }

    public JButton getGadgetStartBtn() {
        return gadgetStartBtn;
    }

    public JCheckBox getGadgetNativeBox() {
        return gadgetNativeBox;
    }

    public JCheckBox getGadgetHessianBox() {
        return gadgetHessianBox;
    }

    public JCheckBox getGadgetJdbcBox() {
        return gadgetJdbcBox;
    }

    public JCheckBox getGadgetFastjsonBox() {
        return gadgetFastjsonBox;
    }

    public JCheckBox getPasswordCheckBox() {
        return passwordCheckBox;
    }

    public JCheckBox getAIKeyCheckBox() {
        return AIKeyCheckBox;
    }

    public JCheckBox getCryptoKeyCheckBox() {
        return cryptoKeyCheckBox;
    }

    public JCheckBox getAPIKeyCheckBox() {
        return APIKeyCheckBox;
    }

    public JCheckBox getBankCardCheckBox() {
        return bankCardCheckBox;
    }

    public JCheckBox getAKSKCheckBox() {
        return AKSKCheckBox;
    }

    public JButton getExportLeakBtn() {
        return exportLeakBtn;
    }

    public JCheckBox getSourceOnlyWebBox() {
        return sourceOnlyWebBox;
    }

    public MainForm(boolean fake) {
        initializeComponents();
        if (fake) {
            logger.info("init fake instance");
        }
    }

    public static float FONT_SIZE = 16f;

    public MainForm() {
        // 2025/08/01 FONT SIZE 允许通过 CMD 设置
        initializeComponents();
        int inputFontSize = Application.startCmd.getFontSize();
        String input;
        float font;
        if (inputFontSize < 1) {
            // 2025/06/14 修复有时候找不到 DIALOG 的问题
            JDialog topDialog = new JDialog();
            topDialog.setAlwaysOnTop(true);
            topDialog.setModal(true);
            topDialog.setLocationRelativeTo(null);
            input = (String) JOptionPane.showInputDialog(
                    topDialog,
                    "请输入字体大小 (10-50)：",
                    "设置字体大小",
                    JOptionPane.PLAIN_MESSAGE,
                    IconManager.ausIcon,
                    null,
                    "16"
            );
            topDialog.dispose();
        } else {
            input = String.valueOf(inputFontSize);
        }
        if (input != null) {
            try {
                int size = Integer.parseInt(input.trim());
                if (size < 10 || size > 50) {
                    JOptionPane.showMessageDialog(null,
                            "字体大小必须在 10 到 50 之间！使用默认 16", "警告", JOptionPane.WARNING_MESSAGE);
                    font = 16;
                } else {
                    font = size;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "请输入有效的数字！使用默认 16", "错误", JOptionPane.ERROR_MESSAGE);
                font = 16;
            }
        } else {
            font = 16;
        }
        FONT_SIZE = font;
        logger.info("use font size {}", FONT_SIZE);

        logger.info("init main form");
        methodCallRadioButton.setSelected(true);
        fernRadio.setSelected(true);
        fernRadio.setText(DecompileEngine.INFO);
        searchStrText.setEnabled(false);
        deleteTempCheckBox.setSelected(true);
        LogUtil.setT(logArea);
        SyntaxAreaHelper.buildJava(codePanel);
        engineVal.setText("CLOSED");
        engineVal.setForeground(Color.RED);
        autoSaveCheckBox.setSelected(true);
        if (ConfigEngine.exist()) {
            config = ConfigEngine.parseConfig();
            resolveConfig();
        }
        fileTree.refresh();

        allMethodList.setCellRenderer(new AllMethodsRender());
        calleeList.setCellRenderer(new MethodCallRender());
        callerList.setCellRenderer(new MethodCallRender());
        searchList.setCellRenderer(new MethodCallRender());
        methodImplList.setCellRenderer(new MethodCallRender());
        superImplList.setCellRenderer(new MethodCallRender());
        springCList.setCellRenderer(new ClassRender());
        springMList.setCellRenderer(new SpringMethodRender());

        springIList.setCellRenderer(new ClassRender());
        servletList.setCellRenderer(new ClassRender());
        filterList.setCellRenderer(new ClassRender());
        listenerList.setCellRenderer(new ClassRender());
        springConfigList.setCellRenderer(new ClassRender());

        historyList.setCellRenderer(new MethodCallRender());
        favList.setCellRenderer(new MethodCallRender());
        leakResultList.setCellRenderer(new LeakResultRender());

        historyListData = new DefaultListModel<>();
        historyList.setModel(historyListData);
        favData = new DefaultListModel<>();
        favList.setModel(favData);

        logoLabel.setIcon(IconManager.showIcon);
        jreRuntimeLabel.setIcon(IconManager.javaIcon);
        dbPathLabel.setIcon(IconManager.dbIcon);
        startEngineButton.setIcon(IconManager.startIcon);
        openJDBtn.setIcon(IconManager.jdStartIcon);
        curJarLabel.setIcon(IconManager.jarIcon);
        curClassLabel.setIcon(IconManager.curIcon);
        curMethodLabel.setIcon(IconManager.curIcon);
        authorLabel.setIcon(IconManager.auIcon);
        authorTextLabel.setIcon(IconManager.githubIcon);
        classBlackListLabel.setIcon(IconManager.whiteIcon);
        classWhiteListLabel.setIcon(IconManager.whiteIcon);

        authorTextLabel.addMouseListener(new AuthorAdapter());

        blackArea.setText(Const.blackAreaText);
        classBlackArea.setText(Const.classBlackAreaText);
        classWhiteArea.setText(Const.classWhiteAreaText);

        likeSearchRadioButton.setSelected(true);

        VulnerabilityBuilder.build(this);

        showAllRadio.setSelected(true);
        showAllRadio.addActionListener(e -> {
            VulnerabilityBuilder.refreshAll();
            advancePanel.revalidate();
            advancePanel.repaint();
        });
        showHighRadio.addActionListener(e -> {
            VulnerabilityBuilder.refreshHigh();
            advancePanel.revalidate();
            advancePanel.repaint();
        });
        showMediumRadio.addActionListener(e -> {
            VulnerabilityBuilder.refreshMedium();
            advancePanel.revalidate();
            advancePanel.repaint();
        });
        showLowRadio.addActionListener(e -> {
            VulnerabilityBuilder.refreshLow();
            advancePanel.revalidate();
            advancePanel.repaint();
        });

        filterModeCombo.addItem("黑名单模式 (Black List) - 排除匹配项");
        filterModeCombo.addItem("白名单模式 (White List) - 仅保留匹配项");
        filterModeCombo.setSelectedIndex(0);

        quickSinkBtn.addActionListener(e -> tabbedPanel.setSelectedIndex(9));

        logger.info("init main form success");
    }

    private static void init() {
        FontHelper.installFont(FONT_SIZE);
        DropHelper.setDrop();
        CodeMenuHelper.run();

        ChoseJarAction.run();
        BuildAction.run();
        JarsInJarAction.run();
        CommonSearchAction.run();
        SearchAction.run();
        RuntimeJarAction.run();
        ASMAction.run();
        PluginsAction.run();
        SpringAction.run();
        CleanAction.run();
        ShowStringAction.run();
        TipsAction.run();
        HtmlGraph.run();

        SCAAction.register();
        LeakAction.register();
        MCPAction.register();

        Font codeFont = FontHelper.getFont(FONT_SIZE);
        instance.blackArea.setFont(codeFont);
        instance.classBlackArea.setFont(codeFont);
        instance.classWhiteArea.setFont(codeFont);

        codeArea.addKeyListener(new GlobalKeyListener());
        instance.allMethodList.addKeyListener(new GlobalKeyListener());
        // 安装全局键盘分发器，使双 Shift 不依赖焦点所在组件
        GlobalKeyListener.installGlobalKeyDispatcher();
        instance.fileTree.addKeyListener(new FileTreeKeyAdapter());
        instance.fileTree.addMouseListener(new TreeMouseAdapter());
        instance.fileTree.addMouseListener(new TreeRightMenuAdapter());
        instance.allMethodList.addMouseListener(new CommonMouseAdapter());
        instance.callerList.addMouseListener(new CommonMouseAdapter());
        instance.calleeList.addMouseListener(new CommonMouseAdapter());
        instance.methodImplList.addMouseListener(new CommonMouseAdapter());
        instance.superImplList.addMouseListener(new CommonMouseAdapter());
        instance.searchList.addMouseListener(new CommonMouseAdapter());
        instance.historyList.addMouseListener(new CommonMouseAdapter());
        instance.springCList.addMouseListener(new ControllerMouseAdapter());
        instance.springMList.addMouseListener(new CommonMouseAdapter());

        instance.springIList.addMouseListener(new ClassResultAdapter());
        instance.servletList.addMouseListener(new ClassResultAdapter());
        instance.filterList.addMouseListener(new ClassResultAdapter());
        instance.listenerList.addMouseListener(new ClassResultAdapter());
        instance.springConfigList.addMouseListener(new ClassResultAdapter());

        instance.getLeakResultList().addMouseListener(new LeakResultMouseAdapter());
        instance.favList.addMouseListener(new FavMouseAdapter());
        instance.fileTreeSearchTextField.getDocument().addDocumentListener(new SearchInputListener());
        instance.fileTreeSearchTextField.addKeyListener(new SearchTextFieldKeyAdapter());
        instance.fileTreeSearchTextField.addKeyListener(new FileTreeKeyAdapter());

        instance.getAddToFavoritesButton().addActionListener(e -> {
            if (curMethod != null) {
                getFavData().addElement(curMethod);
                MainForm.getEngine().addFav(curMethod);
                JOptionPane.showMessageDialog(instance.masterPanel, "add current method to favorite ok");
            } else {
                JOptionPane.showMessageDialog(instance.masterPanel, "current method is null");
            }
        });

        instance.exportSearchJsonBtn.addActionListener(e -> {
            List<MethodResult> results = new ArrayList<>();
            ListModel<MethodResult> model = instance.searchList.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                results.add(model.getElementAt(i));
            }
            Exporter exporter = new SearchResultJsonExporter(results);
            boolean success = exporter.doExport();
            if (success) {
                JOptionPane.showMessageDialog(instance.masterPanel,
                        "export success: " + exporter.getFileName());
            } else {
                JOptionPane.showMessageDialog(instance.masterPanel,
                        "no search results to export");
            }
        });

        instance.exportTxtBtn.addActionListener(e -> {
            Exporter exporter = new TxtExporter();
            boolean success = exporter.doExport();
            if (success) {
                String fileName = exporter.getFileName();
                JOptionPane.showMessageDialog(instance.masterPanel, "导出到 " + fileName);
            } else {
                JOptionPane.showMessageDialog(instance.masterPanel, "TXT 导出失败");
            }
        });
        instance.exportJsonBtn.addActionListener(e -> {
            Exporter exporter = new JsonExporter();
            boolean success = exporter.doExport();
            if (success) {
                String fileName = exporter.getFileName();
                JOptionPane.showMessageDialog(instance.masterPanel, "导出到 " + fileName);
            } else {
                JOptionPane.showMessageDialog(instance.masterPanel, "JSON 导出失败");
            }
        });
        instance.exportCsvBtn.addActionListener(e -> {
            Exporter exporter = new CsvExporter();
            boolean success = exporter.doExport();
            if (success) {
                String fileName = exporter.getFileName();
                JOptionPane.showMessageDialog(instance.masterPanel, "导出到 " + fileName);
            } else {
                JOptionPane.showMessageDialog(instance.masterPanel, "CSV 导出失败");
            }
        });

        instance.openJDBtn.addActionListener(e -> JDGUIStarter.start());

        refreshLang(false);
        MenuUtil.setLangFlag();

        updateIcon();

        GadgetUIBuilder.init(instance);

        Color elColor = new Color(198, 239, 189);
        instance.startELSearchButton.setIcon(SvgManager.SpringIcon);
        instance.springELStartButton.setIcon(SvgManager.SpringIcon);
        instance.startELSearchButton.setBackground(elColor);
        instance.springELStartButton.setBackground(elColor);
    }

    private static void updateIcon() {
        instance.getTabbedPanel().setIconAt(0, SvgManager.StartIcon);
        instance.getTabbedPanel().setIconAt(1, SvgManager.SearchIcon);
        instance.getTabbedPanel().setIconAt(2, SvgManager.ConnectIcon);
        instance.getTabbedPanel().setIconAt(3, SvgManager.InheritIcon);
        instance.getTabbedPanel().setIconAt(4, SvgManager.SpringIcon);
        instance.getTabbedPanel().setIconAt(5, SvgManager.NoteIcon);
        instance.getTabbedPanel().setIconAt(6, SvgManager.ScaIcon);
        instance.getTabbedPanel().setIconAt(7, SvgManager.LeakIcon);
        instance.getTabbedPanel().setIconAt(8, SvgManager.GadgetIcon);
        instance.getTabbedPanel().setIconAt(9, SvgManager.AdvanceIcon);
        instance.getTabbedPanel().setIconAt(10, SvgManager.DogIcon);
        if (instance.getTabbedPanel().getTabCount() > 11) {
            instance.getTabbedPanel().setIconAt(11, SvgManager.ConnectIcon);
        }

        instance.webTabbed.setIconAt(0, SvgManager.SpringIcon);
        instance.webTabbed.setIconAt(1, SvgManager.SpringIcon);
        instance.webTabbed.setIconAt(2, SvgManager.SpringIcon);
        instance.webTabbed.setIconAt(3, SvgManager.TomcatIcon);
        instance.webTabbed.setIconAt(4, SvgManager.TomcatIcon);
        instance.webTabbed.setIconAt(5, SvgManager.TomcatIcon);
        instance.getTabbedPanel().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        instance.webTabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public static void refreshLang(boolean checkConfig) {
        if (!checkConfig && config != null) {
            String lang = config.getLang();
            if (lang != null) {
                if (lang.equals("en")) {
                    GlobalOptions.setLang(GlobalOptions.ENGLISH);
                } else if (lang.equals("zh")) {
                    GlobalOptions.setLang(GlobalOptions.CHINESE);
                }
            }
        }
        try {
            if (GlobalOptions.getLang() == GlobalOptions.CHINESE) {
                instance.codePanel.setBorder(
                        BorderFactory.createTitledBorder(null,
                                "Java 反编译代码",
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                null, null));

                int c = instance.tabbedPanel.getTabCount();
                if (c != 12) {
                    throw new RuntimeException("tabbed panel error");
                }
                instance.tabbedPanel.setTitleAt(0, "开始");
                instance.tabbedPanel.setTitleAt(1, "搜索");
                instance.tabbedPanel.setTitleAt(2, "调用");
                instance.tabbedPanel.setTitleAt(3, "实现");
                instance.tabbedPanel.setTitleAt(4, "web");
                instance.tabbedPanel.setTitleAt(5, "记录");
                instance.tabbedPanel.setTitleAt(6, "SCA");
                instance.tabbedPanel.setTitleAt(7, "泄露");
                instance.tabbedPanel.setTitleAt(8, "GADGET");
                instance.tabbedPanel.setTitleAt(9, "高级");
                instance.tabbedPanel.setTitleAt(10, "漏洞链");
                instance.tabbedPanel.setTitleAt(11, "MCP");

                instance.chosePanel.setBorder(
                        BorderFactory.createTitledBorder(null,
                                "启动器",
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                null, null));
                instance.choseBtn.setText("选择文件/目录");
                instance.dbPathLabel.setText("数据库路径");
                instance.jreRuntimeLabel.setText("JRE 环境路径");
                instance.classBlackListLabel.setText("<html>\n" +
                        "类黑名单<br>" +
                        "(1) <font style=\"color: blue; font-weight: bold;\">com.a.</font> package<br>" +
                        "(2) <font style=\"color: blue; font-weight: bold;\">com.a.Test</font> full name" +
                        "</html>");
                instance.classWhiteListLabel.setText("<html>\n" +
                        "类白名单<br>" +
                        "(1) <font style=\"color: blue; font-weight: bold;\">com.a.</font> package<br>" +
                        "(2) <font style=\"color: blue; font-weight: bold;\">com.a.Test</font> full name" +
                        "</html>");
                instance.resolveJarsInJarCheckBox.setText("解决内嵌JAR问题");
                instance.autoSaveCheckBox.setText("自动保存");
                instance.deleteTempCheckBox.setText("在启动引擎前删除旧缓存");
                instance.autoFindRtJarCheckBox.setText("自动搜索RT.JAR");
                instance.addRtJarWhenCheckBox.setText("分析时添加RT.JAR");
                instance.startEngineButton.setText("启动");
                instance.openJDBtn.setText("JD-GUI");

                instance.infoPanel.setBorder(
                        BorderFactory.createTitledBorder(null,
                                "基本信息",
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                null, null));
                instance.engineLabel.setText("引擎状态");
                instance.databaseSizeLabel.setText("数据库");
                instance.totalJarLabel.setText("JAR数量");
                instance.totalClassLabel.setText("类数量");
                instance.totalMethodLabel.setText("方法数量");

                instance.decompilerPanel.setBorder(
                        BorderFactory.createTitledBorder(null,
                                "反编译相关",
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                null, null));
                instance.opcodeBtn.setText("显示方法字节码指令");
                instance.javaAsmBtn.setText("显示JAVA ASM代码");
                instance.fernRadio.setText("FernFlower (来自 jetbrains 的 IDEA 项目)");

                instance.analysis.setBorder(BorderFactory.createTitledBorder(null,
                        "深入分析",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        null, null));
                instance.cfgBtn.setText("展示控制流图");
                instance.frameBtn.setText("完整栈帧");
                instance.simpleFrameButton.setText("简易栈帧");

                instance.showStringListButton.setText("所有字符串");
                instance.cleanButton.setText("清除缓存");

                instance.curPanel.setBorder(BorderFactory.createTitledBorder(
                        null,
                        "当前",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        null, null));
                instance.curJarLabel.setText("当前JAR");
                instance.curClassLabel.setText("当前类");
                instance.curMethodLabel.setText("当前方法");

                instance.classBlackLabel.setText(" class / package filter list (split by ; and \\n) " +
                        "类名包名过滤列表 (按照 ; 和 \\n 分割)");
                instance.startSearchButton.setText("开始搜索");
                instance.springLabel.setText("分析 JAR/JARS 中的 Spring Controller/Mapping 信息");
                instance.pathSearchButton.setText("查找");
                instance.pathSearchLabel.setText("在所有 Mapping 中查找 Path");
                instance.refreshButton.setText("刷新全部");
                instance.javaVulLabel.setText("快速搜索通用 JAVA 漏洞相关");

                instance.sqliteLabel.setText("一个 SQLITE 查询工具");
                instance.encoderLabel.setText("一个编码解码加密解密工具");
                instance.listenerLabel.setText("一个 SOCKET 监听工具");
                instance.spelLabel.setText("一个 SPEL 表达式搜索工具");
                instance.startELSearchButton.setText("表达式搜索");
                instance.serUtilLabel.setText("一个分析 Java 序列化数据中字节码的工具");
                instance.bcelLabel.setText("一个分析 BCEL 字节码转为 Java 代码的工具");

                instance.scaTipLabel.setText(" 不建议一次分析开启多个模块");

                instance.addToFavoritesButton.setText("添加到收藏");
                instance.scaFileLabel.setText("JAR 文件/JAR 目录");
                instance.scaOutLabel.setText("输出类型");
                instance.outputFileLabel.setText("输出文件");
                instance.scaOpenBtn.setText("选择");
                instance.scaStartBtn.setText("开始");
                instance.scaResultOpenBtn.setText("打开");
                instance.scaOutConsoleRadio.setText("命令行输出");
                instance.scaOutHtmlRadio.setText("HTML文件");
                instance.htmlGraphBtn.setText("方法HTML图");
            } else if (GlobalOptions.getLang() == GlobalOptions.ENGLISH) {
                instance.codePanel.setBorder(
                        BorderFactory.createTitledBorder(null,
                                "Java Decompile Code",
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                null, null));

                int c = instance.tabbedPanel.getTabCount();
                if (c != 12) {
                    throw new RuntimeException("tabbed panel error");
                }
                instance.tabbedPanel.setTitleAt(0, "start");
                instance.tabbedPanel.setTitleAt(1, "search");
                instance.tabbedPanel.setTitleAt(2, "call");
                instance.tabbedPanel.setTitleAt(3, "impl");
                instance.tabbedPanel.setTitleAt(4, "web");
                instance.tabbedPanel.setTitleAt(5, "note");
                instance.tabbedPanel.setTitleAt(6, "sca");
                instance.tabbedPanel.setTitleAt(7, "leak");
                instance.tabbedPanel.setTitleAt(8, "gadget");
                instance.tabbedPanel.setTitleAt(9, "advance");
                instance.tabbedPanel.setTitleAt(10, "chains");
                instance.tabbedPanel.setTitleAt(11, "MCP");

                instance.chosePanel.setBorder(
                        BorderFactory.createTitledBorder(null,
                                "Starter",
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                null, null));
                instance.choseBtn.setText("Chose File / Dir");
                instance.dbPathLabel.setText("Database Path");
                instance.jreRuntimeLabel.setText("JRE Runtime");
                instance.classBlackListLabel.setText("<html>\n" +
                        "Class Black List<br>" +
                        "(1) <font style=\"color: blue; font-weight: bold;\">com.a.</font> package<br>" +
                        "(2) <font style=\"color: blue; font-weight: bold;\">com.a.Test</font> full name" +
                        "</html>");
                instance.classWhiteListLabel.setText("<html>\n" +
                        "Class White List<br>" +
                        "(1) <font style=\"color: blue; font-weight: bold;\">com.a.</font> package<br>" +
                        "(2) <font style=\"color: blue; font-weight: bold;\">com.a.Test</font> full name" +
                        "</html>");
                instance.resolveJarsInJarCheckBox.setText("Resolve Jars in Jar");
                instance.autoSaveCheckBox.setText("Auto Save");
                instance.deleteTempCheckBox.setText("Delete Temp Dir Before Build");
                instance.autoFindRtJarCheckBox.setText("Auto Find rt.jar");
                instance.addRtJarWhenCheckBox.setText("Add rt.jar to Analyze");
                instance.startEngineButton.setText("Start");
                instance.openJDBtn.setText("JD-GUI");

                instance.infoPanel.setBorder(
                        BorderFactory.createTitledBorder(null,
                                "Information",
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                null, null));
                instance.engineLabel.setText("Engine State");
                instance.databaseSizeLabel.setText("Database");
                instance.totalJarLabel.setText("Total Jar");
                instance.totalClassLabel.setText("Total Class");
                instance.totalMethodLabel.setText("Total Method");

                instance.decompilerPanel.setBorder(
                        BorderFactory.createTitledBorder(null,
                                "Decompiler",
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                null, null));
                instance.opcodeBtn.setText("Show Method Opcode");
                instance.javaAsmBtn.setText("Java ASM Code");
                instance.fernRadio.setText(" FernFlower (from jetbrains/intellij-community)");

                instance.analysis.setBorder(BorderFactory.createTitledBorder(null,
                        "Analysis",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        null, null));
                instance.cfgBtn.setText("Show CFG");
                instance.frameBtn.setText("Full Frame");
                instance.simpleFrameButton.setText("Simple Frame");

                instance.showStringListButton.setText("All Strings");
                instance.cleanButton.setText("Clean");

                instance.curPanel.setBorder(BorderFactory.createTitledBorder(
                        null,
                        "Current",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        null, null));
                instance.curJarLabel.setText("Jar");
                instance.curClassLabel.setText("Class");
                instance.curMethodLabel.setText("Method");

                instance.classBlackLabel.setText(" class / package filter list (split by ; and \\n) " +
                        "类名包名过滤列表 (按照 ; 和 \\n 分割)");
                instance.startSearchButton.setText("Start Search");
                instance.springLabel.setText(" Analyze Spring Controllers and Mappings in Jar/Jars");
                instance.pathSearchButton.setText("Search");
                instance.pathSearchLabel.setText(" Search path in all Mappings");
                instance.refreshButton.setText("Refresh All");
                instance.javaVulLabel.setText("Quickly Search Commons Java Vulnerabilities Call");

                instance.sqliteLabel.setText("A tool for run custom query in SQLite database");
                instance.encoderLabel.setText("A tool for encode/decode encrypt/decrypt operations");
                instance.listenerLabel.setText("A tool for listening port and send by socket");
                instance.spelLabel.setText("A tool for Spring EL search");
                instance.startELSearchButton.setText("EL Search");
                instance.serUtilLabel.setText("A tool for bytecodes in Java Serialization Data");
                instance.bcelLabel.setText("A tool for parse BCEL bytecode to Java code");

                instance.scaTipLabel.setText(" not recommended to enable multiple modules in one analysis");

                instance.addToFavoritesButton.setText("add to favorites");
                instance.scaFileLabel.setText("JAR FILE / JAR DIR");
                instance.scaOutLabel.setText("OUTPUT");
                instance.outputFileLabel.setText("OUTPUT FILE");
                instance.scaOpenBtn.setText("OPEN");
                instance.scaStartBtn.setText("START");
                instance.scaResultOpenBtn.setText("OPEN");
                instance.scaOutConsoleRadio.setText("CONSOLE");
                instance.scaOutHtmlRadio.setText("HTML");
                instance.htmlGraphBtn.setText("HTML Graph");
            } else {
                throw new RuntimeException("invalid language");
            }
        } catch (Exception ex) {
            logger.error("error: {}", ex);
        }
    }

    public static JFrame start() {
        UIHelper.setup();
        JFrame frame = new JFrame(Const.app);
        instance = new MainForm();

        init();

        initChains();

        frame.setJMenuBar(MenuUtil.createMenuBar());
        frame.setContentPane(instance.masterPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.setIconImage(IconManager.showIcon.getImage());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int resp = JOptionPane.showConfirmDialog(frame, "CONFIRM EXIT?",
                        "EXIT", JOptionPane.OK_CANCEL_OPTION);
                if (resp == JOptionPane.OK_OPTION) {
                    // Persist UI geometry synchronously so a fast exit
                    // can't lose the latest values to the debounced timer.
                    try {
                        UIPrefs.captureFrameGeometry(frame);
                        UIPrefs.save();
                    } catch (Exception ignored) {
                    }
                    frame.dispose();
                    System.exit(0);
                }
            }
        });

        LogUtil.info("###############################################");
        LogUtil.info("本项目是免费开源软件，不存在任何商业版本/收费版本");
        LogUtil.info("This project is free and open-source software");
        LogUtil.info("There are no commercial or paid versions");
        LogUtil.info("###############################################");

        LogCleanHelper.build();

        // 2025/10/12 不应该在启动时 init note/history
        // 会导致数据库被连接无法断开重新开启新任务
        // initNote();
        instance.loadFavBtn.addActionListener(e -> {
            initNote();
        });

        UpdateChecker.check();

        // 最后一步 处理 UI
        if (config != null && config.getTheme() != null) {
            if (config.getTheme().equals("dark")) {
                MenuUtil.useDark();
            }
            if (config.getTheme().equals("orange")) {
                MenuUtil.useOrange();
            }
        } else {
            MenuUtil.useDefault();
        }

        // 最后一步 处理字体
        getCodeArea().setFont(getCodeArea().getFont().deriveFont(FONT_SIZE));


        frame.pack();
        // Apply remembered geometry / split-divider positions before the
        // frame is shown so the user never sees a flicker. Falls back to
        // setLocationRelativeTo(null) when no preference exists yet.
        try {
            UIPrefs.load();
            boolean hadGeometry = UIPrefs.getInt(UIPrefs.K_FRAME_W) != null
                    && UIPrefs.getInt(UIPrefs.K_FRAME_H) != null;
            UIPrefs.applyFrameGeometry(frame);
            if (!hadGeometry) {
                frame.setLocationRelativeTo(null);
            }
            UIPrefs.installFrameListeners(frame);
            UIPrefs.bindSplit(instance.rootSplit, UIPrefs.K_SPLIT_ROOT);
            UIPrefs.bindSplit(instance.treeContentSplit, UIPrefs.K_SPLIT_TREE);
            UIPrefs.bindSplit(instance.coreSplit, UIPrefs.K_SPLIT_CORE);
        } catch (Exception ex) {
            logger.warn("apply ui prefs failed: {}", ex.toString());
            frame.setLocationRelativeTo(null);
        }
        frame.setResizable(true);
        frame.setVisible(false);
        return frame;
    }

    private static void initNote() {
        if (engine == null) {
            return;
        }
        // 2025/10/14 修复多次点击按钮导致数据重复问题
        favData.clear();
        historyListData.clear();
        // ADD
        ArrayList<MethodResult> favList = engine.getAllFavMethods();
        for (MethodResult m : favList) {
            favData.addElement(m);
        }
        ArrayList<MethodResult> hisList = engine.getAllHisMethods();
        for (MethodResult m : hisList) {
            historyListData.addElement(m);
        }
        // SET MODEL
        instance.favList.setModel(favData);
        instance.historyList.setModel(historyListData);
        // REFRESH
        instance.favList.repaint();
        instance.favList.revalidate();
        instance.historyList.repaint();
        instance.historyList.revalidate();
    }

    public static void setSink(String className, String methodName, String methodDesc) {
        instance.sinkClassText.setText(className);
        instance.sinkMethodText.setText(methodName);
        instance.sinkDescText.setText(methodDesc);
    }

    public static void setSource(String className, String methodName, String methodDesc) {
        instance.sourceClassText.setText(className);
        instance.sourceMethodText.setText(methodName);
        instance.sourceDescText.setText(methodDesc);
    }

    private static void closeProgressDialog(JDialog dialog) {
        if (dialog == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            dialog.dispose();
            dialog.setVisible(false);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            dialog.dispose();
            dialog.setVisible(false);
        });
    }

    private static void initChains() {
        instance.chainsLabel.setText("<html>" +
                "&nbsp;<font color='red'><b>漏洞利用链模块</b></font>（<font color='blue'><b>chains</b></font>）" +
                "使用&nbsp;<font color='red'><b>深度优先搜索算法</b></font>（<font color='blue'><b>DFS</b></font>）" +
                "尝试分析出所有可能的&nbsp;<font color='red'><b>漏洞链</b></font>" +
                "</html>");
        instance.chainsLabel.setIcon(SvgManager.DogIcon);
        instance.sinkRadio.setSelected(true);
        instance.maxDepthSpin.setValue(10);

        instance.sourceNullRadio.setSelected(false);
        instance.sourceEnableRadio.setSelected(true);

        instance.sinkClassText.setText("java/lang/Runtime");
        instance.sinkMethodText.setText("exec");
        instance.sinkDescText.setText("(Ljava/lang/String;)Ljava/lang/Process;");

        instance.clearBtn.addActionListener(e -> ((ChainsResultPanel) instance.chainsResult).clear());

        ChainsBuilder.buildBox(
                instance.sinkBox,
                instance.sinkClassText,
                instance.sinkMethodText,
                instance.sinkDescText);

        instance.dfsAdvanceBtn.addActionListener(e -> {
            DFSConfigDialog dialog = new DFSConfigDialog(
                    (JFrame) instance.getMasterPanel().getTopLevelAncestor(),
                    dfsMaxLimit,
                    dfsBlacklist
            );
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                dfsMaxLimit = dialog.getMaxLimit();
                dfsBlacklist = dialog.getBlacklist();
            }
        });

        // 2026/05/29 默认启用 Null Source 优化体验
        instance.sourceNullRadio.setSelected(true);

        instance.startChainsBtn.addActionListener(e -> {
            DFSEngine dfsEngine = new DFSEngine(
                    instance.chainsResult,
                    instance.sinkRadio.isSelected(),
                    instance.sourceNullRadio.isSelected(),
                    (Integer) instance.maxDepthSpin.getValue());
            dfsEngine.setMaxLimit(dfsMaxLimit);
            Set<String> blacklistSet = new HashSet<>();
            if (dfsBlacklist != null && !dfsBlacklist.trim().isEmpty()) {
                String[] lines = dfsBlacklist.split("\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    blacklistSet.add(line.trim());
                }
            }
            dfsEngine.setBlacklist(blacklistSet);
            dfsEngine.setSink(
                    instance.sinkClassText.getText(),
                    instance.sinkMethodText.getText(),
                    instance.sinkDescText.getText()
            );
            dfsEngine.setSource(
                    instance.sourceClassText.getText(),
                    instance.sourceMethodText.getText(),
                    instance.sourceDescText.getText()
            );
            JDialog dialog = ProcessDialog.createProgressDialog(instance.getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            new Thread(() -> {
                List<DFSResult> resultList;
                try {
                    dfsEngine.doAnalyze();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    resultList = dfsEngine.getResults();
                    DFSUtil.save(resultList);
                    TaintCache.dfsCache.clear();
                    TaintCache.dfsCache.addAll(resultList);
                    SwingUtilities.invokeLater(() -> instance.getTabbedPanel().setSelectedIndex(10));
                } catch (Throwable ex) {
                    logger.error("chains dfs analyze failed", ex);
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(instance.getMasterPanel(),
                                    "chains dfs analyze failed: " + ex.getMessage()));
                    return;
                } finally {
                    closeProgressDialog(dialog);
                }
                if (instance.getTaintBox().isSelected()) {
                    // 弹框提醒用户即将开始污点分析验证
                    int result = JOptionPane.showConfirmDialog(
                            instance.getMasterPanel().getTopLevelAncestor(),
                            "即将对 DFS 结果开始污点分析验证，此过程可能需要一些时间。\n是否继续？",
                            "污点分析确认",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    // 如果用户选择取消，直接返回
                    if (result != JOptionPane.YES_OPTION) {
                        logger.info("user cancelled taint analysis");
                        return;
                    }

                    logger.info("start taint analyze");
                    JDialog taintDialog = ProcessDialog.createProgressDialog(instance.getMasterPanel());
                    new Thread(() -> taintDialog.setVisible(true)).start();
                    try {
                        List<TaintResult> taintResult = TaintAnalyzer.analyze(resultList);
                        TaintCache.cache.clear();
                        TaintCache.cache.addAll(taintResult);
                        // 显示污点分析结果的详细GUI窗体
                        TaintResultDialog.showTaintResults(instance.getMasterPanel().getTopLevelAncestor() instanceof Frame ?
                                (Frame) instance.getMasterPanel().getTopLevelAncestor() : null, new ArrayList<>(TaintCache.cache));
                    } finally {
                        closeProgressDialog(taintDialog);
                    }
                }
            }).start();
        });

        instance.startTaintBtn.addActionListener(e -> {
            if (TaintCache.dfsCache.isEmpty()) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "请确保 DFS 漏洞链分析有结果");
                return;
            }
            List<TaintResult> taintResult = TaintAnalyzer.analyze(new ArrayList<>(TaintCache.dfsCache));
            TaintCache.cache.clear();
            TaintCache.cache.addAll(taintResult);
            TaintResultDialog.showTaintResults(instance.getMasterPanel().getTopLevelAncestor() instanceof Frame ?
                    (Frame) instance.getMasterPanel().getTopLevelAncestor() : null, new ArrayList<>(TaintCache.cache));
        });
    }

    private void resolveConfig() {
        if (config != null) {
            String temp = config.getTempPath();
            String db = config.getDbPath();
            if (temp == null || db == null) {
                return;
            }
            if (Files.exists(Paths.get(temp)) && Files.exists(Paths.get(db))) {
                databaseSizeVal.setText(config.getDbSize());
                totalClassVal.setText(config.getTotalClass());
                totalJarVal.setText(config.getTotalJar());
                totalMethodVal.setText(config.getTotalMethod());
                fileText.setText(config.getJarPath());
                loadDBText.setText(config.getDbPath());

                engine = new CoreEngine(config);
                engineVal.setText("RUNNING");
                engineVal.setForeground(Color.GREEN);
                buildBar.setValue(100);
            } else {
                try {
                    Files.delete(Paths.get(ConfigEngine.CONFIG_FILE_PATH));
                } catch (Exception ignored) {
                }
                try {
                    DirUtil.removeDir(new File(Const.tempDir));
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void initializeComponents() {
        createCustomComponents();
        masterPanel = new JPanel();
        SwingLayout.configureGrid(masterPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        rootSplit = new JSplitPane();
        rootSplit.setDividerLocation(254);
        SwingLayout.add(masterPanel, rootSplit, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, new Dimension(200, 200), null, 0);
        leftPanel = new JPanel();
        SwingLayout.configureGrid(leftPanel, 2, 1, new Insets(0, 0, 0, 0), -1, -1);
        rootSplit.setLeftComponent(leftPanel);
        treeScrollPanel = new JScrollPane();
        SwingLayout.add(leftPanel, treeScrollPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(250, 800), new Dimension(250, 800), null, 0);
        fileTree = new FileTree();
        treeScrollPanel.setViewportView(fileTree);
        fileTreeSearchPanel = new JPanel();
        SwingLayout.configureGrid(fileTreeSearchPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        fileTreeSearchPanel.setVisible(false);
        SwingLayout.add(leftPanel, fileTreeSearchPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(250, -1), new Dimension(250, -1), null, 0);
        searchFileNamePanel = new JPanel();
        SwingLayout.configureGrid(searchFileNamePanel, 2, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(fileTreeSearchPanel, searchFileNamePanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(248, -1), new Dimension(248, -1), null, 0);
        searchFileNamePanel.setBorder(BorderFactory.createTitledBorder(null, "File Name (press 'ENTER' to next)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        fileTreeSearchTextField = new JTextField();
        fileTreeSearchTextField.setVisible(true);
        SwingLayout.add(searchFileNamePanel, fileTreeSearchTextField, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(200, -1), new Dimension(200, -1), null, 0);
        fileTreeSearchLabel = new JLabel();
        fileTreeSearchLabel.setText("");
        fileTreeSearchLabel.setVisible(false);
        SwingLayout.add(searchFileNamePanel, fileTreeSearchLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, new Dimension(246, -1), new Dimension(246, -1), null, 0);
        treeContentSplit = new JSplitPane();
        treeContentSplit.setDividerLocation(760);
        treeContentSplit.setOrientation(0);
        rootSplit.setRightComponent(treeContentSplit);
        coreSplit = new JSplitPane();
        coreSplit.setDividerLocation(531);
        coreSplit.setResizeWeight(0.8);
        treeContentSplit.setLeftComponent(coreSplit);
        codePanel = new JPanel();
        SwingLayout.configureGrid(codePanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        coreSplit.setLeftComponent(codePanel);
        codePanel.setBorder(BorderFactory.createTitledBorder(null, "Java Decompile Code", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        coreRightSplit = new JPanel();
        SwingLayout.configureGrid(coreRightSplit, 3, 1, new Insets(0, 0, 0, 0), -1, -1);
        coreSplit.setRightComponent(coreRightSplit);
        tabbedPanel = new JTabbedPane();
        SwingLayout.add(coreRightSplit, tabbedPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        startPanel = new JPanel();
        SwingLayout.configureGrid(startPanel, 3, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("start", startPanel);
        chosePanel = new JPanel();
        SwingLayout.configureGrid(chosePanel, 9, 7, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(startPanel, chosePanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        chosePanel.setBorder(BorderFactory.createTitledBorder(null, "Starter", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        choseBtn = new JButton();
        choseBtn.setText("Chose File / Dir");
        SwingLayout.add(chosePanel, choseBtn, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        fileText = new JTextField();
        fileText.setEditable(false);
        fileText.setText("");
        SwingLayout.add(chosePanel, fileText, 0, 1, 1, 6, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        resolveJarsInJarCheckBox = new JCheckBox();
        resolveJarsInJarCheckBox.setText("Resolve Jars in Jar");
        SwingLayout.add(chosePanel, resolveJarsInJarCheckBox, 5, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        buildBar = new JProgressBar();
        buildBar.setForeground(new Color(-9524737));
        buildBar.setStringPainted(true);
        SwingLayout.add(chosePanel, buildBar, 7, 0, 1, 7, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        autoSaveCheckBox = new JCheckBox();
        autoSaveCheckBox.setText("Auto Save");
        SwingLayout.add(chosePanel, autoSaveCheckBox, 5, 1, 1, 4, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        dbPathLabel = new JLabel();
        dbPathLabel.setText("Database Path");
        SwingLayout.add(chosePanel, dbPathLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        loadDBText = new JTextField();
        loadDBText.setEditable(false);
        SwingLayout.add(chosePanel, loadDBText, 1, 1, 1, 6, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        jreRuntimeLabel = new JLabel();
        jreRuntimeLabel.setText("JRE Runtime");
        SwingLayout.add(chosePanel, jreRuntimeLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        rtText = new JTextField();
        rtText.setEditable(false);
        SwingLayout.add(chosePanel, rtText, 2, 1, 1, 6, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        autoFindRtJarCheckBox = new JCheckBox();
        autoFindRtJarCheckBox.setText("Auto Find rt.jar");
        SwingLayout.add(chosePanel, autoFindRtJarCheckBox, 6, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        addRtJarWhenCheckBox = new JCheckBox();
        addRtJarWhenCheckBox.setText("Add rt.jar to Analyze");
        SwingLayout.add(chosePanel, addRtJarWhenCheckBox, 6, 1, 1, 4, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        classBlackListLabel = new JLabel();
        classBlackListLabel.setText("Class Black List");
        SwingLayout.add(chosePanel, classBlackListLabel, 4, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        classBlackPanel = new JScrollPane();
        SwingLayout.add(chosePanel, classBlackPanel, 4, 1, 1, 6, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(-1, 75), new Dimension(-1, 75), new Dimension(-1, 75), 0);
        classBlackArea = new JTextArea();
        classBlackArea.setBackground(new Color(-12895429));
        classBlackArea.setForeground(new Color(-16711931));
        classBlackArea.setLineWrap(true);
        classBlackArea.setRows(0);
        classBlackPanel.setViewportView(classBlackArea);
        classWhiteListLabel = new JLabel();
        classWhiteListLabel.setText("Class White List");
        SwingLayout.add(chosePanel, classWhiteListLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        classWhitePanel = new JScrollPane();
        SwingLayout.add(chosePanel, classWhitePanel, 3, 1, 1, 6, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(-1, 75), new Dimension(-1, 75), new Dimension(-1, 75), 0);
        classWhiteArea = new JTextArea();
        classWhiteArea.setBackground(new Color(-12895429));
        classWhiteArea.setForeground(new Color(-853761));
        classWhiteArea.setLineWrap(true);
        classWhiteArea.setRows(0);
        classWhiteArea.setText("");
        classWhitePanel.setViewportView(classWhiteArea);
        decompilerPanel = new JPanel();
        SwingLayout.configureGrid(decompilerPanel, 2, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(chosePanel, decompilerPanel, 8, 0, 1, 7, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        decompilerPanel.setBorder(BorderFactory.createTitledBorder(null, "Decompiler", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        fernRadio = new JRadioButton();
        fernRadio.setEnabled(true);
        fernRadio.setSelected(true);
        fernRadio.setText(" FernFlower (from jetbrains/intellij-community)");
        SwingLayout.add(decompilerPanel, fernRadio, 1, 0, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        opcodeBtn = new JButton();
        opcodeBtn.setText("Show Method Opcode");
        SwingLayout.add(decompilerPanel, opcodeBtn, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        javaAsmBtn = new JButton();
        javaAsmBtn.setText("Java ASM Code");
        SwingLayout.add(decompilerPanel, javaAsmBtn, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        enginePanel = new JPanel();
        SwingLayout.configureGrid(enginePanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(chosePanel, enginePanel, 6, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        startEngineButton = new JButton();
        startEngineButton.setText("Start");
        SwingLayout.add(enginePanel, startEngineButton, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        openJDBtn = new JButton();
        openJDBtn.setText("JD-GUI");
        SwingLayout.add(enginePanel, openJDBtn, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        deleteTempCheckBox = new JCheckBox();
        deleteTempCheckBox.setText("Delete Temp Dir Before Build");
        SwingLayout.add(chosePanel, deleteTempCheckBox, 5, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        infoPanel = new JPanel();
        SwingLayout.configureGrid(infoPanel, 5, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(startPanel, infoPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        infoPanel.setBorder(BorderFactory.createTitledBorder(null, "Information", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalClassLabel = new JLabel();
        totalClassLabel.setText("Total Class");
        SwingLayout.add(infoPanel, totalClassLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        totalClassVal = new JLabel();
        totalClassVal.setText("0");
        SwingLayout.add(infoPanel, totalClassVal, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        totalMethodLabel = new JLabel();
        totalMethodLabel.setText("Total Method");
        SwingLayout.add(infoPanel, totalMethodLabel, 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        totalMethodVal = new JLabel();
        totalMethodVal.setText("0");
        SwingLayout.add(infoPanel, totalMethodVal, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        totalJarLabel = new JLabel();
        totalJarLabel.setText("Total Jar");
        SwingLayout.add(infoPanel, totalJarLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        totalJarVal = new JLabel();
        totalJarVal.setText("0");
        SwingLayout.add(infoPanel, totalJarVal, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        databaseSizeLabel = new JLabel();
        databaseSizeLabel.setText("Database");
        SwingLayout.add(infoPanel, databaseSizeLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        databaseSizeVal = new JLabel();
        databaseSizeVal.setText("0 MB");
        SwingLayout.add(infoPanel, databaseSizeVal, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        engineLabel = new JLabel();
        engineLabel.setText("Engine State");
        SwingLayout.add(infoPanel, engineLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        engineVal = new JLabel();
        engineVal.setText("CLOSED");
        SwingLayout.add(infoPanel, engineVal, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        authorPanel = new JPanel();
        SwingLayout.configureGrid(authorPanel, 2, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(infoPanel, authorPanel, 0, 2, 5, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0);
        authorLabel = new JLabel();
        authorLabel.setText("");
        SwingLayout.add(authorPanel, authorLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        authorTextLabel = new JLabel();
        authorTextLabel.setText("4ra1n");
        SwingLayout.add(authorPanel, authorTextLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        actionPanel = new JPanel();
        SwingLayout.configureGrid(actionPanel, 1, 4, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(startPanel, actionPanel, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        actionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        cleanButton = new JButton();
        cleanButton.setText("Clean");
        SwingLayout.add(actionPanel, cleanButton, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        showStringListButton = new JButton();
        showStringListButton.setText("All Strings");
        SwingLayout.add(actionPanel, showStringListButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        startELSearchButton = new JButton();
        startELSearchButton.setText("EL Search");
        SwingLayout.add(actionPanel, startELSearchButton, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        quickSinkBtn = new JButton();
        quickSinkBtn.setText("一键搜 Sink");
        SwingLayout.add(actionPanel, quickSinkBtn, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        searchResPanel = new JPanel();
        SwingLayout.configureGrid(searchResPanel, 2, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("search", searchResPanel);
        searchScroll = new JScrollPane();
        SwingLayout.add(searchResPanel, searchScroll, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        searchList = new JList();
        searchScroll.setViewportView(searchList);
        searchOptionsPanel = new JPanel();
        SwingLayout.configureGrid(searchOptionsPanel, 4, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(searchResPanel, searchOptionsPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        searchOptionsPanel.setBorder(BorderFactory.createTitledBorder(null, "Search Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        methodDefinitionRadioButton = new JRadioButton();
        methodDefinitionRadioButton.setText("method definition (方法定义)");
        SwingLayout.add(searchOptionsPanel, methodDefinitionRadioButton, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        methodCallRadioButton = new JRadioButton();
        methodCallRadioButton.setText("method call (方法调用)");
        SwingLayout.add(searchOptionsPanel, methodCallRadioButton, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        stringContainsRadioButton = new JRadioButton();
        stringContainsRadioButton.setText("string contains (包含字符串)");
        SwingLayout.add(searchOptionsPanel, stringContainsRadioButton, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        binarySearchRadioButton = new JRadioButton();
        binarySearchRadioButton.setText("binary search (二进制包含)");
        SwingLayout.add(searchOptionsPanel, binarySearchRadioButton, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        searchInnerPanel = new JPanel();
        SwingLayout.configureGrid(searchInnerPanel, 3, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(searchOptionsPanel, searchInnerPanel, 3, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        searchClassText = new JTextField();
        SwingLayout.add(searchInnerPanel, searchClassText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        searchClassLabel = new JLabel();
        searchClassLabel.setText("Class");
        SwingLayout.add(searchInnerPanel, searchClassLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        searchMethodLabel = new JLabel();
        searchMethodLabel.setText("Method");
        SwingLayout.add(searchInnerPanel, searchMethodLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        searchMethodText = new JTextField();
        SwingLayout.add(searchInnerPanel, searchMethodText, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        searchStrLabel = new JLabel();
        searchStrLabel.setText("String");
        SwingLayout.add(searchInnerPanel, searchStrLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        searchStrText = new JTextField();
        SwingLayout.add(searchInnerPanel, searchStrText, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        startSearchButton = new JButton();
        startSearchButton.setText("Start Search");
        SwingLayout.add(searchInnerPanel, startSearchButton, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        exportSearchJsonBtn = new JButton();
        exportSearchJsonBtn.setText("导出");
        exportSearchJsonBtn.setToolTipText("Export current search results as JSON");
        SwingLayout.add(searchInnerPanel, exportSearchJsonBtn, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(72, -1), null, 0);
        logoLabel = new JLabel();
        logoLabel.setText("");
        SwingLayout.add(searchInnerPanel, logoLabel, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        soPanel = new JPanel();
        SwingLayout.configureGrid(soPanel, 3, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(searchOptionsPanel, soPanel, 2, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        soPanel.setBorder(BorderFactory.createTitledBorder(null, "Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        likeSearchRadioButton = new JRadioButton();
        likeSearchRadioButton.setText("like search (模糊搜索模式)");
        SwingLayout.add(soPanel, likeSearchRadioButton, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        equalsSearchRadioButton = new JRadioButton();
        equalsSearchRadioButton.setText("equals search (精确搜索模式)");
        SwingLayout.add(soPanel, equalsSearchRadioButton, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        blackListPanel = new JPanel();
        SwingLayout.configureGrid(blackListPanel, 3, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(soPanel, blackListPanel, 2, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        blackScroll = new JScrollPane();
        SwingLayout.add(blackListPanel, blackScroll, 2, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(-1, 100), new Dimension(-1, 100), new Dimension(-1, 100), 0);
        blackScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        blackArea = new JTextArea();
        blackArea.setBackground(new Color(-12895429));
        Font blackAreaFont = this.resolveFont("Consolas", -1, -1, blackArea.getFont());
        if (blackAreaFont != null) blackArea.setFont(blackAreaFont);
        blackArea.setForeground(new Color(-16711931));
        blackScroll.setViewportView(blackArea);
        classBlackLabel = new JLabel();
        classBlackLabel.setText(" class / package black list (split by ; and \\n) 类名包名黑名单 (按照 ; 和 \\n 分割)");
        SwingLayout.add(blackListPanel, classBlackLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        filterModeCombo = new JComboBox();
        SwingLayout.add(blackListPanel, filterModeCombo, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        npbPanel = new JPanel();
        SwingLayout.configureGrid(npbPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(soPanel, npbPanel, 1, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        nullParamBox = new JCheckBox();
        nullParamBox.setText("except null parameter method / 排除空参方法 (空参方法一般无漏洞)");
        SwingLayout.add(npbPanel, nullParamBox, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        callPanel = new JPanel();
        SwingLayout.configureGrid(callPanel, 2, 1, new Insets(3, 3, 3, 3), -1, -1);
        tabbedPanel.addTab("call", callPanel);
        callerScroll = new JScrollPane();
        SwingLayout.add(callPanel, callerScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        callerScroll.setBorder(BorderFactory.createTitledBorder(null, "Caller", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        callerList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        callerList.setModel(defaultListModel1);
        callerScroll.setViewportView(callerList);
        calleeScroll = new JScrollPane();
        SwingLayout.add(callPanel, calleeScroll, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        calleeScroll.setBorder(BorderFactory.createTitledBorder(null, "Callee", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        calleeList = new JList();
        calleeScroll.setViewportView(calleeList);
        methodImplPanel = new JPanel();
        SwingLayout.configureGrid(methodImplPanel, 2, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("impl", methodImplPanel);
        implScroll = new JScrollPane();
        SwingLayout.add(methodImplPanel, implScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        implScroll.setBorder(BorderFactory.createTitledBorder(null, "Method Impl", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        methodImplList = new JList();
        implScroll.setViewportView(methodImplList);
        superImplScroll = new JScrollPane();
        SwingLayout.add(methodImplPanel, superImplScroll, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        superImplScroll.setBorder(BorderFactory.createTitledBorder(null, "Super Impl", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        superImplList = new JList();
        superImplScroll.setViewportView(superImplList);
        webPanel = new JPanel();
        SwingLayout.configureGrid(webPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("web", webPanel);
        webTabbed = new JTabbedPane();
        SwingLayout.add(webPanel, webTabbed, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, new Dimension(200, 200), null, 0);
        springPanel = new JPanel();
        SwingLayout.configureGrid(springPanel, 2, 1, new Insets(0, 0, 0, 0), -1, -1);
        webTabbed.addTab("spring controller", springPanel);
        springCPanel = new JPanel();
        SwingLayout.configureGrid(springCPanel, 4, 3, new Insets(3, 0, 0, 3), -1, -1);
        SwingLayout.add(springPanel, springCPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        scScroll = new JScrollPane();
        SwingLayout.add(springCPanel, scScroll, 3, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        springCList = new JList();
        scScroll.setViewportView(springCList);
        refreshButton = new JButton();
        refreshButton.setText("Refresh All");
        SwingLayout.add(springCPanel, refreshButton, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        springLabel = new JLabel();
        springLabel.setText(" Analyze Spring Controllers and Mappings in Jar/Jars");
        SwingLayout.add(springCPanel, springLabel, 1, 0, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        pathSearchButton = new JButton();
        pathSearchButton.setText("Search");
        SwingLayout.add(springCPanel, pathSearchButton, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        pathSearchLabel = new JLabel();
        pathSearchLabel.setText(" Search path in all Mappings");
        SwingLayout.add(springCPanel, pathSearchLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        pathSearchTextField = new JTextField();
        pathSearchTextField.setToolTipText("");
        SwingLayout.add(springCPanel, pathSearchTextField, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        exportPanel = new JPanel();
        SwingLayout.configureGrid(exportPanel, 1, 5, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(springCPanel, exportPanel, 0, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        exportJsonBtn = new JButton();
        exportJsonBtn.setText("导出为 JSON");
        SwingLayout.add(exportPanel, exportJsonBtn, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        final Component spacer1 = Box.createGlue();
        SwingLayout.add(exportPanel, spacer1, 0, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        exportTxtBtn = new JButton();
        exportTxtBtn.setText("导出为 TXT");
        SwingLayout.add(exportPanel, exportTxtBtn, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        exportCsvBtn = new JButton();
        exportCsvBtn.setText("导出为 CSV");
        SwingLayout.add(exportPanel, exportCsvBtn, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        exportAllLabel = new JLabel();
        exportAllLabel.setText(" Export All Data / 导出所有结果");
        SwingLayout.add(exportPanel, exportAllLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        springMPanel = new JPanel();
        SwingLayout.configureGrid(springMPanel, 1, 1, new Insets(0, 0, 0, 3), -1, -1);
        SwingLayout.add(springPanel, springMPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        smScroll = new JScrollPane();
        SwingLayout.add(springMPanel, smScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        springMList = new JList();
        smScroll.setViewportView(springMList);
        springIPanel = new JPanel();
        SwingLayout.configureGrid(springIPanel, 1, 1, new Insets(3, 3, 3, 3), -1, -1);
        webTabbed.addTab("spring interceptor", springIPanel);
        springIScroll = new JScrollPane();
        SwingLayout.add(springIPanel, springIScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        springIList = new JList();
        springIScroll.setViewportView(springIList);
        springConfigPanel = new JPanel();
        SwingLayout.configureGrid(springConfigPanel, 1, 1, new Insets(3, 3, 3, 3), -1, -1);
        webTabbed.addTab("spring config", springConfigPanel);
        springConfigScroll = new JScrollPane();
        SwingLayout.add(springConfigPanel, springConfigScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        springConfigList = new JList();
        springConfigScroll.setViewportView(springConfigList);
        servletPanel = new JPanel();
        SwingLayout.configureGrid(servletPanel, 1, 1, new Insets(3, 3, 3, 3), -1, -1);
        webTabbed.addTab("servlet", servletPanel);
        servletScroll = new JScrollPane();
        SwingLayout.add(servletPanel, servletScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        servletList = new JList();
        servletScroll.setViewportView(servletList);
        filterPanel = new JPanel();
        SwingLayout.configureGrid(filterPanel, 1, 1, new Insets(3, 3, 3, 3), -1, -1);
        webTabbed.addTab("filter", filterPanel);
        filterScroll = new JScrollPane();
        SwingLayout.add(filterPanel, filterScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        filterList = new JList();
        filterScroll.setViewportView(filterList);
        listenerPanel = new JPanel();
        SwingLayout.configureGrid(listenerPanel, 1, 1, new Insets(3, 3, 3, 3), -1, -1);
        webTabbed.addTab("listener", listenerPanel);
        listenerScroll = new JScrollPane();
        SwingLayout.add(listenerPanel, listenerScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        listenerList = new JList();
        listenerScroll.setViewportView(listenerList);
        notePanel = new JPanel();
        SwingLayout.configureGrid(notePanel, 3, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("note", notePanel);
        hisScroll = new JScrollPane();
        SwingLayout.add(notePanel, hisScroll, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        hisScroll.setBorder(BorderFactory.createTitledBorder(null, "history", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        historyList = new JList();
        final DefaultListModel defaultListModel2 = new DefaultListModel();
        historyList.setModel(defaultListModel2);
        hisScroll.setViewportView(historyList);
        favScroll = new JScrollPane();
        SwingLayout.add(notePanel, favScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        favScroll.setBorder(BorderFactory.createTitledBorder(null, "favorites", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        favList = new JList();
        favScroll.setViewportView(favList);
        favHisPanel = new JPanel();
        SwingLayout.configureGrid(favHisPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(notePanel, favHisPanel, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        loadFavBtn = new JButton();
        loadFavBtn.setText("加载历史 favorites/history");
        SwingLayout.add(favHisPanel, loadFavBtn, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        final Component spacer2 = Box.createGlue();
        SwingLayout.add(favHisPanel, spacer2, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        scaPanel = new JPanel();
        SwingLayout.configureGrid(scaPanel, 3, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("sca", scaPanel);
        modulePanel = new JPanel();
        SwingLayout.configureGrid(modulePanel, 2, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(scaPanel, modulePanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Module", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scaLog4jBox = new JCheckBox();
        scaLog4jBox.setText("Apache Log4j2");
        SwingLayout.add(modulePanel, scaLog4jBox, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        scaShiroBox = new JCheckBox();
        scaShiroBox.setText("Apache Shiro");
        SwingLayout.add(modulePanel, scaShiroBox, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        scaTipPanel = new JPanel();
        SwingLayout.configureGrid(scaTipPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(modulePanel, scaTipPanel, 0, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        scaTipLabel = new JLabel();
        scaTipLabel.setText(" not recommended to enable multiple modules in one analysis");
        SwingLayout.add(scaTipPanel, scaTipLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        scaFastjsonBox = new JCheckBox();
        scaFastjsonBox.setText("FASTJSON");
        SwingLayout.add(modulePanel, scaFastjsonBox, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        final Component spacer3 = Box.createGlue();
        SwingLayout.add(scaPanel, spacer3, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, false, true, null, null, null, 0);
        scaActionPanel = new JPanel();
        SwingLayout.configureGrid(scaActionPanel, 4, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(scaPanel, scaActionPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        scaActionPanel.setBorder(BorderFactory.createTitledBorder(null, "Action", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scaFileLabel = new JLabel();
        scaFileLabel.setText("JAR FILE / JAR DIR");
        SwingLayout.add(scaActionPanel, scaFileLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        scaFileText = new JTextField();
        SwingLayout.add(scaActionPanel, scaFileText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        scaOpenBtn = new JButton();
        scaOpenBtn.setText("OPEN");
        SwingLayout.add(scaActionPanel, scaOpenBtn, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        scanConsoleScroll = new JScrollPane();
        SwingLayout.add(scaActionPanel, scanConsoleScroll, 3, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        scanConsoleScroll.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scaConsoleArea = new JTextArea();
        scaConsoleArea.setBackground(new Color(-12895429));
        scaConsoleArea.setEditable(false);
        scaConsoleArea.setForeground(new Color(-16711931));
        scaConsoleArea.setLineWrap(true);
        scaConsoleArea.setRows(20);
        scanConsoleScroll.setViewportView(scaConsoleArea);
        scaOutLabel = new JLabel();
        scaOutLabel.setText("OUTPUT");
        SwingLayout.add(scaActionPanel, scaOutLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        scaOutPanel = new JPanel();
        SwingLayout.configureGrid(scaOutPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(scaActionPanel, scaOutPanel, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        scaOutConsoleRadio = new JRadioButton();
        scaOutConsoleRadio.setText("CONSOLE");
        SwingLayout.add(scaOutPanel, scaOutConsoleRadio, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        scaOutHtmlRadio = new JRadioButton();
        scaOutHtmlRadio.setText("HTML");
        SwingLayout.add(scaOutPanel, scaOutHtmlRadio, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        scaStartBtn = new JButton();
        scaStartBtn.setText("START");
        SwingLayout.add(scaActionPanel, scaStartBtn, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        outputFileLabel = new JLabel();
        outputFileLabel.setText("OUTPUT FILE");
        SwingLayout.add(scaActionPanel, outputFileLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        outputFileText = new JTextField();
        SwingLayout.add(scaActionPanel, outputFileText, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        scaResultOpenBtn = new JButton();
        scaResultOpenBtn.setText("OPEN");
        SwingLayout.add(scaActionPanel, scaResultOpenBtn, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        leakPanel = new JPanel();
        SwingLayout.configureGrid(leakPanel, 4, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("leak", leakPanel);
        leakRulesPanel = new JPanel();
        SwingLayout.configureGrid(leakRulesPanel, 5, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(leakPanel, leakRulesPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        leakRulesPanel.setBorder(BorderFactory.createTitledBorder(null, "Rules", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        leakUrlBox = new JCheckBox();
        leakUrlBox.setText("Url Info");
        SwingLayout.add(leakRulesPanel, leakUrlBox, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakJdbcBox = new JCheckBox();
        leakJdbcBox.setText("JDBC Connection");
        SwingLayout.add(leakRulesPanel, leakJdbcBox, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakFileBox = new JCheckBox();
        leakFileBox.setText("File Path");
        SwingLayout.add(leakRulesPanel, leakFileBox, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakJWTBox = new JCheckBox();
        leakJWTBox.setText("JWT");
        SwingLayout.add(leakRulesPanel, leakJWTBox, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakMacBox = new JCheckBox();
        leakMacBox.setText("MAC Address");
        SwingLayout.add(leakRulesPanel, leakMacBox, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakIpBox = new JCheckBox();
        leakIpBox.setText("IP Address");
        SwingLayout.add(leakRulesPanel, leakIpBox, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakPhoneBox = new JCheckBox();
        leakPhoneBox.setText("Phone Number");
        SwingLayout.add(leakRulesPanel, leakPhoneBox, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakIdBox = new JCheckBox();
        leakIdBox.setText("ID Card");
        SwingLayout.add(leakRulesPanel, leakIdBox, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakEmailBox = new JCheckBox();
        leakEmailBox.setText("Email");
        SwingLayout.add(leakRulesPanel, leakEmailBox, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        AKSKCheckBox = new JCheckBox();
        AKSKCheckBox.setText("AK SK");
        SwingLayout.add(leakRulesPanel, AKSKCheckBox, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        bankCardCheckBox = new JCheckBox();
        bankCardCheckBox.setText("Bank Card");
        SwingLayout.add(leakRulesPanel, bankCardCheckBox, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        APIKeyCheckBox = new JCheckBox();
        APIKeyCheckBox.setText("API Key");
        SwingLayout.add(leakRulesPanel, APIKeyCheckBox, 3, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        cryptoKeyCheckBox = new JCheckBox();
        cryptoKeyCheckBox.setText("Crypto Key");
        SwingLayout.add(leakRulesPanel, cryptoKeyCheckBox, 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        AIKeyCheckBox = new JCheckBox();
        AIKeyCheckBox.setText("AI Key");
        SwingLayout.add(leakRulesPanel, AIKeyCheckBox, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        passwordCheckBox = new JCheckBox();
        passwordCheckBox.setText("Password");
        SwingLayout.add(leakRulesPanel, passwordCheckBox, 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakConfigPanel = new JPanel();
        SwingLayout.configureGrid(leakConfigPanel, 1, 6, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(leakPanel, leakConfigPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        leakConfigPanel.setBorder(BorderFactory.createTitledBorder(null, "Config", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        leakDetBase64Box = new JCheckBox();
        leakDetBase64Box.setText("Detect Base64");
        SwingLayout.add(leakConfigPanel, leakDetBase64Box, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakAITriageBox = new JCheckBox();
        leakAITriageBox.setText("AI Triage");
        leakAITriageBox.setToolTipText("启用 AI 对每条命中结果研判，仅展示通过的结果");
        SwingLayout.add(leakConfigPanel, leakAITriageBox, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        leakAITriageViewBtn = new JButton();
        leakAITriageViewBtn.setText("AI VIEW");
        leakAITriageViewBtn.setToolTipText("查看 AI 研判面板（通过/未通过及原因）");
        SwingLayout.add(leakConfigPanel, leakAITriageViewBtn, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        leakStartBtn = new JButton();
        leakStartBtn.setText("START");
        SwingLayout.add(leakConfigPanel, leakStartBtn, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        leakCleanBtn = new JButton();
        leakCleanBtn.setText("CLEAN");
        SwingLayout.add(leakConfigPanel, leakCleanBtn, 0, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        exportLeakBtn = new JButton();
        exportLeakBtn.setText("EXPORT");
        SwingLayout.add(leakConfigPanel, exportLeakBtn, 0, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        leakProgressBar = new JProgressBar();
        leakProgressBar.setBorderPainted(true);
        leakProgressBar.setString("ready");
        leakProgressBar.setStringPainted(true);
        SwingLayout.add(leakPanel, leakProgressBar, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        leakResultPanel = new JPanel();
        SwingLayout.configureGrid(leakResultPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(leakPanel, leakResultPanel, 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        leakResultPanel.setBorder(BorderFactory.createTitledBorder(null, "Result", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        leakResultScroll = new JScrollPane();
        SwingLayout.add(leakResultPanel, leakResultScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(-1, 300), null, null, 0);
        leakResultList = new JList();
        leakResultScroll.setViewportView(leakResultList);
        gadgetPanel = new JPanel();
        SwingLayout.configureGrid(gadgetPanel, 2, 1, new Insets(3, 3, 3, 3), -1, -1);
        tabbedPanel.addTab("gadget", gadgetPanel);
        gadgetInputPanel = new JPanel();
        SwingLayout.configureGrid(gadgetInputPanel, 3, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(gadgetPanel, gadgetInputPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, false, null, null, null, 0);
        gadgetDirLabel = new JLabel();
        gadgetDirLabel.setText("选择依赖目录");
        SwingLayout.add(gadgetInputPanel, gadgetDirLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        gadgetChoseBtn = new JButton();
        gadgetChoseBtn.setText("选择");
        SwingLayout.add(gadgetInputPanel, gadgetChoseBtn, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        gadgetInputText = new JTextField();
        gadgetInputText.setEditable(false);
        SwingLayout.add(gadgetInputPanel, gadgetInputText, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        gadgetDescLabel = new JLabel();
        gadgetDescLabel.setText("说明");
        SwingLayout.add(gadgetInputPanel, gadgetDescLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        gadgetDescValueLabel = new JLabel();
        gadgetDescValueLabel.setText("");
        SwingLayout.add(gadgetInputPanel, gadgetDescValueLabel, 1, 1, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        gadgetOpPanel = new JPanel();
        SwingLayout.configureGrid(gadgetOpPanel, 1, 5, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(gadgetInputPanel, gadgetOpPanel, 2, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        gadgetStartBtn = new JButton();
        gadgetStartBtn.setText("开始分析");
        SwingLayout.add(gadgetOpPanel, gadgetStartBtn, 0, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        gadgetNativeBox = new JCheckBox();
        gadgetNativeBox.setText("原生");
        SwingLayout.add(gadgetOpPanel, gadgetNativeBox, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        gadgetHessianBox = new JCheckBox();
        gadgetHessianBox.setText("Hessian");
        SwingLayout.add(gadgetOpPanel, gadgetHessianBox, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        gadgetJdbcBox = new JCheckBox();
        gadgetJdbcBox.setText("JDBC");
        SwingLayout.add(gadgetOpPanel, gadgetJdbcBox, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        gadgetFastjsonBox = new JCheckBox();
        gadgetFastjsonBox.setText("Fastjson");
        SwingLayout.add(gadgetOpPanel, gadgetFastjsonBox, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        gadgetResultPanel = new JPanel();
        SwingLayout.configureGrid(gadgetResultPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(gadgetPanel, gadgetResultPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        gadgetResultScroll = new JScrollPane();
        SwingLayout.add(gadgetResultPanel, gadgetResultScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        gadgetResultTable = new JTable();
        gadgetResultScroll.setViewportView(gadgetResultTable);
        advancePanel = new JPanel();
        SwingLayout.configureGrid(advancePanel, 4, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("advance", advancePanel);
        javaVulSearchPanel = new JPanel();
        SwingLayout.configureGrid(javaVulSearchPanel, 12, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(advancePanel, javaVulSearchPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        javaVulSearchPanel.setBorder(BorderFactory.createTitledBorder(null, "Java Vulnerability", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        javaVulLabel = new JLabel();
        javaVulLabel.setText("Quickly Search Commons Java Vulnerabilities Call");
        SwingLayout.add(javaVulSearchPanel, javaVulLabel, 0, 0, 1, 3, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        javaVulScroll = new JScrollPane();
        SwingLayout.add(javaVulSearchPanel, javaVulScroll, 2, 0, 10, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        vulOpPanel = new JPanel();
        SwingLayout.configureGrid(vulOpPanel, 1, 5, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(javaVulSearchPanel, vulOpPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        showLowRadio = new JRadioButton();
        showLowRadio.setText("展示低危（low）");
        SwingLayout.add(vulOpPanel, showLowRadio, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        final Component spacer4 = Box.createGlue();
        SwingLayout.add(vulOpPanel, spacer4, 0, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        showMediumRadio = new JRadioButton();
        showMediumRadio.setText("展示中危（medium）");
        SwingLayout.add(vulOpPanel, showMediumRadio, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        showHighRadio = new JRadioButton();
        showHighRadio.setText("展示高危（high）");
        SwingLayout.add(vulOpPanel, showHighRadio, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        showAllRadio = new JRadioButton();
        showAllRadio.setText("全部");
        SwingLayout.add(vulOpPanel, showAllRadio, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        final Component spacer5 = Box.createGlue();
        SwingLayout.add(advancePanel, spacer5, 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, false, true, null, null, null, 0);
        piPanel = new JPanel();
        SwingLayout.configureGrid(piPanel, 6, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(advancePanel, piPanel, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        piPanel.setBorder(BorderFactory.createTitledBorder(null, "Plugins", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        encoderLabel = new JLabel();
        encoderLabel.setText("A tool for encode/decode encrypt/decrypt operations");
        SwingLayout.add(piPanel, encoderLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        encoderBtn = new JButton();
        encoderBtn.setText("Start");
        SwingLayout.add(piPanel, encoderBtn, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        listenerLabel = new JLabel();
        listenerLabel.setText("A tool for listening port and send by socket");
        SwingLayout.add(piPanel, listenerLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        listenerBtn = new JButton();
        listenerBtn.setText("Start");
        SwingLayout.add(piPanel, listenerBtn, 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        sqliteButton = new JButton();
        sqliteButton.setText("Start");
        SwingLayout.add(piPanel, sqliteButton, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        sqliteLabel = new JLabel();
        sqliteLabel.setText("A tool for run custom query in SQLite database");
        SwingLayout.add(piPanel, sqliteLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        spelLabel = new JLabel();
        spelLabel.setText("A tool for Spring EL search");
        SwingLayout.add(piPanel, spelLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        springELStartButton = new JButton();
        springELStartButton.setText("Start");
        SwingLayout.add(piPanel, springELStartButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        serUtilLabel = new JLabel();
        serUtilLabel.setText("A tool for bytecodes in Java Serialization Data");
        SwingLayout.add(piPanel, serUtilLabel, 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        serUtilBtn = new JButton();
        serUtilBtn.setText("Start");
        SwingLayout.add(piPanel, serUtilBtn, 4, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        bcelLabel = new JLabel();
        bcelLabel.setText("A tool for parse BCEL bytecode to Java code");
        SwingLayout.add(piPanel, bcelLabel, 5, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        bcelBtn = new JButton();
        bcelBtn.setText("Start");
        SwingLayout.add(piPanel, bcelBtn, 5, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        analysis = new JPanel();
        SwingLayout.configureGrid(analysis, 1, 10, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(advancePanel, analysis, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        analysis.setBorder(BorderFactory.createTitledBorder(null, "Analysis", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        frameBtn = new JButton();
        frameBtn.setText("Full Frame");
        SwingLayout.add(analysis, frameBtn, 0, 7, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, new Dimension(150, -1), 0);
        cfgBtn = new JButton();
        cfgBtn.setText("Show CFG");
        SwingLayout.add(analysis, cfgBtn, 0, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, new Dimension(150, -1), 0);
        simpleFrameButton = new JButton();
        simpleFrameButton.setText("Simple Frame");
        SwingLayout.add(analysis, simpleFrameButton, 0, 5, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, new Dimension(150, -1), 0);
        htmlGraphBtn = new JButton();
        htmlGraphBtn.setText("HTML Graph");
        SwingLayout.add(analysis, htmlGraphBtn, 0, 2, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, new Dimension(150, -1), 0);
        chainsPanel = new JPanel();
        SwingLayout.configureGrid(chainsPanel, 4, 2, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel.addTab("chains", chainsPanel);
        chainsSinkPanel = new JPanel();
        SwingLayout.configureGrid(chainsSinkPanel, 6, 2, new Insets(5, 5, 5, 5), -1, -1);
        SwingLayout.add(chainsPanel, chainsSinkPanel, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        chainsSinkPanel.setBorder(BorderFactory.createTitledBorder(null, "Sink Config", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        sinkClassLabel = new JLabel();
        sinkClassLabel.setText("Sink Class");
        SwingLayout.add(chainsSinkPanel, sinkClassLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sinkClassText = new JTextField();
        SwingLayout.add(chainsSinkPanel, sinkClassText, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        sinkMethodLabel = new JLabel();
        sinkMethodLabel.setText("Sink Method");
        SwingLayout.add(chainsSinkPanel, sinkMethodLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sinkDescLabel = new JLabel();
        sinkDescLabel.setText("Sink Desc");
        SwingLayout.add(chainsSinkPanel, sinkDescLabel, 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sinkMethodText = new JTextField();
        SwingLayout.add(chainsSinkPanel, sinkMethodText, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        sinkDescText = new JTextField();
        SwingLayout.add(chainsSinkPanel, sinkDescText, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        sinkTipLabel = new JLabel();
        sinkTipLabel.setText("从 note -> favorites 右键 send to sink");
        SwingLayout.add(chainsSinkPanel, sinkTipLabel, 0, 0, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sinkBox = new JComboBox();
        SwingLayout.add(chainsSinkPanel, sinkBox, 1, 0, 1, 2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        sinkJarLabel = new JLabel();
        sinkJarLabel.setText("注意：不要求加载 Jar 中包含 Sink 类");
        SwingLayout.add(chainsSinkPanel, sinkJarLabel, 5, 0, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        chainsSourcePanel = new JPanel();
        SwingLayout.configureGrid(chainsSourcePanel, 5, 2, new Insets(5, 5, 5, 5), -1, -1);
        SwingLayout.add(chainsPanel, chainsSourcePanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        chainsSourcePanel.setBorder(BorderFactory.createTitledBorder(null, "Source Config", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        sourceClassLabel = new JLabel();
        sourceClassLabel.setText("Source Class");
        SwingLayout.add(chainsSourcePanel, sourceClassLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sourceClassText = new JTextField();
        SwingLayout.add(chainsSourcePanel, sourceClassText, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        sourceMethodLabel = new JLabel();
        sourceMethodLabel.setText("Source Method");
        SwingLayout.add(chainsSourcePanel, sourceMethodLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sourceDescLabel = new JLabel();
        sourceDescLabel.setText("Source Desc");
        SwingLayout.add(chainsSourcePanel, sourceDescLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sourceMethodText = new JTextField();
        SwingLayout.add(chainsSourcePanel, sourceMethodText, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        sourceDescText = new JTextField();
        SwingLayout.add(chainsSourcePanel, sourceDescText, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        sourceTipLabel = new JLabel();
        sourceTipLabel.setText("从 note -> favorites 右键 send to source");
        SwingLayout.add(chainsSourcePanel, sourceTipLabel, 0, 0, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sourcePanel = new JPanel();
        SwingLayout.configureGrid(sourcePanel, 2, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(chainsSourcePanel, sourcePanel, 4, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        sourceNullRadio = new JRadioButton();
        sourceNullRadio.setText("空 Source 列举");
        SwingLayout.add(sourcePanel, sourceNullRadio, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        sourceEnableRadio = new JRadioButton();
        sourceEnableRadio.setText("指定 Source 搜索");
        SwingLayout.add(sourcePanel, sourceEnableRadio, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        sourceLabel = new JLabel();
        sourceLabel.setText("模式");
        SwingLayout.add(sourcePanel, sourceLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        sourceOnlyWebBox = new JCheckBox();
        sourceOnlyWebBox.setText("Source 只考虑 Spring / Servlet 入口");
        SwingLayout.add(sourcePanel, sourceOnlyWebBox, 1, 1, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        chainsDescPanel = new JPanel();
        SwingLayout.configureGrid(chainsDescPanel, 1, 2, new Insets(5, 5, 5, 5), -1, -1);
        SwingLayout.add(chainsPanel, chainsDescPanel, 0, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        chainsLabel = new JLabel();
        chainsLabel.setText("");
        SwingLayout.add(chainsDescPanel, chainsLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        final Component spacer6 = Box.createGlue();
        SwingLayout.add(chainsDescPanel, spacer6, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        chainsOpPanel = new JPanel();
        SwingLayout.configureGrid(chainsOpPanel, 2, 8, new Insets(5, 5, 5, 5), -1, -1);
        SwingLayout.add(chainsPanel, chainsOpPanel, 3, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        chainsOpPanel.setBorder(BorderFactory.createTitledBorder(null, "启动配置", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        startChainsBtn = new JButton();
        startChainsBtn.setText("分析");
        SwingLayout.add(chainsOpPanel, startChainsBtn, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        sourceRadio = new JRadioButton();
        sourceRadio.setText("从 Source 分析");
        SwingLayout.add(chainsOpPanel, sourceRadio, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        sinkRadio = new JRadioButton();
        sinkRadio.setText("从 Sink 分析");
        SwingLayout.add(chainsOpPanel, sinkRadio, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        maxDepthSpin = new JSpinner();
        SwingLayout.add(chainsOpPanel, maxDepthSpin, 0, 5, 1, 2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, null, new Dimension(80, -1), 0);
        maxDepthLabel = new JLabel();
        maxDepthLabel.setText("最大深度");
        SwingLayout.add(chainsOpPanel, maxDepthLabel, 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        final Component spacer7 = Box.createGlue();
        SwingLayout.add(chainsOpPanel, spacer7, 0, 7, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        clearBtn = new JButton();
        clearBtn.setText("清空");
        SwingLayout.add(chainsOpPanel, clearBtn, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        taintBox = new JCheckBox();
        taintBox.setText("污点分析验证");
        SwingLayout.add(chainsOpPanel, taintBox, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        startTaintBtn = new JButton();
        startTaintBtn.setText("手动启动污点分析");
        SwingLayout.add(chainsOpPanel, startTaintBtn, 1, 4, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        dfsAdvanceBtn = new JButton();
        dfsAdvanceBtn.setText("高级设置（数量限制/导出等）");
        SwingLayout.add(chainsOpPanel, dfsAdvanceBtn, 1, 1, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        SwingLayout.add(chainsPanel, chainsResult, 2, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        curPanel = new JPanel();
        SwingLayout.configureGrid(curPanel, 3, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(coreRightSplit, curPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        curPanel.setBorder(BorderFactory.createTitledBorder(null, "Current", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        curClassLabel = new JLabel();
        curClassLabel.setText("Class");
        SwingLayout.add(curPanel, curClassLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        curClassText = new JTextField();
        curClassText.setEditable(false);
        SwingLayout.add(curPanel, curClassText, 1, 1, 1, 2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        curMethodLabel = new JLabel();
        curMethodLabel.setText("Method");
        SwingLayout.add(curPanel, curMethodLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        curMethodText = new JTextField();
        curMethodText.setEditable(false);
        SwingLayout.add(curPanel, curMethodText, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        curJarLabel = new JLabel();
        curJarLabel.setText("Jar");
        SwingLayout.add(curPanel, curJarLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        curJarText = new JTextField();
        curJarText.setEditable(false);
        SwingLayout.add(curPanel, curJarText, 0, 1, 1, 2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        addToFavoritesButton = new JButton();
        addToFavoritesButton.setText("add to favorites");
        SwingLayout.add(curPanel, addToFavoritesButton, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        final Component spacer8 = Box.createGlue();
        SwingLayout.add(coreRightSplit, spacer8, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, false, true, null, null, null, 0);
        logPanel = new JPanel();
        SwingLayout.configureGrid(logPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        treeContentSplit.setRightComponent(logPanel);
        logScroll = new JScrollPane();
        SwingLayout.add(logPanel, logScroll, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(500, 100), new Dimension(500, 100), null, 0);
        logScroll.setBorder(BorderFactory.createTitledBorder(null, "Log", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        logArea = new JTextPane();
        logArea.setBackground(new Color(-13619152));
        logArea.setEditable(false);
        logArea.setForeground(new Color(-16012544));
        logArea.setText("");
        logScroll.setViewportView(logArea);
        curMethodPanel = new JPanel();
        SwingLayout.configureGrid(curMethodPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(logPanel, curMethodPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(600, 150), new Dimension(600, 150), null, 0);
        allMethodScroll = new JScrollPane();
        SwingLayout.add(curMethodPanel, allMethodScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        allMethodList = new JList();
        allMethodScroll.setViewportView(allMethodList);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(methodDefinitionRadioButton);
        buttonGroup.add(methodCallRadioButton);
        buttonGroup.add(stringContainsRadioButton);
        buttonGroup.add(binarySearchRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(fernRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(likeSearchRadioButton);
        buttonGroup.add(equalsSearchRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(scaOutConsoleRadio);
        buttonGroup.add(scaOutHtmlRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(sourceRadio);
        buttonGroup.add(sinkRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(sourceNullRadio);
        buttonGroup.add(sourceEnableRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(showLowRadio);
        buttonGroup.add(showMediumRadio);
        buttonGroup.add(showHighRadio);
        buttonGroup.add(showAllRadio);
    }

    /**
     * @noinspection ALL
     */
    private Font resolveFont(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return masterPanel;
    }

    private void createCustomComponents() {
        chainsResult = new ChainsResultPanel();
    }
}

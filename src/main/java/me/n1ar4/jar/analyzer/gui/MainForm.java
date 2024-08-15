package me.n1ar4.jar.analyzer.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.graph.HtmlGraph;
import me.n1ar4.jar.analyzer.gui.action.*;
import me.n1ar4.jar.analyzer.gui.adapter.*;
import me.n1ar4.jar.analyzer.gui.font.FontHelper;
import me.n1ar4.jar.analyzer.gui.render.AllMethodsRender;
import me.n1ar4.jar.analyzer.gui.render.ClassRender;
import me.n1ar4.jar.analyzer.gui.render.MethodCallRender;
import me.n1ar4.jar.analyzer.gui.render.SpringMethodRender;
import me.n1ar4.jar.analyzer.gui.state.State;
import me.n1ar4.jar.analyzer.gui.tree.FileTree;
import me.n1ar4.jar.analyzer.gui.update.UpdateChecker;
import me.n1ar4.jar.analyzer.gui.util.*;
import me.n1ar4.jar.analyzer.gui.vul.*;
import me.n1ar4.jar.analyzer.sca.SCAAction;
import me.n1ar4.jar.analyzer.starter.Const;
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
import java.util.Locale;

public class MainForm {
    private static final Logger logger = LogManager.getLogger();
    // FOR CLI
    private static final MainForm fakeInstance = new MainForm(true);
    private static MainForm instance;
    private static ConfigFile config;
    private static CoreEngine engine;
    private static JTextArea codeArea;
    private static MethodResult curMethod;
    private static String curClass;
    private static State prevState;
    private static State nextState;
    private static State curState;
    private static DefaultListModel<MethodResult> historyListData;
    private JPanel masterPanel;
    private JTabbedPane tabbedPanel;
    private JPanel codePanel;
    private JPanel corePanel;
    private JPanel startPanel;
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
    private JPanel leftPanel;
    private JScrollPane treeScrollPanel;
    private FileTree fileTree;
    private JPanel logPanel;
    private JScrollPane logScroll;
    private JTextPane logArea;
    private JPanel curMethodPanel;
    private JScrollPane allMethodScroll;
    private JList<MethodResult> allMethodList;
    private JPanel notePanel;
    private JPanel advancePanel;
    private JScrollPane hisScroll;
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
    private JPanel searchResPanel;
    private JScrollPane searchScroll;
    private JList<MethodResult> searchList;
    private JTextField curJarText;
    private JLabel curJarLabel;
    private JTextField rtText;
    private JLabel jreRuntimeLabel;
    private JCheckBox autoFindRtJarCheckBox;
    private JCheckBox addRtJarWhenCheckBox;
    private JButton opcodeBtn;
    private JButton javaAsmBtn;
    private JButton JNDIButton;
    private JButton runtimeExecButton;
    private JButton processBuilderStartButton;
    private JButton spELGetValueButton;
    private JButton readObjectButton;
    private JButton scriptEngineEvalButton;
    private JButton BCELLoadClassButton;
    private JButton defineClassButton;
    private JButton OGNLGetValueButton;
    private JPanel javaVulSearchPanel;
    private JLabel javaVulLabel;
    private JLabel logoLabel;
    private JPanel authorPanel;
    private JLabel authorLabel;
    private JLabel authorTextLabel;
    private JPanel curPanel;
    private JPanel methodImplPanel;
    private JScrollPane implScroll;
    private JList<MethodResult> methodImplList;
    private JCheckBox deleteTempCheckBox;
    private JPanel callerPanel;
    private JScrollPane callerScroll;
    private JPanel calleePanel;
    private JScrollPane calleeScroll;
    private JPanel callPanel;
    private JPanel searchOptionsPanel;
    private JPanel searchInnerPanel;
    private JLabel searchClassLabel;
    private JLabel searchMethodLabel;
    private JLabel searchStrLabel;
    private JScrollPane superImplScroll;
    private JList<MethodResult> superImplList;
    private JPanel analysis;
    private JButton cfgBtn;
    private JButton frameBtn;
    private JButton encoderBtn;
    private JButton repeaterBtn;
    private JButton listenerBtn;
    private JPanel springPanel;
    private JPanel springCPanel;
    private JPanel springMPanel;
    private JScrollPane scScroll;
    private JScrollPane smScroll;
    private JList<ClassResult> springCList;
    private JList<MethodResult> springMList;
    private JPanel piPanel;
    private JLabel encoderLabel;
    private JLabel repeaterLabel;
    private JLabel listenerLabel;
    private JButton prevBtn;
    private JButton nextBtn;
    private JPanel actionPanel;
    private JPanel soPanel;
    private JRadioButton likeSearchRadioButton;
    private JRadioButton equalsSearchRadioButton;
    private JPanel blackListPanel;
    private JScrollPane blackScroll;
    private JTextArea blackArea;
    private JTextArea classBlackArea;
    private JLabel classBlackListLabel;
    private JScrollPane classBlackPanel;
    private JLabel classBlackLabel;
    private JButton simpleFrameButton;
    private JButton refreshButton;
    private JLabel springLabel;
    private JButton sqliteButton;
    private JLabel sqliteLabel;
    private JButton cleanButton;
    private JButton showStringListButton;
    private JButton fastjsonButton;
    private JButton unzipButton;
    private JButton hessianButton;
    private JButton springELStartButton;
    @SuppressWarnings("all")
    private JLabel spelLabel;
    private JButton startELSearchButton;
    private JButton obfBtn;
    private JLabel obfLabel;
    private JButton serUtilBtn;
    private JLabel serUtilLabel;
    private JScrollPane classWhitePanel;
    private JTextArea classWhiteArea;
    private JLabel classWhiteListLabel;
    private JButton pathSearchButton;
    private JLabel pathSearchLabel;
    private JTextField pathSearchTextField;
    private JTextField fileTreeSearchTextField;
    private JPanel fileTreeSearchPanel;
    private JLabel fileTreeSearchLabel;
    private JPanel searchFileNamePanel;
    private JPanel scaPanel;
    private JCheckBox scaLog4jBox;
    private JCheckBox scaShiroBox;
    private JCheckBox scaFastjsonBox;
    private JPanel modulePanel;
    private JTextField scaFileText;
    private JButton scaOpenBtn;
    private JTextArea scaConsoleArea;
    private JRadioButton scaOutConsoleRadio;
    private JRadioButton scaOutHtmlRadio;
    private JButton scaStartBtn;
    private JPanel scaOutPanel;
    private JLabel scaOutLabel;
    private JScrollPane scanConsoleScroll;
    private JLabel scaFileLabel;
    private JPanel scaActionPanel;
    private JPanel scaTipPanel;
    private JLabel scaTipLabel;
    private JLabel outputFileLabel;
    private JTextField outputFileText;
    private JButton scaResultOpenBtn;
    private JButton htmlGraphBtn;
    private JScrollPane favScroll;
    private JList<MethodResult> favList;
    private JButton addToFavoritesButton;
    private JButton freeMarkerButton;
    private JButton JEXLButton;
    private JButton rhinoEvalButton;
    private JButton aviatorExecuteButton;
    private JButton mvelEvalButton;
    private JButton qlExpressButton;
    private JButton sqlExecNoPrepareButton;
    private JButton sqlExecButton;
    private JButton xStreamButton;
    private static DefaultListModel<MethodResult> favData;

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

    public JButton getRepeaterBtn() {
        return repeaterBtn;
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

    public JButton getPrevBtn() {
        return prevBtn;
    }

    public JButton getNextBtn() {
        return nextBtn;
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

    public JTextArea getClassBlackArea() {
        return classBlackArea;
    }

    public JTextArea getClassWhiteArea() {
        return classWhiteArea;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getJNDIButton() {
        return JNDIButton;
    }

    public JButton getRuntimeExecButton() {
        return runtimeExecButton;
    }

    public JButton getProcessBuilderStartButton() {
        return processBuilderStartButton;
    }

    public JButton getSpELGetValueButton() {
        return spELGetValueButton;
    }

    public JButton getReadObjectButton() {
        return readObjectButton;
    }

    public JButton getOGNLGetValueButton() {
        return OGNLGetValueButton;
    }

    public JButton getBCELLoadClassButton() {
        return BCELLoadClassButton;
    }

    public JButton getJEXLButton() {
        return JEXLButton;
    }

    public JButton getRhinoEvalButton() {
        return rhinoEvalButton;
    }

    public JButton getAviatorExecuteButton() {
        return aviatorExecuteButton;
    }

    public JButton getMvelEvalButton() {
        return mvelEvalButton;
    }

    public JButton getQlExpressButton() {
        return qlExpressButton;
    }

    public JButton getSqlExecButton() {
        return sqlExecButton;
    }

    public JButton getSqlExecNoPrepareButton() {
        return sqlExecNoPrepareButton;
    }

    public JButton getXStreamButton() {
        return xStreamButton;
    }

    public JButton getDefineClassButton() {
        return defineClassButton;
    }

    public JButton getScriptEngineEvalButton() {
        return scriptEngineEvalButton;
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

    public JButton getFastjsonButton() {
        return fastjsonButton;
    }

    public JButton getUnzipButton() {
        return unzipButton;
    }

    public JButton getHessianButton() {
        return hessianButton;
    }

    public JButton getFreeMarkerButton() {
        return freeMarkerButton;
    }

    public JButton getStartELSearchButton() {
        return startELSearchButton;
    }

    public JButton getObfBtn() {
        return obfBtn;
    }

    public JButton getSerUtilBtn() {
        return serUtilBtn;
    }

    public static State getPrevState() {
        return prevState;
    }

    public static void setPrevState(State prevState) {
        MainForm.prevState = prevState;
    }

    public static State getNextState() {
        return nextState;
    }

    public static void setNextState(State nextState) {
        MainForm.nextState = nextState;
    }

    public static State getCurState() {
        return curState;
    }

    public static void setCurState(State curState) {
        MainForm.curState = curState;
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

    public MainForm(boolean fake) {
        if (fake) {
            logger.info("init fake instance");
        }
    }

    public MainForm() {
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

        historyList.setCellRenderer(new MethodCallRender());
        favList.setCellRenderer(new MethodCallRender());

        historyListData = new DefaultListModel<>();
        historyList.setModel(historyListData);
        favData = new DefaultListModel<>();
        favList.setModel(favData);

        prevBtn.setIcon(IconManager.prevIcon);
        nextBtn.setIcon(IconManager.nextIcon);

        logoLabel.setIcon(IconManager.showIcon);
        jreRuntimeLabel.setIcon(IconManager.javaIcon);
        dbPathLabel.setIcon(IconManager.dbIcon);
        startEngineButton.setIcon(IconManager.startIcon);
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

        logger.info("init main form success");
    }

    private static void init() {
        FontHelper.installFont();
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
        PrevNextAction.run();
        SpringAction.run();
        CleanAction.run();
        ShowStringAction.run();
        TipsAction.run();
        HtmlGraph.run();

        SCAAction.register();
        JNDIVulAction.register();
        RuntimeExecAction.register();
        ProcessBuilderAction.register();
        SpELAction.register();
        OGNLAction.register();
        BCELVulAction.register();
        DefineClassAction.register();
        ReadObjectVulAction.register();
        ScriptEngineAction.register();
        FastjsonVulAction.register();
        ZIPVulAction.register();
        HessianAction.register();
        FreeMarkerAction.register();
        JEXLAction.register();
        RhinoAction.register();
        AviatorAction.register();
        MvelAction.register();
        QLExpressAction.register();
        XStreamAction.register();
        SQLExecAction.register();
        SQLExecNoPrepareAction.register();

        Font codeFont = FontHelper.getFont();
        instance.blackArea.setFont(codeFont);
        instance.classBlackArea.setFont(codeFont);
        instance.classWhiteArea.setFont(codeFont);

        codeArea.addKeyListener(new GlobalKeyListener());
        instance.allMethodList.addKeyListener(new GlobalKeyListener());
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
        instance.favList.addMouseListener(new FavMouseAdapter());
        instance.fileTreeSearchTextField.getDocument().addDocumentListener(new SearchInputListener());
        instance.fileTreeSearchTextField.addKeyListener(new SearchTextFieldKeyAdapter());
        instance.fileTreeSearchTextField.addKeyListener(new FileTreeKeyAdapter());

        instance.getAddToFavoritesButton().addActionListener(e -> {
            if (curMethod != null) {
                getFavData().addElement(curMethod);
                JOptionPane.showMessageDialog(instance.masterPanel, "add current method to favorite ok");
            } else {
                JOptionPane.showMessageDialog(instance.masterPanel, "current method is null");
            }
        });

        refreshLang(false);
        MenuUtil.setLangFlag();
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
                if (c != 8) {
                    throw new RuntimeException("tabbed panel error");
                }
                instance.tabbedPanel.setTitleAt(0, "开始");
                instance.tabbedPanel.setTitleAt(1, "搜索");
                instance.tabbedPanel.setTitleAt(2, "调用");
                instance.tabbedPanel.setTitleAt(3, "实现");
                instance.tabbedPanel.setTitleAt(4, "spring");
                instance.tabbedPanel.setTitleAt(5, "记录");
                instance.tabbedPanel.setTitleAt(6, "SCA");
                instance.tabbedPanel.setTitleAt(7, "高级");

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
                instance.deleteTempCheckBox.setText("删除缓存");
                instance.autoFindRtJarCheckBox.setText("自动搜索RT.JAR");
                instance.addRtJarWhenCheckBox.setText("分析时添加RT.JAR");
                instance.startEngineButton.setText("启动引擎");

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

                instance.classBlackLabel.setText("类名黑名单 (完整类名并使用 ; 和 \\n 分割)");
                instance.startSearchButton.setText("开始搜索");
                instance.springLabel.setText("分析 JAR/JARS 中的 Spring Controller/Mapping 信息");
                instance.pathSearchButton.setText("查找");
                instance.pathSearchLabel.setText("在所有 Mapping 中查找 Path");
                instance.refreshButton.setText("刷新");
                instance.javaVulLabel.setText("快速搜索通用 JAVA 漏洞相关");

                instance.sqliteLabel.setText("一个 SQLITE 查询工具");
                instance.encoderLabel.setText("一个编码解码加密解密工具");
                instance.repeaterLabel.setText("一个 HTTP 发送工具");
                instance.listenerLabel.setText("一个 SOCKET 监听工具");
                instance.spelLabel.setText("一个 SPEL 表达式搜索工具");
                instance.startELSearchButton.setText("开始表达式搜索");
                instance.obfLabel.setText("一个 Java 序列化数据混淆工具");
                instance.serUtilLabel.setText("一个分析 Java 序列化数据中字节码的工具");

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
                if (c != 8) {
                    throw new RuntimeException("tabbed panel error");
                }
                instance.tabbedPanel.setTitleAt(0, "start");
                instance.tabbedPanel.setTitleAt(1, "search");
                instance.tabbedPanel.setTitleAt(2, "call");
                instance.tabbedPanel.setTitleAt(3, "impl");
                instance.tabbedPanel.setTitleAt(4, "spring");
                instance.tabbedPanel.setTitleAt(5, "note");
                instance.tabbedPanel.setTitleAt(6, "SCA");
                instance.tabbedPanel.setTitleAt(7, "advance");

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
                instance.deleteTempCheckBox.setText("Delete Temp");
                instance.autoFindRtJarCheckBox.setText("Auto Find rt.jar");
                instance.addRtJarWhenCheckBox.setText("Add rt.jar to Analyze");
                instance.startEngineButton.setText("Start Engine");

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

                instance.classBlackLabel.setText("Class Name Black List (full class name and split by ; and \\n)");
                instance.startSearchButton.setText("Start Search");
                instance.springLabel.setText(" Analyze Spring Controllers and Mappings in Jar/Jars");
                instance.pathSearchButton.setText("Search");
                instance.pathSearchLabel.setText(" Search path in all Mappings");
                instance.refreshButton.setText("Refresh");
                instance.javaVulLabel.setText("Quickly Search Commons Java Vulnerabilities Call");

                instance.sqliteLabel.setText("A tool for run custom query in SQLite database");
                instance.encoderLabel.setText("A tool for encode/decode encrypt/decrypt operations");
                instance.repeaterLabel.setText("A tool for sending http request and get response");
                instance.listenerLabel.setText("A tool for listening port and send by socket");
                instance.spelLabel.setText("A tool for Spring EL search");
                instance.startELSearchButton.setText("Start EL Search");
                instance.obfLabel.setText("A tool for obfuscate java serialization data");
                instance.serUtilLabel.setText("A tool for bytecodes in Java Serialization Data");

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

        frame.setJMenuBar(MenuUtil.createMenuBar());
        frame.setContentPane(instance.masterPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.setIconImage(IconManager.showIcon.getImage());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int resp = JOptionPane.showConfirmDialog(frame, "CONFIRM EXIT?",
                        "Exit Confirmation", JOptionPane.OK_CANCEL_OPTION);
                if (resp == JOptionPane.OK_OPTION) {
                    frame.dispose();
                    System.exit(0);
                }
            }
        });

        UpdateChecker.check();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(false);
        return frame;
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
        masterPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        corePanel = new JPanel();
        corePanel.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        masterPanel.add(corePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        codePanel = new JPanel();
        codePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        corePanel.add(codePanel, new GridConstraints(0, 1, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(700, 750), new Dimension(700, 750), null, 0, false));
        codePanel.setBorder(BorderFactory.createTitledBorder(null, "Java Decompile Code", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tabbedPanel = new JTabbedPane();
        corePanel.add(tabbedPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(550, -1), new Dimension(550, 200), new Dimension(550, -1), 0, false));
        startPanel = new JPanel();
        startPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("start", startPanel);
        chosePanel = new JPanel();
        chosePanel.setLayout(new GridLayoutManager(9, 6, new Insets(0, 0, 0, 0), -1, -1));
        startPanel.add(chosePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        chosePanel.setBorder(BorderFactory.createTitledBorder(null, "Starter", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        choseBtn = new JButton();
        choseBtn.setText("Chose File / Dir");
        chosePanel.add(choseBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileText = new JTextField();
        fileText.setEditable(false);
        fileText.setText("");
        chosePanel.add(fileText, new GridConstraints(0, 1, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        resolveJarsInJarCheckBox = new JCheckBox();
        resolveJarsInJarCheckBox.setText("Resolve Jars in Jar");
        chosePanel.add(resolveJarsInJarCheckBox, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buildBar = new JProgressBar();
        buildBar.setForeground(new Color(-9524737));
        buildBar.setStringPainted(true);
        chosePanel.add(buildBar, new GridConstraints(7, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        autoSaveCheckBox = new JCheckBox();
        autoSaveCheckBox.setText("Auto Save");
        chosePanel.add(autoSaveCheckBox, new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dbPathLabel = new JLabel();
        dbPathLabel.setText("Database Path");
        chosePanel.add(dbPathLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        loadDBText = new JTextField();
        loadDBText.setEditable(false);
        chosePanel.add(loadDBText, new GridConstraints(1, 1, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jreRuntimeLabel = new JLabel();
        jreRuntimeLabel.setText("JRE Runtime");
        chosePanel.add(jreRuntimeLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        rtText = new JTextField();
        rtText.setEditable(false);
        chosePanel.add(rtText, new GridConstraints(2, 1, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        autoFindRtJarCheckBox = new JCheckBox();
        autoFindRtJarCheckBox.setText("Auto Find rt.jar");
        chosePanel.add(autoFindRtJarCheckBox, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addRtJarWhenCheckBox = new JCheckBox();
        addRtJarWhenCheckBox.setText("Add rt.jar to Analyze");
        chosePanel.add(addRtJarWhenCheckBox, new GridConstraints(6, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startEngineButton = new JButton();
        startEngineButton.setText("Start Engine");
        chosePanel.add(startEngineButton, new GridConstraints(6, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteTempCheckBox = new JCheckBox();
        deleteTempCheckBox.setText("Delete Temp");
        chosePanel.add(deleteTempCheckBox, new GridConstraints(5, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classBlackListLabel = new JLabel();
        classBlackListLabel.setText("Class Black List");
        chosePanel.add(classBlackListLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        classBlackPanel = new JScrollPane();
        chosePanel.add(classBlackPanel, new GridConstraints(4, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 75), new Dimension(-1, 75), new Dimension(-1, 75), 0, false));
        classBlackArea = new JTextArea();
        classBlackArea.setBackground(new Color(-12895429));
        classBlackArea.setForeground(new Color(-16711931));
        classBlackArea.setRows(0);
        classBlackPanel.setViewportView(classBlackArea);
        classWhiteListLabel = new JLabel();
        classWhiteListLabel.setText("Class White List");
        chosePanel.add(classWhiteListLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        classWhitePanel = new JScrollPane();
        chosePanel.add(classWhitePanel, new GridConstraints(3, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 75), new Dimension(-1, 75), new Dimension(-1, 75), 0, false));
        classWhiteArea = new JTextArea();
        classWhiteArea.setBackground(new Color(-12895429));
        classWhiteArea.setForeground(new Color(-853761));
        classWhiteArea.setRows(0);
        classWhiteArea.setText("");
        classWhitePanel.setViewportView(classWhiteArea);
        decompilerPanel = new JPanel();
        decompilerPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        chosePanel.add(decompilerPanel, new GridConstraints(8, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        decompilerPanel.setBorder(BorderFactory.createTitledBorder(null, "Decompiler", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        fernRadio = new JRadioButton();
        fernRadio.setEnabled(true);
        fernRadio.setSelected(true);
        fernRadio.setText(" FernFlower (from jetbrains/intellij-community)");
        decompilerPanel.add(fernRadio, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        opcodeBtn = new JButton();
        opcodeBtn.setText("Show Method Opcode");
        decompilerPanel.add(opcodeBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        javaAsmBtn = new JButton();
        javaAsmBtn.setText("Java ASM Code");
        decompilerPanel.add(javaAsmBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        startPanel.add(infoPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoPanel.setBorder(BorderFactory.createTitledBorder(null, "Information", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalClassLabel = new JLabel();
        totalClassLabel.setText("Total Class");
        infoPanel.add(totalClassLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        totalClassVal = new JLabel();
        totalClassVal.setText("0");
        infoPanel.add(totalClassVal, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        totalMethodLabel = new JLabel();
        totalMethodLabel.setText("Total Method");
        infoPanel.add(totalMethodLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        totalMethodVal = new JLabel();
        totalMethodVal.setText("0");
        infoPanel.add(totalMethodVal, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        totalJarLabel = new JLabel();
        totalJarLabel.setText("Total Jar");
        infoPanel.add(totalJarLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        totalJarVal = new JLabel();
        totalJarVal.setText("0");
        infoPanel.add(totalJarVal, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        databaseSizeLabel = new JLabel();
        databaseSizeLabel.setText("Database");
        infoPanel.add(databaseSizeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        databaseSizeVal = new JLabel();
        databaseSizeVal.setText("0 MB");
        infoPanel.add(databaseSizeVal, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        engineLabel = new JLabel();
        engineLabel.setText("Engine State");
        infoPanel.add(engineLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        engineVal = new JLabel();
        engineVal.setText("CLOSED");
        infoPanel.add(engineVal, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        authorPanel = new JPanel();
        authorPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        infoPanel.add(authorPanel, new GridConstraints(0, 2, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0, false));
        authorLabel = new JLabel();
        authorLabel.setText("");
        authorPanel.add(authorLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        authorTextLabel = new JLabel();
        authorTextLabel.setText("4ra1n");
        authorPanel.add(authorTextLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        startPanel.add(actionPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        actionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        prevBtn = new JButton();
        prevBtn.setText("");
        actionPanel.add(prevBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nextBtn = new JButton();
        nextBtn.setText("");
        actionPanel.add(nextBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cleanButton = new JButton();
        cleanButton.setText("Clean");
        actionPanel.add(cleanButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showStringListButton = new JButton();
        showStringListButton.setText("All Strings");
        actionPanel.add(showStringListButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startELSearchButton = new JButton();
        startELSearchButton.setText("Start EL Search");
        actionPanel.add(startELSearchButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchResPanel = new JPanel();
        searchResPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("search", searchResPanel);
        searchScroll = new JScrollPane();
        searchResPanel.add(searchScroll, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        searchList = new JList();
        searchScroll.setViewportView(searchList);
        searchOptionsPanel = new JPanel();
        searchOptionsPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        searchResPanel.add(searchOptionsPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        searchOptionsPanel.setBorder(BorderFactory.createTitledBorder(null, "Search Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        methodDefinitionRadioButton = new JRadioButton();
        methodDefinitionRadioButton.setText("method definition (方法定义)");
        searchOptionsPanel.add(methodDefinitionRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        methodCallRadioButton = new JRadioButton();
        methodCallRadioButton.setText("method call (方法调用)");
        searchOptionsPanel.add(methodCallRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stringContainsRadioButton = new JRadioButton();
        stringContainsRadioButton.setText("string contains (包含字符串)");
        searchOptionsPanel.add(stringContainsRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        binarySearchRadioButton = new JRadioButton();
        binarySearchRadioButton.setText("binary search (二进制包含)");
        searchOptionsPanel.add(binarySearchRadioButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchInnerPanel = new JPanel();
        searchInnerPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        searchOptionsPanel.add(searchInnerPanel, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        searchClassText = new JTextField();
        searchInnerPanel.add(searchClassText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        searchClassLabel = new JLabel();
        searchClassLabel.setText("Class");
        searchInnerPanel.add(searchClassLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        searchMethodLabel = new JLabel();
        searchMethodLabel.setText("Method");
        searchInnerPanel.add(searchMethodLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        searchMethodText = new JTextField();
        searchInnerPanel.add(searchMethodText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        searchStrLabel = new JLabel();
        searchStrLabel.setText("String");
        searchInnerPanel.add(searchStrLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        searchStrText = new JTextField();
        searchInnerPanel.add(searchStrText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        startSearchButton = new JButton();
        startSearchButton.setText("Start Search");
        searchInnerPanel.add(startSearchButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logoLabel = new JLabel();
        logoLabel.setText("");
        searchInnerPanel.add(logoLabel, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        soPanel = new JPanel();
        soPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        searchOptionsPanel.add(soPanel, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        soPanel.setBorder(BorderFactory.createTitledBorder(null, "Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        likeSearchRadioButton = new JRadioButton();
        likeSearchRadioButton.setText("like search (模糊搜索模式)");
        soPanel.add(likeSearchRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        equalsSearchRadioButton = new JRadioButton();
        equalsSearchRadioButton.setText("equals search (精确搜索模式)");
        soPanel.add(equalsSearchRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        blackListPanel = new JPanel();
        blackListPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        soPanel.add(blackListPanel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        blackScroll = new JScrollPane();
        blackListPanel.add(blackScroll, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 100), new Dimension(-1, 100), new Dimension(-1, 100), 0, false));
        blackScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        blackArea = new JTextArea();
        blackArea.setBackground(new Color(-12895429));
        Font blackAreaFont = this.$$$getFont$$$("Consolas", -1, -1, blackArea.getFont());
        if (blackAreaFont != null) blackArea.setFont(blackAreaFont);
        blackArea.setForeground(new Color(-16711931));
        blackScroll.setViewportView(blackArea);
        classBlackLabel = new JLabel();
        classBlackLabel.setText("Class Name Black List (split by ; and \\n)");
        blackListPanel.add(classBlackLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        callPanel = new JPanel();
        callPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("call", callPanel);
        callerPanel = new JPanel();
        callerPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        callPanel.add(callerPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(550, 300), null, null, 0, false));
        callerPanel.setBorder(BorderFactory.createTitledBorder(null, "Caller", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        callerScroll = new JScrollPane();
        callerPanel.add(callerScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        callerList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        callerList.setModel(defaultListModel1);
        callerScroll.setViewportView(callerList);
        calleePanel = new JPanel();
        calleePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        callPanel.add(calleePanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(550, 300), null, null, 0, false));
        calleePanel.setBorder(BorderFactory.createTitledBorder(null, "Callee", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        calleeScroll = new JScrollPane();
        calleePanel.add(calleeScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        calleeList = new JList();
        calleeScroll.setViewportView(calleeList);
        methodImplPanel = new JPanel();
        methodImplPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("impl", methodImplPanel);
        implScroll = new JScrollPane();
        methodImplPanel.add(implScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        implScroll.setBorder(BorderFactory.createTitledBorder(null, "Method Impl", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        methodImplList = new JList();
        implScroll.setViewportView(methodImplList);
        superImplScroll = new JScrollPane();
        methodImplPanel.add(superImplScroll, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        superImplScroll.setBorder(BorderFactory.createTitledBorder(null, "Super Impl", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        superImplList = new JList();
        superImplScroll.setViewportView(superImplList);
        springPanel = new JPanel();
        springPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("spring", springPanel);
        springCPanel = new JPanel();
        springCPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        springPanel.add(springCPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        springCPanel.setBorder(BorderFactory.createTitledBorder(null, "Spring Controllers", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scScroll = new JScrollPane();
        springCPanel.add(scScroll, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        springCList = new JList();
        scScroll.setViewportView(springCList);
        refreshButton = new JButton();
        refreshButton.setText("Refresh");
        springCPanel.add(refreshButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        springLabel = new JLabel();
        springLabel.setText(" Analyze Spring Controllers and Mappings in Jar/Jars");
        springCPanel.add(springLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pathSearchButton = new JButton();
        pathSearchButton.setText("Search");
        springCPanel.add(pathSearchButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pathSearchLabel = new JLabel();
        pathSearchLabel.setText(" Search path in all Mappings");
        springCPanel.add(pathSearchLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pathSearchTextField = new JTextField();
        pathSearchTextField.setToolTipText("");
        springCPanel.add(pathSearchTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        springMPanel = new JPanel();
        springMPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        springPanel.add(springMPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        springMPanel.setBorder(BorderFactory.createTitledBorder(null, "Spring Mappings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        smScroll = new JScrollPane();
        springMPanel.add(smScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        springMList = new JList();
        smScroll.setViewportView(springMList);
        notePanel = new JPanel();
        notePanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("note", notePanel);
        hisScroll = new JScrollPane();
        notePanel.add(hisScroll, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        hisScroll.setBorder(BorderFactory.createTitledBorder(null, "history", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        historyList = new JList();
        final DefaultListModel defaultListModel2 = new DefaultListModel();
        historyList.setModel(defaultListModel2);
        hisScroll.setViewportView(historyList);
        favScroll = new JScrollPane();
        notePanel.add(favScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        favScroll.setBorder(BorderFactory.createTitledBorder(null, "favorites", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        favList = new JList();
        favScroll.setViewportView(favList);
        scaPanel = new JPanel();
        scaPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("SCA", scaPanel);
        modulePanel = new JPanel();
        modulePanel.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        scaPanel.add(modulePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Module", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scaLog4jBox = new JCheckBox();
        scaLog4jBox.setText("Apache Log4j2");
        modulePanel.add(scaLog4jBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaShiroBox = new JCheckBox();
        scaShiroBox.setText("Apache Shiro");
        modulePanel.add(scaShiroBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaTipPanel = new JPanel();
        scaTipPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        modulePanel.add(scaTipPanel, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scaTipLabel = new JLabel();
        scaTipLabel.setText(" not recommended to enable multiple modules in one analysis");
        scaTipPanel.add(scaTipLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaFastjsonBox = new JCheckBox();
        scaFastjsonBox.setText("FASTJSON");
        modulePanel.add(scaFastjsonBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        scaPanel.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scaActionPanel = new JPanel();
        scaActionPanel.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        scaPanel.add(scaActionPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scaActionPanel.setBorder(BorderFactory.createTitledBorder(null, "Action", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scaFileLabel = new JLabel();
        scaFileLabel.setText("JAR FILE / JAR DIR");
        scaActionPanel.add(scaFileLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        scaFileText = new JTextField();
        scaActionPanel.add(scaFileText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        scaOpenBtn = new JButton();
        scaOpenBtn.setText("OPEN");
        scaActionPanel.add(scaOpenBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scanConsoleScroll = new JScrollPane();
        scaActionPanel.add(scanConsoleScroll, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        scaActionPanel.add(scaOutLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        scaOutPanel = new JPanel();
        scaOutPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        scaActionPanel.add(scaOutPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scaOutConsoleRadio = new JRadioButton();
        scaOutConsoleRadio.setText("CONSOLE");
        scaOutPanel.add(scaOutConsoleRadio, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaOutHtmlRadio = new JRadioButton();
        scaOutHtmlRadio.setText("HTML");
        scaOutPanel.add(scaOutHtmlRadio, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaStartBtn = new JButton();
        scaStartBtn.setText("START");
        scaActionPanel.add(scaStartBtn, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        outputFileLabel = new JLabel();
        outputFileLabel.setText("OUTPUT FILE");
        scaActionPanel.add(outputFileLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        outputFileText = new JTextField();
        scaActionPanel.add(outputFileText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        scaResultOpenBtn = new JButton();
        scaResultOpenBtn.setText("OPEN");
        scaActionPanel.add(scaResultOpenBtn, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        advancePanel = new JPanel();
        advancePanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("advance", advancePanel);
        javaVulSearchPanel = new JPanel();
        javaVulSearchPanel.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
        advancePanel.add(javaVulSearchPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        javaVulSearchPanel.setBorder(BorderFactory.createTitledBorder(null, "Java Vulnerability", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        JNDIButton = new JButton();
        JNDIButton.setText("JNDI");
        javaVulSearchPanel.add(JNDIButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        runtimeExecButton = new JButton();
        runtimeExecButton.setText("Runtime exec");
        javaVulSearchPanel.add(runtimeExecButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processBuilderStartButton = new JButton();
        processBuilderStartButton.setText("ProcessBuilder start");
        javaVulSearchPanel.add(processBuilderStartButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spELGetValueButton = new JButton();
        spELGetValueButton.setText("SpEL getValue");
        javaVulSearchPanel.add(spELGetValueButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        readObjectButton = new JButton();
        readObjectButton.setText("readObject");
        javaVulSearchPanel.add(readObjectButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scriptEngineEvalButton = new JButton();
        scriptEngineEvalButton.setText("ScriptEngine eval");
        javaVulSearchPanel.add(scriptEngineEvalButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        BCELLoadClassButton = new JButton();
        BCELLoadClassButton.setText("BCEL loadClass");
        javaVulSearchPanel.add(BCELLoadClassButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        defineClassButton = new JButton();
        defineClassButton.setText("defineClass");
        javaVulSearchPanel.add(defineClassButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OGNLGetValueButton = new JButton();
        OGNLGetValueButton.setText("OGNL getValue");
        javaVulSearchPanel.add(OGNLGetValueButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        javaVulLabel = new JLabel();
        javaVulLabel.setText("Quickly Search Commons Java Vulnerabilities Call");
        javaVulSearchPanel.add(javaVulLabel, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fastjsonButton = new JButton();
        fastjsonButton.setText("Fastjson");
        javaVulSearchPanel.add(fastjsonButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unzipButton = new JButton();
        unzipButton.setText("Unzip Code");
        javaVulSearchPanel.add(unzipButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hessianButton = new JButton();
        hessianButton.setText("Hessian readObject");
        javaVulSearchPanel.add(hessianButton, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        freeMarkerButton = new JButton();
        freeMarkerButton.setText("FreeMarker");
        javaVulSearchPanel.add(freeMarkerButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        JEXLButton = new JButton();
        JEXLButton.setText("JEXL eval");
        javaVulSearchPanel.add(JEXLButton, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rhinoEvalButton = new JButton();
        rhinoEvalButton.setText("Rhino eval");
        javaVulSearchPanel.add(rhinoEvalButton, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aviatorExecuteButton = new JButton();
        aviatorExecuteButton.setText("Aviator execute");
        javaVulSearchPanel.add(aviatorExecuteButton, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mvelEvalButton = new JButton();
        mvelEvalButton.setText("MVEL eval");
        javaVulSearchPanel.add(mvelEvalButton, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        qlExpressButton = new JButton();
        qlExpressButton.setText("QLExpress");
        javaVulSearchPanel.add(qlExpressButton, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sqlExecNoPrepareButton = new JButton();
        sqlExecNoPrepareButton.setText("SQL exec (no prepare)");
        javaVulSearchPanel.add(sqlExecNoPrepareButton, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sqlExecButton = new JButton();
        sqlExecButton.setText("SQL exec");
        javaVulSearchPanel.add(sqlExecButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xStreamButton = new JButton();
        xStreamButton.setText("XStream");
        javaVulSearchPanel.add(xStreamButton, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        advancePanel.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        piPanel = new JPanel();
        piPanel.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        advancePanel.add(piPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        piPanel.setBorder(BorderFactory.createTitledBorder(null, "Plugins", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        encoderLabel = new JLabel();
        encoderLabel.setText("A tool for encode/decode encrypt/decrypt operations");
        piPanel.add(encoderLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        encoderBtn = new JButton();
        encoderBtn.setText("Start");
        piPanel.add(encoderBtn, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        repeaterLabel = new JLabel();
        repeaterLabel.setText("A tool for sending http request and get response");
        piPanel.add(repeaterLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        repeaterBtn = new JButton();
        repeaterBtn.setText("Start");
        piPanel.add(repeaterBtn, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listenerLabel = new JLabel();
        listenerLabel.setText("A tool for listening port and send by socket");
        piPanel.add(listenerLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listenerBtn = new JButton();
        listenerBtn.setText("Start");
        piPanel.add(listenerBtn, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sqliteButton = new JButton();
        sqliteButton.setText("Start");
        piPanel.add(sqliteButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sqliteLabel = new JLabel();
        sqliteLabel.setText("A tool for run custom query in SQLite database");
        piPanel.add(sqliteLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spelLabel = new JLabel();
        spelLabel.setText("A tool for Spring EL search");
        piPanel.add(spelLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        springELStartButton = new JButton();
        springELStartButton.setText("Start");
        piPanel.add(springELStartButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        obfLabel = new JLabel();
        obfLabel.setText("A tool for obfuscate java serialization data");
        piPanel.add(obfLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        obfBtn = new JButton();
        obfBtn.setText("Start");
        piPanel.add(obfBtn, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serUtilLabel = new JLabel();
        serUtilLabel.setText("A tool for bytecodes in Java Serialization Data");
        piPanel.add(serUtilLabel, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serUtilBtn = new JButton();
        serUtilBtn.setText("Start");
        piPanel.add(serUtilBtn, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        analysis = new JPanel();
        analysis.setLayout(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), -1, -1));
        advancePanel.add(analysis, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        analysis.setBorder(BorderFactory.createTitledBorder(null, "Analysis", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        frameBtn = new JButton();
        frameBtn.setText("Full Frame");
        analysis.add(frameBtn, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(150, -1), 0, false));
        cfgBtn = new JButton();
        cfgBtn.setText("Show CFG");
        analysis.add(cfgBtn, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(150, -1), 0, false));
        simpleFrameButton = new JButton();
        simpleFrameButton.setText("Simple Frame");
        analysis.add(simpleFrameButton, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(150, -1), 0, false));
        htmlGraphBtn = new JButton();
        htmlGraphBtn.setText("HTML Graph");
        analysis.add(htmlGraphBtn, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(150, -1), 0, false));
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        corePanel.add(leftPanel, new GridConstraints(0, 0, 4, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        treeScrollPanel = new JScrollPane();
        leftPanel.add(treeScrollPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(250, 800), new Dimension(250, 800), null, 0, false));
        fileTree = new FileTree();
        treeScrollPanel.setViewportView(fileTree);
        fileTreeSearchPanel = new JPanel();
        fileTreeSearchPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        fileTreeSearchPanel.setVisible(false);
        leftPanel.add(fileTreeSearchPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        searchFileNamePanel = new JPanel();
        searchFileNamePanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        fileTreeSearchPanel.add(searchFileNamePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        searchFileNamePanel.setBorder(BorderFactory.createTitledBorder(null, "File Name (press 'ENTER' to next)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        fileTreeSearchTextField = new JTextField();
        fileTreeSearchTextField.setVisible(true);
        searchFileNamePanel.add(fileTreeSearchTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        fileTreeSearchLabel = new JLabel();
        fileTreeSearchLabel.setText("");
        fileTreeSearchLabel.setVisible(false);
        searchFileNamePanel.add(fileTreeSearchLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logPanel = new JPanel();
        logPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        corePanel.add(logPanel, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        logScroll = new JScrollPane();
        logPanel.add(logScroll, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(500, 100), new Dimension(500, 100), null, 0, false));
        logScroll.setBorder(BorderFactory.createTitledBorder(null, "Log", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        logArea = new JTextPane();
        logArea.setBackground(new Color(-13619152));
        logArea.setEditable(false);
        logArea.setForeground(new Color(-16012544));
        logArea.setText("");
        logScroll.setViewportView(logArea);
        curMethodPanel = new JPanel();
        curMethodPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        logPanel.add(curMethodPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(600, 150), new Dimension(600, 150), null, 0, false));
        allMethodScroll = new JScrollPane();
        curMethodPanel.add(allMethodScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        allMethodList = new JList();
        allMethodScroll.setViewportView(allMethodList);
        curPanel = new JPanel();
        curPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        corePanel.add(curPanel, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        curPanel.setBorder(BorderFactory.createTitledBorder(null, "Current", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        curClassLabel = new JLabel();
        curClassLabel.setText("Class");
        curPanel.add(curClassLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        curClassText = new JTextField();
        curClassText.setEditable(false);
        curPanel.add(curClassText, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        curMethodLabel = new JLabel();
        curMethodLabel.setText("Method");
        curPanel.add(curMethodLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        curMethodText = new JTextField();
        curMethodText.setEditable(false);
        curPanel.add(curMethodText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        curJarLabel = new JLabel();
        curJarLabel.setText("Jar");
        curPanel.add(curJarLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        curJarText = new JTextField();
        curJarText.setEditable(false);
        curPanel.add(curJarText, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        addToFavoritesButton = new JButton();
        addToFavoritesButton.setText("add to favorites");
        curPanel.add(addToFavoritesButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
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
    public JComponent $$$getRootComponent$$$() {
        return masterPanel;
    }

}

package me.n1ar4.jar.analyzer.engine;

import me.n1ar4.jar.analyzer.el.ResObj;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.render.AllMethodsRender;
import me.n1ar4.jar.analyzer.gui.util.ListParser;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.gui.util.MenuUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class CoreHelper {
    public static void refreshAllMethods(String className) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getMethodsByClass(className);
        if (results.size() == 0) {
            results = MainForm.getEngine().getMethodsByClassNoJar(className);
        }
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            if (result.getMethodName().startsWith("access$")) {
                continue;
            }
            methodsList.addElement(result);
        }
        MainForm.getInstance().getAllMethodList().setCellRenderer(new AllMethodsRender());
        MainForm.getInstance().getAllMethodList().setModel(methodsList);
        MainForm.getInstance().getAllMethodList().repaint();
        MainForm.getInstance().getAllMethodList().revalidate();
    }

    public static void refreshCallers(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getCallers(className, methodName, methodDesc);
        if (MenuUtil.sortedByMethod()) {
            results.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            results.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            methodsList.addElement(result);
        }
        MainForm.getInstance().getCallerList().setModel(methodsList);
        MainForm.getInstance().getCallerList().repaint();
        MainForm.getInstance().getCallerList().revalidate();
    }

    public static void refreshImpls(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getImpls(className, methodName, methodDesc);
        if (MenuUtil.sortedByMethod()) {
            results.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            results.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            methodsList.addElement(result);
        }
        MainForm.getInstance().getMethodImplList().setModel(methodsList);
        MainForm.getInstance().getMethodImplList().repaint();
        MainForm.getInstance().getMethodImplList().revalidate();
    }

    public static void refreshSuperImpls(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getSuperImpls(className, methodName, methodDesc);
        if (MenuUtil.sortedByMethod()) {
            results.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            results.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            methodsList.addElement(result);
        }
        MainForm.getInstance().getSuperImplList().setModel(methodsList);
        MainForm.getInstance().getSuperImplList().repaint();
        MainForm.getInstance().getSuperImplList().revalidate();
    }

    public static void refreshCallee(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getCallee(className, methodName, methodDesc);
        if (MenuUtil.sortedByMethod()) {
            results.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            results.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            methodsList.addElement(result);
        }
        MainForm.getInstance().getCalleeList().setModel(methodsList);
        MainForm.getInstance().getCalleeList().repaint();
        MainForm.getInstance().getCalleeList().revalidate();
    }

    public static void refreshSpringC() {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<ClassResult> results = MainForm.getEngine().getAllSpringC();
        results.sort(Comparator.comparing(ClassResult::getClassName));
        DefaultListModel<ClassResult> springCModel = new DefaultListModel<>();
        for (ClassResult result : results) {
            springCModel.addElement(result);
        }
        MainForm.getInstance().getSpringCList().setModel(springCModel);
    }

    public static void pathSearchC() {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        String text = MainForm.getInstance().getPathSearchTextField().getText();
        if (StringUtil.isNull(text)) {
            LogUtil.info("please enter the search keyword");
            return;
        }
        DefaultListModel<ClassResult> springCModelvar0 = new DefaultListModel<>();
        MainForm.getInstance().getSpringCList().setModel(springCModelvar0);
        ArrayList<ClassResult> results = MainForm.getEngine().getAllSpringC();
        HashSet<ClassResult> classResults = new HashSet<>();
        ArrayList<MethodResult> methodResultsTotal = new ArrayList<>();
        results.forEach(result -> {
            ArrayList<MethodResult> methodResults = MainForm.getEngine().getSpringM(result.getClassName());
            List<MethodResult> collect = methodResults.stream().filter(a -> a.getPath().contains(text)).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                classResults.add(result);
                methodResultsTotal.addAll(collect);
            }
        });
        methodResultsTotal.sort(Comparator.comparing(MethodResult::getPath));
        classResults.stream().sorted(Comparator.comparing(ClassResult::getClassName));
        DefaultListModel<MethodResult> springMModel = new DefaultListModel<>();
        for (ClassResult result : classResults) {
            springCModelvar0.addElement(result);
        }
        for (MethodResult result : methodResultsTotal) {
            springMModel.addElement(result);
        }
        LogUtil.info("total spring controller records ：" + springCModelvar0.size());
        LogUtil.info("total path method records ：" + springMModel.size());
        MainForm.getInstance().getSpringMList().setModel(springMModel);
        MainForm.getInstance().getSpringCList().setModel(springCModelvar0);
    }

    public static void refreshSpringM(String className) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getSpringM(className);
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> springCModel = new DefaultListModel<>();
        for (MethodResult result : results) {
            springCModel.addElement(result);
        }
        MainForm.getInstance().getSpringMList().setModel(springCModel);
    }

    public static void refreshCallSearch(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        // java.lang.String java/lang/String
        if (className != null) {
            className = className.replace(".", "/");
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getCallers(className, methodName, methodDesc);

        // BALCK LIST
        ArrayList<String> bl = ListParser.parse(MainForm.getInstance().getBlackArea().getText());
        ArrayList<MethodResult> newReulst = new ArrayList<>();
        for (MethodResult m : results) {
            boolean filtered = false;
            for (String b : bl) {
                if (m.getClassName().equals(b)) {
                    filtered = true;
                    break;
                }
            }
            if (!filtered) {
                newReulst.add(m);
            }
        }

        if (MenuUtil.sortedByMethod()) {
            newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            newReulst.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }

        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
            return;
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();

        MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);

        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                String.format("result number: %d", methodsList.size()));
    }

    public static void refreshCallSearchList(List<SearchCondition> conditions) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<MethodResult> totalResults = new ArrayList<>();
        for (SearchCondition condition : conditions) {
            String className = condition.getClassName();
            String methodName = condition.getMethodName();
            String methodDesc = condition.getMethodDesc();
            // java.lang.String java/lang/String
            if (className != null) {
                className = className.replace(".", "/");
            }
            ArrayList<MethodResult> results = MainForm.getEngine().getCallers(className, methodName, methodDesc);
            totalResults.addAll(results);
        }
        // BALCK LIST
        ArrayList<String> bl = ListParser.parse(MainForm.getInstance().getBlackArea().getText());
        ArrayList<MethodResult> newReulst = new ArrayList<>();
        for (MethodResult m : totalResults) {
            boolean filtered = false;
            for (String b : bl) {
                if (m.getClassName().equals(b)) {
                    filtered = true;
                    break;
                }
            }
            if (!filtered) {
                newReulst.add(m);
            }
        }

        if (MenuUtil.sortedByMethod()) {
            newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            newReulst.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }

        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
            return;
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();

        MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);

        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                String.format("result number: %d", methodsList.size()));
    }

    public static void refreshDefSearch(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        // java.lang.String java/lang/String
        if (className != null) {
            className = className.replace(".", "/");
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getMethod(className, methodName, methodDesc);

        // BALCK LIST
        ArrayList<String> bl = ListParser.parse(MainForm.getInstance().getBlackArea().getText());
        ArrayList<MethodResult> newReulst = new ArrayList<>();
        for (MethodResult m : results) {
            boolean filtered = false;
            for (String b : bl) {
                if (m.getClassName().equals(b)) {
                    filtered = true;
                    break;
                }
            }
            if (!filtered) {
                newReulst.add(m);
            }
        }

        if (MenuUtil.sortedByMethod()) {
            newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            newReulst.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }

        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
            return;
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();

        MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);

        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                String.format("result number: %d", methodsList.size()));
    }

    public static void refreshStrSearch(String val) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getMethodsByStr(val);

        // BALCK LIST
        ArrayList<String> bl = ListParser.parse(MainForm.getInstance().getBlackArea().getText());
        ArrayList<MethodResult> newReulst = new ArrayList<>();
        for (MethodResult m : results) {
            boolean filtered = false;
            for (String b : bl) {
                if (m.getClassName().equals(b)) {
                    filtered = true;
                    break;
                }
            }
            if (!filtered) {
                newReulst.add(m);
            }
        }

        if (MenuUtil.sortedByMethod()) {
            newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            newReulst.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }

        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
            return;
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();

        MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);

        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                String.format("result number: %d", methodsList.size()));
    }

    public static void refreshHistory(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        DefaultListModel<MethodResult> methodsList = MainForm.getHistoryListData();
        MethodResult methodResult = new MethodResult();
        methodResult.setClassName(className);
        methodResult.setMethodName(methodName);
        methodResult.setMethodDesc(methodDesc);
        methodsList.addElement(methodResult);
        MainForm.getInstance().getHistoryList().repaint();
        MainForm.getInstance().getHistoryList().revalidate();
    }

    public static void refreshCallSearchLike(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        // java.lang.String java/lang/String
        if (className != null) {
            className = className.replace(".", "/");
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getCallersLike(className, methodName, methodDesc);

        // BALCK LIST
        ArrayList<String> bl = ListParser.parse(MainForm.getInstance().getBlackArea().getText());
        ArrayList<MethodResult> newReulst = new ArrayList<>();
        for (MethodResult m : results) {
            boolean filtered = false;
            for (String b : bl) {
                if (m.getClassName().equals(b)) {
                    filtered = true;
                    break;
                }
            }
            if (!filtered) {
                newReulst.add(m);
            }
        }

        if (MenuUtil.sortedByMethod()) {
            newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            newReulst.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }

        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
            return;
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();

        MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);

        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                String.format("result number: %d", methodsList.size()));
    }

    public static void refreshDefSearchLike(String className, String methodName, String methodDesc) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        // java.lang.String java/lang/String
        if (className != null) {
            className = className.replace(".", "/");
        }
        ArrayList<MethodResult> results = MainForm.getEngine().getMethodLike(className, methodName, methodDesc);

        // BALCK LIST
        ArrayList<String> bl = ListParser.parse(MainForm.getInstance().getBlackArea().getText());
        ArrayList<MethodResult> newReulst = new ArrayList<>();
        for (MethodResult m : results) {
            boolean filtered = false;
            for (String b : bl) {
                if (m.getClassName().equals(b)) {
                    filtered = true;
                    break;
                }
            }
            if (!filtered) {
                newReulst.add(m);
            }
        }

        if (MenuUtil.sortedByMethod()) {
            newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        } else if (MenuUtil.sortedByClass()) {
            newReulst.sort(Comparator.comparing(MethodResult::getClassName));
        } else {
            throw new RuntimeException("invalid sort");
        }

        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
            return;
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();

        MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);

        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                String.format("result number: %d", methodsList.size()));
    }

    public static void refreshMethods(List<ResObj> methods) {
        if (MainForm.getInstance().getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }
        if (methods.isEmpty()) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
            return;
        }

        List<MethodResult> methodResultList = methods.stream().map(new Function<ResObj, MethodResult>() {
            @Override
            public MethodResult apply(ResObj resObj) {
                MethodResult methodResult = new MethodResult();
                methodResult.setClassName(resObj.getClassName());
                methodResult.setMethodName(resObj.getMethod().getName());
                methodResult.setMethodDesc(resObj.getMethod().getDesc());
                return methodResult;
            }
        }).collect(Collectors.toList());

        if (MenuUtil.sortedByMethod()) {
            methodResultList.sort(Comparator.comparing(MethodResult::getMethodName));
        }
        if (MenuUtil.sortedByClass()) {
            methodResultList.sort(Comparator.comparing(MethodResult::getClassName));
        }

        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        methodResultList.forEach(methodsList::addElement);

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();

        MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);

        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                String.format("result number: %d", methodsList.size()));
    }
}

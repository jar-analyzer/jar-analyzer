package me.n1ar4.jar.analyzer.engine;

import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.render.AllMethodsRender;
import me.n1ar4.jar.analyzer.gui.util.ListParser;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("all")
public class CoreHelper {
    public static void refreshAllMethods(String className) {
        ArrayList<MethodResult> results = MainForm.getEngine().getMethodsByClass(className);
        if(results.size()==0){
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
        ArrayList<MethodResult> results = MainForm.getEngine().getCallers(className, methodName, methodDesc);
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            methodsList.addElement(result);
        }
        MainForm.getInstance().getCallerList().setModel(methodsList);
        MainForm.getInstance().getCallerList().repaint();
        MainForm.getInstance().getCallerList().revalidate();
    }

    public static void refreshImpls(String className, String methodName, String methodDesc) {
        ArrayList<MethodResult> results = MainForm.getEngine().getImpls(className, methodName, methodDesc);
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            methodsList.addElement(result);
        }
        MainForm.getInstance().getMethodImplList().setModel(methodsList);
        MainForm.getInstance().getMethodImplList().repaint();
        MainForm.getInstance().getMethodImplList().revalidate();
    }

    public static void refreshSuperImpls(String className, String methodName, String methodDesc) {
        ArrayList<MethodResult> results = MainForm.getEngine().getSuperImpls(className, methodName, methodDesc);
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            methodsList.addElement(result);
        }
        MainForm.getInstance().getSuperImplList().setModel(methodsList);
        MainForm.getInstance().getSuperImplList().repaint();
        MainForm.getInstance().getSuperImplList().revalidate();
    }

    public static void refreshCallee(String className, String methodName, String methodDesc) {
        ArrayList<MethodResult> results = MainForm.getEngine().getCallee(className, methodName, methodDesc);
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : results) {
            methodsList.addElement(result);
        }
        MainForm.getInstance().getCalleeList().setModel(methodsList);
        MainForm.getInstance().getCalleeList().repaint();
        MainForm.getInstance().getCalleeList().revalidate();
    }

    public static void refreshSpringC() {
        ArrayList<ClassResult> results = MainForm.getEngine().getAllSpringC();
        results.sort(Comparator.comparing(ClassResult::getClassName));
        DefaultListModel<ClassResult> springCModel = new DefaultListModel<>();
        for (ClassResult result : results) {
            springCModel.addElement(result);
        }
        MainForm.getInstance().getSpringCList().setModel(springCModel);
    }

    public static void refreshSpringM(String className) {
        ArrayList<MethodResult> results = MainForm.getEngine().getSpringM(className);
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> springCModel = new DefaultListModel<>();
        for (MethodResult result : results) {
            springCModel.addElement(result);
        }
        MainForm.getInstance().getSpringMList().setModel(springCModel);
    }

    public static void refreshCallSearch(String className, String methodName, String methodDesc) {
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

        newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();
    }

    public static void refreshCallSearchList(List<SearchCondition> conditions) {
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

        newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();
    }

    public static void refreshDefSearch(String className, String methodName, String methodDesc) {
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

        newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();
    }

    public static void refreshStrSearch(String val) {
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

        newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();
    }

    public static void refreshHistory(String className, String methodName, String methodDesc) {
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

        newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();
    }

    public static void refreshDefSearchLike(String className, String methodName, String methodDesc) {
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

        newReulst.sort(Comparator.comparing(MethodResult::getMethodName));
        DefaultListModel<MethodResult> methodsList = new DefaultListModel<>();
        for (MethodResult result : newReulst) {
            methodsList.addElement(result);
        }

        if (methodsList.isEmpty() || methodsList.size() == 0) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "result is null");
        }

        MainForm.getInstance().getSearchList().setModel(methodsList);
        MainForm.getInstance().getSearchList().repaint();
        MainForm.getInstance().getSearchList().revalidate();
    }
}

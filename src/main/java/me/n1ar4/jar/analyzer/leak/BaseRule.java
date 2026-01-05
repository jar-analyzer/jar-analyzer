/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.leak;

import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class BaseRule {
    static List<String> matchGroup0(String regex, String input) {
        String val;
        if (MainForm.getInstance().getLeakDetBase64Box().isSelected()) {
            try {
                val = new String(Base64.getDecoder().decode(input));
            } catch (Exception ignored) {
                val = input;
            }
        } else {
            val = input;
        }
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(val);
        while (matcher.find()) {
            results.add(matcher.group().trim());
        }
        return results;
    }

    public static List<String> matchGroup1(String regex, String input) {
        String val;
        if (MainForm.getInstance().getLeakDetBase64Box().isSelected()) {
            try {
                val = new String(Base64.getDecoder().decode(input));
            } catch (Exception ignored) {
                val = input;
            }
        } else {
            val = input;
        }
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(val);
        while (matcher.find()) {
            results.add(matcher.group(1).trim());
        }
        return results;
    }

    // 添加支持获取第二个捕获组的方法
    public static List<String> matchGroup2(String regex, String input) {
        String val;
        if (MainForm.getInstance().getLeakDetBase64Box().isSelected()) {
            try {
                val = new String(Base64.getDecoder().decode(input));
            } catch (Exception ignored) {
                val = input;
            }
        } else {
            val = input;
        }
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(val);
        while (matcher.find()) {
            if (matcher.groupCount() >= 2) {
                results.add(matcher.group(2).trim());
            }
        }
        return results;
    }

    // 添加多正则匹配方法 - Java 8 兼容版本
    public static List<String> matchMultipleRegex(List<String> regexList, String input) {
        List<String> results = new ArrayList<>();
        for (String regex : regexList) {
            results.addAll(matchGroup1(regex, input));
        }
        // Java 8 兼容写法
        return results.stream().distinct().collect(Collectors.toList());
    }

    // 添加验证方法
    public static boolean isValidMatch(String pattern, String input) {
        Pattern p = Pattern.compile(pattern);
        return p.matcher(input).matches();
    }

    // 添加敏感度配置
    public static List<String> matchWithSensitivity(String regex, String input, boolean caseSensitive) {
        String val = preprocessInput(input);
        List<String> results = new ArrayList<>();
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(val);
        while (matcher.find()) {
            results.add(matcher.group(1).trim());
        }
        return results;
    }

    private static String preprocessInput(String input) {
        if (MainForm.getInstance().getLeakDetBase64Box().isSelected()) {
            try {
                return new String(Base64.getDecoder().decode(input));
            } catch (Exception ignored) {
                return input;
            }
        }
        return input;
    }
}

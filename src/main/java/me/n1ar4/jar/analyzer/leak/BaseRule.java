/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
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
}

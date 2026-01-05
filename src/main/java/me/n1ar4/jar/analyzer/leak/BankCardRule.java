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

import java.util.List;

public class BankCardRule {
    // 银行卡号正则（13-19位数字）
    private final static String regex = "([^0-9]|^)((?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3[0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12}))([^0-9]|$)";

    public static List<String> match(String input) {
        return BaseRule.matchGroup1(regex, input);
    }
}
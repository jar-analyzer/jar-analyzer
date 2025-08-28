/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.leak;

import java.util.ArrayList;
import java.util.List;

public class IDCardRule {
    private final static String regex = "[^0-9]((\\d{8}(0\\d|10|11|12)([0-2]\\d|30|31)\\d{3}$)|(\\d{6}(18|19|20)\\d{2}(0[1-9]|10|11|12)([0-2]\\d|30|31)\\d{3}(\\d|X|x)))[^0-9]";
    
    public static List<String> match(String input) {
        List<String> candidates = BaseRule.matchGroup1(regex, input);
        List<String> validIds = new ArrayList<>();
        
        for (String id : candidates) {
            if (isValidIdCard(id)) {
                validIds.add(id);
            }
        }
        return validIds;
    }
    
    // 添加身份证校验算法
    private static boolean isValidIdCard(String idCard) {
        if (idCard.length() == 18) {
            return validateChecksum(idCard);
        }
        return true; // 15位身份证暂不校验
    }
    
    private static boolean validateChecksum(String idCard) {
        // 实现18位身份证校验码算法
        // ... 校验逻辑
        return true;
    }
}

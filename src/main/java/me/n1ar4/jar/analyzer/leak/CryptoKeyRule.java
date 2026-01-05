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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CryptoKeyRule {
    // RSA 私钥格式
    private final static String rsaPrivateKeyRegex = "(-----BEGIN RSA PRIVATE KEY-----[\\s\\S]*?-----END RSA PRIVATE KEY-----)";

    // RSA 公钥格式
    private final static String rsaPublicKeyRegex = "(-----BEGIN RSA PUBLIC KEY-----[\\s\\S]*?-----END RSA PUBLIC KEY-----)";

    // 通用私钥格式
    private final static String privateKeyRegex = "(-----BEGIN PRIVATE KEY-----[\\s\\S]*?-----END PRIVATE KEY-----)";

    // 通用公钥格式
    private final static String publicKeyRegex = "(-----BEGIN PUBLIC KEY-----[\\s\\S]*?-----END PUBLIC KEY-----)";

    // AES 密钥（通常是 Base64 编码的字符串，16/24/32 字节对应 22/32/44 个 Base64 字符）
    private final static String aesKeyRegex = "(?i)(aes[_-]?key|secret[_-]?key|encryption[_-]?key)\\s*[=:]\\s*[\"']?([A-Za-z0-9+/]{22}==|[A-Za-z0-9+/]{32}|[A-Za-z0-9+/]{44})[\"']?";

    // 十六进制格式的密钥（32/48/64 个十六进制字符对应 AES-128/192/256）
    private final static String hexKeyRegex = "(?i)(aes[_-]?key|secret[_-]?key|encryption[_-]?key)\\s*[=:]\\s*[\"']?([a-fA-F0-9]{32}|[a-fA-F0-9]{48}|[a-fA-F0-9]{64})[\"']?";

    // PKCS#8 格式私钥
    private final static String pkcs8PrivateKeyRegex = "(-----BEGIN ENCRYPTED PRIVATE KEY-----[\\s\\S]*?-----END ENCRYPTED PRIVATE KEY-----)";

    // 证书格式
    private final static String certificateRegex = "(-----BEGIN CERTIFICATE-----[\\s\\S]*?-----END CERTIFICATE-----)";

    // 在 match 方法中替换 AES 密钥匹配部分
    public static List<String> match(String input) {
        Set<String> resultSet = new LinkedHashSet<>();

        // 匹配各种密钥格式
        resultSet.addAll(BaseRule.matchGroup1(rsaPrivateKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(rsaPublicKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(privateKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(publicKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(pkcs8PrivateKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(certificateRegex, input));

        // 匹配 AES 密钥（使用 group(2) 获取实际密钥值）
        resultSet.addAll(BaseRule.matchGroup2(aesKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup2(hexKeyRegex, input));

        return new ArrayList<>(resultSet);
    }

    // 验证是否是有效的 Base64 字符串
    private static boolean isValidBase64(String str) {
        try {
            java.util.Base64.getDecoder().decode(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
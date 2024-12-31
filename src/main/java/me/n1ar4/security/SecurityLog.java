/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.security;

import me.n1ar4.jar.analyzer.utils.CommonLogUtil;

public class SecurityLog {
    static void log(String info) {
        CommonLogUtil.log(info, "security");
    }
}

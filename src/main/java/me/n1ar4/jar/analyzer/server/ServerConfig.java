/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.server;

/**
 * HTTP API Server 配置（已固化）
 *
 * <p>自 6.0 起，jar-analyzer HTTP API 不再对外暴露：</p>
 * <ul>
 *   <li>固定绑定 {@value #BIND}（仅本机环回）</li>
 *   <li>固定端口 {@value #PORT}</li>
 *   <li>不再提供 token 鉴权（同机进程才可访问）</li>
 * </ul>
 */
public class ServerConfig {

    /**
     * 固定绑定地址 —— 仅环回，不允许修改
     */
    public static final String BIND = "127.0.0.1";

    /**
     * 固定监听端口 —— 不允许修改
     */
    public static final int PORT = 10032;

    /**
     * @return 绑定地址（恒为 127.0.0.1）
     */
    public String getBind() {
        return BIND;
    }

    /**
     * @return 监听端口（恒为 10032）
     */
    public int getPort() {
        return PORT;
    }
}

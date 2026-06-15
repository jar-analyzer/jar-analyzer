/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * AI 配置加载/保存（多配置版本）
 * <p>
 * 文件格式 (./jar-analyzer-ai.json)：
 * {
 * "activeId": "xxxx",
 * "profiles": [ {AIConfig...}, ... ]
 * }
 * <p>
 * 安全约束：
 * - 配置文件保存在当前工作目录: ./jar-analyzer-ai.json
 * （与项目原有 .jar-analyzer 配置文件位置策略保持一致）
 * - POSIX 系统强制 0600 权限；Windows 走默认 ACL
 * - 日志中绝不输出 apiKey
 */
public class AIConfigManager {
    private static final Logger logger = LogManager.getLogger();

    public static final String CONFIG_FILE_PATH = "jar-analyzer-ai.json";

    private static volatile AIConfigStore cached;

    private AIConfigManager() {
    }

    public static Path getConfigPath() {
        return Paths.get(CONFIG_FILE_PATH);
    }

    /**
     * 读取整个 store。文件不存在时返回空 store；旧版本（仅单条 AIConfig）会被自动迁移。
     */
    public static synchronized AIConfigStore loadStore() {
        if (cached != null) {
            return cached;
        }
        Path path = getConfigPath();
        try {
            if (!Files.exists(path)) {
                cached = new AIConfigStore();
                return cached;
            }
            byte[] data = Files.readAllBytes(path);
            // 先尝试按新格式解析
            AIConfigStore store = null;
            try {
                store = JSON.parseObject(data, AIConfigStore.class);
            } catch (Exception ignored) {
            }
            if (store == null || store.getProfiles() == null) {
                store = new AIConfigStore();
            }
            // 兼容旧版本：如果文件内容看起来像单条 AIConfig（含 baseUrl/model 但 profiles 为空），迁移之
            if (store.getProfiles().isEmpty()) {
                try {
                    AIConfig legacy = JSON.parseObject(data, AIConfig.class);
                    if (legacy != null && legacy.getBaseUrl() != null && !legacy.getBaseUrl().isEmpty()) {
                        if (legacy.getId() == null || legacy.getId().isEmpty()) {
                            legacy.setId(newId());
                        }
                        if (legacy.getName() == null || legacy.getName().isEmpty()) {
                            legacy.setName("默认配置");
                        }
                        store.getProfiles().add(legacy);
                        store.setActiveId(legacy.getId());
                        logger.info("migrated legacy single-config to multi-profile store");
                    }
                } catch (Exception ignored) {
                }
            }
            cached = store;
            return cached;
        } catch (Exception ex) {
            logger.error("load ai config error: {}", ex.toString());
            cached = new AIConfigStore();
            return cached;
        }
    }

    public static synchronized void saveStore(AIConfigStore store) {
        if (store == null) {
            return;
        }
        Path path = getConfigPath();
        try {
            byte[] bytes = JSON.toJSONBytes(store, JSONWriter.Feature.PrettyFormat);
            Files.write(path, bytes);
            // POSIX 文件权限收紧到 0600，避免其他用户读取 API Key
            try {
                Set<PosixFilePermission> perms = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(path, perms);
            } catch (UnsupportedOperationException ignored) {
                // Windows 不支持 POSIX 权限：依赖默认 ACL
            } catch (AccessDeniedException ignored) {
            } catch (IOException ignored) {
            }

            cached = store;
            logger.info("ai config store saved (count={}, activeId={}, apiKey=***REDACTED***)",
                    store.getProfiles().size(), store.getActiveId());
        } catch (Exception ex) {
            logger.error("save ai config error: {}", ex.toString());
        }
    }

    /**
     * 取出当前启用的配置；若无启用则返回一个未配置（isReady=false）的占位
     */
    public static AIConfig getActive() {
        AIConfigStore store = loadStore();
        String aid = store.getActiveId();
        if (aid != null && !aid.isEmpty()) {
            for (AIConfig c : store.getProfiles()) {
                if (aid.equals(c.getId())) {
                    return c;
                }
            }
        }
        return new AIConfig();
    }

    /**
     * 兼容旧 API：返回当前启用的配置（聊天对话框等使用）
     */
    public static AIConfig load() {
        return getActive();
    }

    /**
     * 设置启用项，并立即落盘
     */
    public static synchronized void setActive(String id) {
        AIConfigStore store = loadStore();
        store.setActiveId(id == null ? "" : id);
        saveStore(store);
    }

    /**
     * 新增或更新一条配置（按 id 匹配；id 为空则新建）
     */
    public static synchronized AIConfig upsert(AIConfig cfg) {
        if (cfg == null) {
            return null;
        }
        AIConfigStore store = loadStore();
        if (cfg.getId() == null || cfg.getId().isEmpty()) {
            cfg.setId(newId());
            store.getProfiles().add(cfg);
        } else {
            boolean found = false;
            List<AIConfig> list = store.getProfiles();
            for (int i = 0; i < list.size(); i++) {
                if (cfg.getId().equals(list.get(i).getId())) {
                    list.set(i, cfg);
                    found = true;
                    break;
                }
            }
            if (!found) {
                list.add(cfg);
            }
        }
        saveStore(store);
        return cfg;
    }

    /**
     * 删除一条配置；如果删除的是 active，则清空 activeId
     */
    public static synchronized void remove(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        AIConfigStore store = loadStore();
        List<AIConfig> kept = new ArrayList<>();
        for (AIConfig c : store.getProfiles()) {
            if (!id.equals(c.getId())) {
                kept.add(c);
            }
        }
        store.setProfiles(kept);
        if (id.equals(store.getActiveId())) {
            store.setActiveId("");
        }
        saveStore(store);
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 测试时强制重新读取
     */
    public static synchronized void invalidate() {
        cached = null;
    }
}

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

import java.util.ArrayList;
import java.util.List;

/**
 * 多 AI 配置容器
 * <p>
 * 结构：
 * - profiles: 全部已保存的 AI 配置
 * - activeId: 当前启用的配置 ID（与 profile.id 对应）；为空表示没有启用
 */
public class AIConfigStore {

    private List<AIConfig> profiles = new ArrayList<>();

    private String activeId = "";

    public List<AIConfig> getProfiles() {
        if (profiles == null) {
            profiles = new ArrayList<>();
        }
        return profiles;
    }

    public void setProfiles(List<AIConfig> profiles) {
        this.profiles = profiles == null ? new ArrayList<>() : profiles;
    }

    public String getActiveId() {
        return activeId == null ? "" : activeId;
    }

    public void setActiveId(String activeId) {
        this.activeId = activeId == null ? "" : activeId;
    }
}

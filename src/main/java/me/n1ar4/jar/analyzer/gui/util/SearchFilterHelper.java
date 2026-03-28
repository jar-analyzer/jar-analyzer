/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果过滤工具类
 * 支持黑名单模式和白名单模式两种过滤方式
 * <p>
 * 黑名单模式：过滤掉匹配的类/包（默认行为，向后兼容）
 * 白名单模式：只保留匹配的类/包
 */
public class SearchFilterHelper {

    private static final Logger logger = LogManager.getLogger();

    /**
     * 过滤模式枚举
     */
    public enum FilterMode {
        /**
         * 黑名单模式：排除匹配项（默认）
         */
        BLACKLIST,
        /**
         * 白名单模式：仅保留匹配项
         */
        WHITELIST
    }

    /**
     * 判断类名是否匹配给定的过滤规则列表
     * 支持精确类名匹配和包前缀匹配
     *
     * @param className  待检查的类名（内部格式，使用 / 分隔）
     * @param filterList 过滤规则列表（已由 ListParser 解析，使用 / 分隔）
     * @return 是否匹配任一规则
     */
    private static boolean matchesAnyRule(String className, List<String> filterList) {
        for (String rule : filterList) {
            if (className.equals(rule)) {
                return true;
            }
            String normalizedRule = rule.replace(".", "/");
            if (className.startsWith(normalizedRule)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据当前 UI 设置的过滤模式和过滤列表，对搜索结果进行过滤
     * 自动从 MainForm 读取过滤文本和过滤模式
     *
     * @param results 原始搜索结果
     * @return 过滤后的结果
     */
    public static ArrayList<MethodResult> filter(ArrayList<MethodResult> results) {
        String filterText = MainForm.getInstance().getBlackArea().getText();
        FilterMode mode = MainForm.getInstance().getFilterMode();
        logger.info("search mode: {}", mode.toString());
        return filter(results, filterText, mode);
    }

    /**
     * 根据指定的过滤模式和过滤文本，对搜索结果进行过滤
     *
     * @param results    原始搜索结果
     * @param filterText 过滤规则文本（支持 ListParser 格式）
     * @param mode       过滤模式（黑名单/白名单）
     * @return 过滤后的结果列表
     */
    public static ArrayList<MethodResult> filter(ArrayList<MethodResult> results,
                                                 String filterText,
                                                 FilterMode mode) {
        ArrayList<String> filterList = ListParser.parse(filterText);
        if (filterList.isEmpty()) {
            return new ArrayList<>(results);
        }

        ArrayList<MethodResult> filtered = new ArrayList<>();
        for (MethodResult m : results) {
            boolean matches = matchesAnyRule(m.getClassName(), filterList);
            if (mode == FilterMode.BLACKLIST) {
                // 黑名单模式：不匹配的保留
                if (!matches) {
                    filtered.add(m);
                }
            } else {
                // 白名单模式：匹配的保留
                if (matches) {
                    filtered.add(m);
                }
            }
        }
        return filtered;
    }

    /**
     * 过滤空参方法
     * 如果 UI 选中了排除空参方法的选项，移除方法描述符为 () 的方法
     *
     * @param results 待过滤的结果
     * @return 过滤后的结果
     */
    public static ArrayList<MethodResult> filterNullParam(ArrayList<MethodResult> results) {
        if (!MainForm.getInstance().getNullParamBox().isSelected()) {
            return results;
        }
        ArrayList<MethodResult> filtered = new ArrayList<>();
        for (MethodResult result : results) {
            if (!result.getMethodDesc().contains("()")) {
                filtered.add(result);
            }
        }
        return filtered;
    }
}

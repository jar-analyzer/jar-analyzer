/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gadget;

import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GadgetAnalyzer {
    private static final Logger logger = LogManager.getLogger();
    private final String dir;
    private final boolean enableNative;
    private final boolean enableHessian;
    private final boolean enableFastjson;
    private final boolean enableJdbc;

    public GadgetAnalyzer(String dir, boolean enableN, boolean enableH, boolean enableF, boolean enableJ) {
        this.dir = dir;
        this.enableNative = enableN;
        this.enableHessian = enableH;
        this.enableFastjson = enableF;
        this.enableJdbc = enableJ;
    }

    /**
     * 判断单条规则 JAR 名称是否与目录中某个 JAR 文件匹配
     * <p>
     * 支持三种规则语法：
     * <ul>
     *   <li>精确匹配：commons-collections-3.2.1.jar</li>
     *   <li>通配符匹配（*）：commons-collections-*.jar → 转换为正则 commons-collections-.*\.jar</li>
     *   <li>黑名单排除（!version）：commons-collections-!3.2.2.jar
     *       表示前缀匹配 commons-collections- 且版本不等于 3.2.2</li>
     * </ul>
     *
     * @param ruleJarName 规则中定义的 JAR 名称表达式
     * @param fileNames   目录中实际存在的 JAR 文件名列表
     * @return 是否命中
     */
    private boolean matchJarName(String ruleJarName, List<String> fileNames) {
        if (ruleJarName == null || ruleJarName.isEmpty()) {
            return false;
        }
        // 黑名单排除模式：prefix!excludeVersion.jar
        // 例如 commons-collections-!3.2.2.jar 匹配所有 commons-collections-x.x.x.jar 但排除 3.2.2
        if (ruleJarName.contains("!")) {
            int excIdx = ruleJarName.indexOf('!');
            String prefix = ruleJarName.substring(0, excIdx);
            // excludeVersion 取 ! 之后、.jar 之前的内容
            String afterExcl = ruleJarName.substring(excIdx + 1);
            String excludeVer = afterExcl.endsWith(".jar")
                    ? afterExcl.substring(0, afterExcl.length() - 4)
                    : afterExcl;
            for (String fileName : fileNames) {
                if (fileName.startsWith(prefix)) {
                    // 去掉前缀后再去掉 .jar 后缀得到版本字符串
                    String rest = fileName.substring(prefix.length());
                    String ver = rest.endsWith(".jar") ? rest.substring(0, rest.length() - 4) : rest;
                    if (!ver.equals(excludeVer)) {
                        logger.debug("blacklist match: {} (exclude {})", fileName, excludeVer);
                        return true;
                    }
                }
            }
            return false;
        }
        // 通配符模式：包含 * 时转为正则
        if (ruleJarName.contains("*")) {
            // 先按字面量转义 . 再将 * 替换为 .*
            String regex = ruleJarName
                    .replace(".", "\\.")
                    .replace("\\.*", ".*")   // 还原之前错误转义的 .*
                    .replace("*", ".*");
            for (String fileName : fileNames) {
                if (fileName.matches(regex)) {
                    logger.debug("wildcard match: {} -> rule {}", fileName, ruleJarName);
                    return true;
                }
            }
            return false;
        }
        // 精确匹配（大小写不敏感，兼容 Windows 文件系统）
        for (String fileName : fileNames) {
            if (ruleJarName.equalsIgnoreCase(fileName)) {
                logger.debug("exact match: {}", fileName);
                return true;
            }
        }
        return false;
    }

    public List<GadgetInfo> process() {
        logger.info("start gadget analyzer dir={}", this.dir);
        logger.info("enable: native={} hessian={} fastjson={} jdbc={}",
                this.enableNative, this.enableHessian, this.enableFastjson, this.enableJdbc);

        List<String> files = DirUtil.GetFiles(this.dir);
        if (files == null || files.isEmpty()) {
            logger.warn("no files found in dir: {}", this.dir);
            return new ArrayList<>();
        }

        // 收集目录中所有存在的 .jar 文件名（仅文件名，不含路径）
        List<String> jarFileNames = new ArrayList<>();
        for (String file : files) {
            Path tmp = Paths.get(file);
            if (!Files.exists(tmp)) {
                continue;
            }
            String filename = tmp.toFile().getName();
            if (filename.endsWith(".jar")) {
                jarFileNames.add(filename);
            }
        }
        logger.info("found {} jar files in dir", jarFileNames.size());

        List<GadgetInfo> result = new ArrayList<>();

        for (GadgetInfo rule : GadgetRule.rules) {
            String ruleType = rule.getType();
            // 按类型开关过滤
            if (GadgetInfo.NATIVE_TYPE.equals(ruleType) && !this.enableNative) {
                continue;
            }
            if (GadgetInfo.HESSIAN_TYPE.equals(ruleType) && !this.enableHessian) {
                continue;
            }
            if (GadgetInfo.FASTJSON_TYPE.equals(ruleType) && !this.enableFastjson) {
                continue;
            }
            if (GadgetInfo.JDBC_TYPE.equals(ruleType) && !this.enableJdbc) {
                continue;
            }

            // 规则中所有 JAR 必须全部命中（AND 语义）
            List<String> jarsName = rule.getJarsName();
            boolean allMatched = true;
            for (String jarName : jarsName) {
                if (!matchJarName(jarName, jarFileNames)) {
                    allMatched = false;
                    break;
                }
            }

            if (allMatched) {
                logger.info("hit rule [{}] type={} -> {}", rule.getID(), ruleType, rule.getResult());
                result.add(rule);
            }
        }

        logger.info("gadget analyzer finished, hit {} rules", result.size());
        return result;
    }
}

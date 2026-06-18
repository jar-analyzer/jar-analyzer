/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.report;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 默认的报告存储：写入到 ${tempDir}/reports/yyyyMMdd-HHmmss-uuid.json
 * <p>
 */
public final class ReportStore implements ReportSink {

    private static final Logger logger = LogManager.getLogger();

    private static final String DIR_NAME = "reports";

    private final Path baseDir;

    public ReportStore() {
        Path p = Paths.get(Const.tempDir).resolve(DIR_NAME).normalize().toAbsolutePath();
        this.baseDir = p;
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException ignored) {
        }
    }

    public Path getBaseDir() {
        return baseDir;
    }

    @Override
    public synchronized void save(VulnReport report) {
        if (report == null) {
            return;
        }
        try {
            String ts = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
            String fname = ts + "-" + UUID.randomUUID() + ".json";
            // 仅允许文件名中出现安全字符
            if (!fname.matches("[a-zA-Z0-9.\\-]+")) {
                throw new RuntimeException("invalid file name produced");
            }
            Path target = baseDir.resolve(fname).normalize().toAbsolutePath();
            // 边界检查：防止 baseDir 被符号链接劫持后写出去
            if (!target.startsWith(baseDir)) {
                throw new RuntimeException("path traversal detected");
            }
            byte[] bytes = JSON.toJSONString(report,
                            JSONWriter.Feature.PrettyFormat,
                            JSONWriter.Feature.WriteMapNullValue)
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(target, bytes);
            logger.info("report saved: type={} score={} file={}",
                    report.getType(), report.getScore(), target.getFileName());
        } catch (Throwable t) {
            logger.error("save report error: {}", t.toString());
        }
    }

    /**
     * 列出所有历史报告。
     */
    public List<VulnReport> loadAll() {
        List<VulnReport> list = new ArrayList<>();
        if (!Files.isDirectory(baseDir)) {
            return list;
        }
        try {
            Files.list(baseDir).forEach(p -> {
                try {
                    if (!p.toString().endsWith(".json")) {
                        return;
                    }
                    byte[] data = Files.readAllBytes(p);
                    VulnReport r = JSON.parseObject(data, VulnReport.class);
                    if (r != null) {
                        list.add(r);
                    }
                } catch (Throwable ignored) {
                }
            });
        } catch (IOException ignored) {
        }
        Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return list;
    }
}

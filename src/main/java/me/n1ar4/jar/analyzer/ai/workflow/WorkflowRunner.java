/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import me.n1ar4.jar.analyzer.ai.AIConfig;
import me.n1ar4.jar.analyzer.ai.AIConfigManager;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagContext;
import me.n1ar4.jar.analyzer.ai.workflow.core.NodeStatus;
import me.n1ar4.jar.analyzer.ai.workflow.presets.JarAnalyzerSecurityWorkflow;
import me.n1ar4.jar.analyzer.ai.workflow.report.ReportStore;
import me.n1ar4.jar.analyzer.ai.workflow.report.VulnReport;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.List;

/**
 * 可独立运行的命令行入口，便于测试 / 与外部脚本配合。
 * <p>
 * 用法：
 * <pre>
 *   java -cp jar-analyzer.jar me.n1ar4.jar.analyzer.ai.workflow.WorkflowRunner \
 *        [http://127.0.0.1:10032] [maxClasses] [maxIterations]
 * </pre>
 * 必须先在主程序中：
 * 1. 用 {@code Jar Analyzer GUI} 打开目标 jar 完成索引（启动了 HTTP API Server）
 * 2. 配置好 AI 凭据（jar-analyzer-ai.json）
 */
public final class WorkflowRunner {

    private static final Logger logger = LogManager.getLogger();

    private WorkflowRunner() {
    }

    public static void main(String[] args) {
        String api = (args != null && args.length > 0) ? args[0] : "http://127.0.0.1:10032";
        int maxClasses = (args != null && args.length > 1) ? parseInt(args[1], 50) : 50;
        int maxIters = (args != null && args.length > 2) ? parseInt(args[2], 10) : 10;

        AIConfig cfg = AIConfigManager.getActive();
        if (!cfg.isReady()) {
            System.err.println("[ERROR] AI not configured. Please set up AI in GUI first.");
            System.exit(2);
            return;
        }

        ReportStore store = new ReportStore();
        JarAnalyzerSecurityWorkflow wf = new JarAnalyzerSecurityWorkflow(
                cfg, api, store, maxClasses, maxIters);

        System.out.println("---------------------------------------------------------");
        System.out.println(" Jar Analyzer Security Workflow (Java DAG)");
        System.out.println("   api          : " + api);
        System.out.println("   maxClasses   : " + maxClasses);
        System.out.println("   maxIterations: " + maxIters);
        System.out.println("   model        : " + cfg.getModel());
        System.out.println("   reports dir  : " + store.getBaseDir());
        System.out.println("---------------------------------------------------------");

        wf.run(new DagContext.ProgressListener() {
            @Override
            public void onProgress(String nodeId, NodeStatus status, String message) {
                logger.info("[{}] {} - {}", status, nodeId, message);
            }
        });

        List<VulnReport> reports = wf.getCollectedReports();
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" Done. " + reports.size() + " vulnerability report(s) collected.");
        System.out.println("---------------------------------------------------------");
        if (!reports.isEmpty()) {
            System.out.println(JSON.toJSONString(reports,
                    JSONWriter.Feature.PrettyFormat,
                    JSONWriter.Feature.WriteMapNullValue));
        }
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Throwable t) {
            return def;
        }
    }
}

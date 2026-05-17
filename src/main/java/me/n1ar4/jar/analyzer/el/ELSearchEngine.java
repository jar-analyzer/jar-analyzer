/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el;

import me.n1ar4.jar.analyzer.core.DatabaseManager;
import me.n1ar4.jar.analyzer.core.SqlSessionFactoryUtil;
import me.n1ar4.jar.analyzer.core.mapper.ELSearchMapper;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.ibatis.session.SqlSession;
import org.objectweb.asm.Type;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Single-pass EL search.
 * <p>
 * Pipeline (vs. the legacy per-method DB-pounding loop):
 * <ol>
 *   <li>Ensure indexes + PRAGMAs are in place (once per process).</li>
 *   <li>Build the in-memory inheritance graph (one SQL pass).</li>
 *   <li>Plan the query: push every supported predicate down to one
 *       SQL statement; resolve invoke filters via the reverse
 *       callee index.</li>
 *   <li>Execute the candidate query -- usually a few hundred rows
 *       even for 100k-method jars.</li>
 *   <li>Apply residual Java-side predicates (regex, descriptor-aware
 *       checks).</li>
 * </ol>
 * Threads are no longer needed: SQLite serializes the heavy work in
 * the optimizer anyway, and the residual loop is bounded by the
 * candidate count which is small. Running single-threaded also
 * removes the connection-contention overhead the old executor pool
 * suffered from.
 */
public class ELSearchEngine {
    private static final Logger logger = LogManager.getLogger();

    private final Object value;
    private final JLabel msgLabel;
    private final JButton searchButton;
    private final JButton stopBtn;
    private final JTextArea jTextArea;

    private final AtomicLong processedRows = new AtomicLong(0);
    private long totalRows;
    private long startTime;

    private volatile boolean shouldStop = false;

    public ELSearchEngine(Object value, JLabel msgLabel, JButton searchButton,
                          JButton stopBtn, JTextArea jTextArea) {
        this.value = value;
        this.msgLabel = msgLabel;
        this.searchButton = searchButton;
        this.stopBtn = stopBtn;
        this.jTextArea = jTextArea;

        this.stopBtn.addActionListener(e -> {
            shouldStop = true;
            stopBtn.setEnabled(false);
            msgLabel.setText("正在停止搜索，请等待...");
            logger.info("user requested to stop search");
        });
    }

    public void run() {
        shouldStop = false;
        stopBtn.setEnabled(true);
        searchButton.setEnabled(false);
        startTime = System.currentTimeMillis();
        ConcurrentLinkedQueue<ResObj> hits = new ConcurrentLinkedQueue<>();

        // Progress: SQL stage shows phase string; Java filter stage
        // shows row counter. Only update from the EDT-friendly
        // scheduled tick to avoid event flood.
        ScheduledExecutorService progressExecutor =
                Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "el-search-progress");
                    t.setDaemon(true);
                    return t;
                });
        ScheduledFuture<?> progressTask = progressExecutor.scheduleAtFixedRate(
                this::tickProgress, 250, 500, TimeUnit.MILLISECONDS);

        try {
            DatabaseManager.ensureSearchOptimizations();

            MethodEL condition = (MethodEL) value;

            // Open one session for the whole search. Single-connection
            // means all subsequent queries share prepared-statement
            // cache and avoid DBCP turnover.
            try (SqlSession session = SqlSessionFactoryUtil
                    .sqlSessionFactory.openSession(true)) {

                setMsg("构建继承索引...");
                ELInheritanceIndex inh = ELInheritanceIndex.build(session);
                if (shouldStop) {
                    return;
                }

                setMsg("规划查询...");
                ELQueryPlanner.Plan plan = ELQueryPlanner.plan(condition, session, inh);
                if (plan.impossible) {
                    setMsg("条件不可满足（继承目标不存在 / 调用关系无候选）");
                    logger.info("plan reports impossible -- 0 results");
                    return;
                }
                if (shouldStop) {
                    return;
                }

                setMsg("执行候选集查询...");
                ELSearchMapper mapper = session.getMapper(ELSearchMapper.class);
                List<MethodResult> candidates = mapper.selectCandidates(plan.sqlParams);
                int candCount = candidates == null ? 0 : candidates.size();
                totalRows = candCount;
                logger.info("EL candidate count: {}", candCount);

                if (candCount == 0) {
                    return;
                }

                setMsg("应用残余过滤（共 " + candCount + " 候选）...");
                applyResidualFilters(candidates, plan, hits);
            }
        } catch (Throwable t) {
            // The project's Logger.error(String, Object...) treats the
            // Throwable as a varargs format argument and silently
            // swallows it -- so we serialize the stack ourselves.
            java.io.StringWriter sw = new java.io.StringWriter();
            t.printStackTrace(new java.io.PrintWriter(sw));
            logger.error("EL search failed:\n{}", sw.toString());
            // Surface the whole cause chain so the user sees actionable
            // detail in the status bar, not just a generic message.
            StringBuilder sb = new StringBuilder();
            Throwable cur = t;
            int depth = 0;
            while (cur != null && depth < 5) {
                if (depth > 0) {
                    sb.append(" <- ");
                }
                sb.append(cur.getClass().getSimpleName());
                if (cur.getMessage() != null) {
                    sb.append(": ").append(cur.getMessage());
                }
                cur = cur.getCause();
                depth++;
            }
            setMsg("搜索失败: " + sb);
        } finally {
            progressTask.cancel(false);
            progressExecutor.shutdown();
            searchButton.setEnabled(true);
            stopBtn.setEnabled(false);
            renderFinal(hits);
        }
    }

    /**
     * Walks the SQL candidate list applying the predicates that could
     * not be pushed down. The candidate set is small enough (usually
     * tens to a few thousand) that single-thread iteration is faster
     * than ramping up an executor.
     */
    private void applyResidualFilters(List<MethodResult> candidates,
                                      ELQueryPlanner.Plan plan,
                                      ConcurrentLinkedQueue<ResObj> hits) {
        for (MethodResult r : candidates) {
            if (shouldStop) {
                return;
            }
            String className = r.getClassName();
            String methodName = r.getMethodName();
            String desc = r.getMethodDesc();

            if (className == null || methodName == null || desc == null) {
                processedRows.incrementAndGet();
                continue;
            }

            if (plan.classNameRegex != null
                    && !plan.classNameRegex.matcher(className).matches()) {
                processedRows.incrementAndGet();
                continue;
            }
            if (plan.nameRegex != null
                    && !plan.nameRegex.matcher(methodName).matches()) {
                processedRows.incrementAndGet();
                continue;
            }

            // Descriptor-aware filters parsed lazily.
            Type[] argTypes = null;
            if (plan.paramsNum != null
                    || (plan.paramTypes != null && !plan.paramTypes.isEmpty())) {
                try {
                    argTypes = Type.getArgumentTypes(desc);
                } catch (Throwable t) {
                    processedRows.incrementAndGet();
                    continue;
                }
            }
            if (plan.paramsNum != null
                    && (argTypes == null || argTypes.length != plan.paramsNum)) {
                processedRows.incrementAndGet();
                continue;
            }
            if (plan.paramTypes != null && !plan.paramTypes.isEmpty()) {
                boolean ok = true;
                for (Map.Entry<Integer, String> e : plan.paramTypes.entrySet()) {
                    int idx = e.getKey();
                    if (argTypes == null || idx >= argTypes.length
                            || !e.getValue().equals(argTypes[idx].getClassName())) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) {
                    processedRows.incrementAndGet();
                    continue;
                }
            }
            if (plan.returnType != null && !plan.returnType.isEmpty()) {
                String ret;
                try {
                    ret = Type.getReturnType(desc).getClassName();
                } catch (Throwable t) {
                    processedRows.incrementAndGet();
                    continue;
                }
                if (!ret.equals(plan.returnType)) {
                    processedRows.incrementAndGet();
                    continue;
                }
            }

            // Hit. Build a minimal MethodReference handle for the UI
            // pipeline. We deliberately avoid getAllMethodRef-style
            // annotation enrichment because no downstream consumer
            // looks at the annotations on a search hit.
            MethodReference.Handle handle = new MethodReference.Handle(
                    new me.n1ar4.jar.analyzer.core.reference.ClassReference.Handle(className),
                    methodName, desc);
            int line = r.getLineNumber();
            hits.add(new ResObj(handle, className, line));
            processedRows.incrementAndGet();
        }
    }

    private void tickProgress() {
        long processed = processedRows.get();
        long total = totalRows;
        long elapsed = System.currentTimeMillis() - startTime;
        if (total <= 0) {
            return;
        }
        double pct = (double) processed / total * 100.0;
        ELForm.setVal((int) Math.min(99, 5 + pct * 0.94));
        setMsg(String.format("已处理 %d/%d 候选 (%.1f%%) - 已用时 %s",
                processed, total, pct, formatTime(elapsed)));
    }

    private void renderFinal(ConcurrentLinkedQueue<ResObj> hits) {
        long elapsed = System.currentTimeMillis() - startTime;
        if (shouldStop) {
            setMsg("已停止 - " + hits.size() + " 个命中, 用时 " + formatTime(elapsed));
        } else {
            setMsg("完成 - " + hits.size() + " 个命中, 用时 " + formatTime(elapsed));
        }
        ELForm.setVal(100);

        if (hits.isEmpty()) {
            JOptionPane.showMessageDialog(jTextArea,
                    shouldStop ? "搜索已停止" : "没有找到结果");
            return;
        }
        if (!shouldStop) {
            JOptionPane.showMessageDialog(jTextArea, "搜索成功：找到符合表达式的方法");
        }
        ArrayList<ResObj> resObjList = new ArrayList<>(hits);
        new Thread(() -> CoreHelper.refreshMethods(resObjList),
                "el-refresh-methods").start();
    }

    private void setMsg(String s) {
        SwingUtilities.invokeLater(() -> msgLabel.setText(s));
    }

    private static String formatTime(long ms) {
        if (ms < 0) return "未知";
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;
        s = s % 60;
        m = m % 60;
        if (h > 0) return String.format("%d小时%d分%d秒", h, m, s);
        if (m > 0) return String.format("%d分%d秒", m, s);
        return String.format("%d.%ds", s, (ms / 100) % 10);
    }
}

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

import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ELSearchEngine {
    private static final Logger logger = LogManager.getLogger();

    private final Object value;
    private final JLabel msgLabel;
    private final JButton searchButton;
    private final JButton stopBtn;
    private final JTextArea jTextArea;

    private final AtomicLong processedMethods = new AtomicLong(0);
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private long startTime;
    private volatile long totalMethods;

    // 添加停止标志
    private volatile boolean shouldStop = false;

    public ELSearchEngine(
            Object value,
            JLabel msgLabel,
            JButton searchButton,
            JButton stopBtn,
            JTextArea jTextArea) {
        this.value = value;
        this.msgLabel = msgLabel;
        this.searchButton = searchButton;
        this.stopBtn = stopBtn;
        this.jTextArea = jTextArea;

        // 添加停止按钮的事件处理
        this.stopBtn.addActionListener(e -> {
            shouldStop = true;
            stopBtn.setEnabled(false);
            msgLabel.setText("正在停止搜索，请等待...");
            logger.info("用户请求停止搜索");
        });
    }

    public void run() {
        // 重置停止标志
        shouldStop = false;
        stopBtn.setEnabled(true);
        searchButton.setEnabled(false);

        int threadNum = Runtime.getRuntime().availableProcessors() * 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        ConcurrentLinkedQueue<ResObj> searchList = new ConcurrentLinkedQueue<>();

        startTime = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>();

        try {
            MethodEL condition = (MethodEL) value;

            totalMethods = MainForm.getEngine().getMethodsCount();
            logger.info("total method: {}", totalMethods);

            AtomicInteger taskId = new AtomicInteger(0);

            ScheduledExecutorService progressExecutor = Executors.newSingleThreadScheduledExecutor();
            ScheduledFuture<?> progressTask = progressExecutor.scheduleAtFixedRate(
                    this::updateProgress, 1, 1, TimeUnit.SECONDS);

            for (int offset = 0; offset < totalMethods && !shouldStop; ) {
                List<MethodReference> mrs = MainForm.getEngine().getAllMethodRef(offset);
                offset += mrs.size();

                final List<MethodReference> taskMrs = new ArrayList<>(mrs);
                Future<?> future = executor.submit(() -> {
                    try {
                        int id = taskId.incrementAndGet();
                        logger.debug("task - {} start, processing {} methods", id, taskMrs.size());

                        for (MethodReference mr : taskMrs) {
                            if (shouldStop) {
                                logger.info("任务被用户手动停止");
                                break;
                            }

                            try {
                                ClassReference.Handle ch = mr.getClassReference();
                                MethodELProcessor processor = new MethodELProcessor(ch, mr, searchList, condition);
                                processor.process();
                                processedMethods.incrementAndGet();
                            } catch (Exception ex) {
                                logger.error("处理方法引用时出错: {}", mr, ex);
                            }
                        }

                        int completed = completedTasks.incrementAndGet();
                        logger.info("task - {} finish, completed tasks: {}", id, completed);
                    } catch (Exception ex) {
                        logger.error("任务执行异常", ex);
                    }
                });

                futures.add(future);
            }

            executor.shutdown();
            msgLabel.setText("所有任务已加入线程池，请等待执行结束");

            try {
                while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    if (shouldStop) {
                        executor.shutdownNow();
                        break;
                    }
                }

                progressTask.cancel(false);
                progressExecutor.shutdown();

                if (shouldStop) {
                    msgLabel.setText("搜索已被用户停止");
                    logger.info("搜索被用户手动停止");
                }

                int failedTasks = 0;
                for (Future<?> future : futures) {
                    try {
                        if (!future.isCancelled()) {
                            future.get(1, TimeUnit.SECONDS);
                        }
                    } catch (ExecutionException ex) {
                        failedTasks++;
                        logger.error("任务执行失败", ex.getCause());
                    } catch (TimeoutException ex) {
                        logger.debug("跳过未完成的任务");
                    }
                }

                if (failedTasks > 0) {
                    logger.warn("有 {} 个任务执行失败", failedTasks);
                }

            } catch (InterruptedException ex) {
                logger.error("等待任务完成时被中断", ex);
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }

        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
            searchButton.setEnabled(true);
            stopBtn.setEnabled(false);
        }

        updateFinalProgress();

        if (searchList.isEmpty()) {
            ELForm.setVal(100);
            JOptionPane.showMessageDialog(jTextArea, shouldStop ? "搜索已停止" : "没有找到结果");
            return;
        } else if (!shouldStop) {
            JOptionPane.showMessageDialog(jTextArea, "搜索成功：找到符合表达式的方法");
        }

        ArrayList<ResObj> resObjList = new ArrayList<>();
        Object[] array = searchList.toArray();
        for (Object o : array) {
            resObjList.add((ResObj) o);
        }

        new Thread(() -> CoreHelper.refreshMethods(resObjList)).start();
        ELForm.setVal(100);
    }

    private void updateProgress() {
        long processed = processedMethods.get();
        if (totalMethods == 0) return;

        double progress = (double) processed / totalMethods;
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        String eta = "计算中...";
        if (processed > 0 && progress < 1.0) {
            long estimatedTotalTime = (long) (elapsedTime / progress);
            long remainingTime = estimatedTotalTime - elapsedTime;
            eta = formatTime(remainingTime);
        } else if (progress >= 1.0) {
            eta = "已完成";
        }

        String msg = String.format(
                "已处理 %d/%d 方法 - %.2f%% | 预计剩余时间: %s | 已用时: %s",
                processed, totalMethods, progress * 100, eta, formatTime(elapsedTime)
        );

        msgLabel.setText(msg);

        int progressValue = (int) (3 + progress * 97);
        ELForm.setVal(progressValue);

        logger.debug("Progress update: {}", msg);
    }

    private void updateFinalProgress() {
        long totalTime = System.currentTimeMillis() - startTime;
        String msg = String.format(
                "%s %d/%d 方法 - %.2f%% | 总用时: %s",
                shouldStop ? "已停止处理" : "处理完成",
                processedMethods.get(), totalMethods,
                ((double) processedMethods.get() / totalMethods) * 100,
                formatTime(totalTime)
        );

        msgLabel.setText(msg);
        logger.info("Final progress: {}", msg);
    }

    private String formatTime(long timeMs) {
        if (timeMs < 0) return "未知";

        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%d小时%d分钟%d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.agent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程安全的 token 用量累计器。
 * <p>
 * 由 {@link AiAgentRunner} 在每次 chat 完成后调用 {@link #onUsage(TokenUsage)} 累加；
 * GUI 线程读取 {@link #snapshot()} 即可拿到当前总量做实时渲染。
 */
public final class TokenUsageCounter implements TokenUsageSink {

    private final AtomicLong promptTokens = new AtomicLong();
    private final AtomicLong completionTokens = new AtomicLong();
    private final AtomicLong totalTokens = new AtomicLong();
    private final AtomicLong calls = new AtomicLong();

    @Override
    public void onUsage(TokenUsage usage) {
        if (usage == null) {
            return;
        }
        promptTokens.addAndGet(usage.getPromptTokens());
        completionTokens.addAndGet(usage.getCompletionTokens());
        totalTokens.addAndGet(usage.getTotalTokens());
        calls.incrementAndGet();
    }

    /**
     * 当前累计快照。
     */
    public TokenUsage snapshot() {
        return new TokenUsage(promptTokens.get(), completionTokens.get(), totalTokens.get());
    }

    /**
     * 已记录的 chat 调用次数。
     */
    public long callCount() {
        return calls.get();
    }

    public void reset() {
        promptTokens.set(0);
        completionTokens.set(0);
        totalTokens.set(0);
        calls.set(0);
    }
}

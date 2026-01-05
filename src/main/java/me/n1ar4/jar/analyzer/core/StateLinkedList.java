/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.gui.state.State;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.LinkedList;

/**
 * 修改链表
 * 要求 STATE 数量不超过 0XFF 个
 * 如果超出则删除最先进入是 STATE
 */
public class StateLinkedList extends LinkedList<State> {
    private static final Logger logger = LogManager.getLogger();
    private static final int MAX_CAPACITY = 0xFF;

    @Override
    public void add(int index, State element) {
        if (this.size() >= MAX_CAPACITY) {
            logger.info("states too large (0xff) delete first element");
            this.removeFirst();
            super.add(index - 1, element);
        } else {
            super.add(index, element);
        }
    }
}

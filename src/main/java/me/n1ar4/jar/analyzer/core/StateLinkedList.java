/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

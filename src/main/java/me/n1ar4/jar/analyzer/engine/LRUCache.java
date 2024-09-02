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

package me.n1ar4.jar.analyzer.engine;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

/**
 * LRU Cache
 */
public class LRUCache {
    private static final Logger logger = LogManager.getLogger();
    private final int capacity;
    private int count;
    private final Node[] nodes;
    private final Node head;
    private final Node tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.nodes = new Node[capacity];
        this.count = 0;
        head = new Node();
        tail = new Node();
        head.next = tail;
        tail.prev = head;
    }

    /**
     * Get Cache
     *
     * @param key String KEY
     * @return String CODE
     */
    public synchronized String get(String key) {
        Node node = findNodeByKey(key);
        if (node == null) {
            return null;
        }
        moveToHead(node);

        logger.debug("LRU GET - capacity : {} - size : {}", capacity, count);

        return node.value;
    }

    /**
     * Put Cache
     *
     * @param key   String KEY
     * @param value String CODE
     */
    public synchronized void put(String key, String value) {
        Node node = findNodeByKey(key);

        if (node != null) {
            node.value = value;
            moveToHead(node);
            return;
        }

        if (count == capacity) {
            Node tail = popTail();
            removeNodeFromNodes(tail.key);
            count--;
        }

        Node newNode = new Node(key, value);
        nodes[count] = newNode;
        count++;
        addNode(newNode);

        logger.debug("LRU PUT - capacity : {} - size : {}", capacity, count);
    }

    private Node findNodeByKey(String key) {
        for (int i = 0; i < count; i++) {
            if (nodes[i].key.equals(key)) {
                return nodes[i];
            }
        }
        return null;
    }

    private void addNode(Node node) {
        node.prev = head;
        node.next = head.next;

        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        Node prev = node.prev;
        Node next = node.next;

        prev.next = next;
        next.prev = prev;
    }

    private void moveToHead(Node node) {
        removeNode(node);
        addNode(node);
    }

    private Node popTail() {
        Node res = tail.prev;
        removeNode(res);
        return res;
    }

    private void removeNodeFromNodes(String key) {
        for (int i = 0; i < count; i++) {
            if (nodes[i].key.equals(key)) {
                System.arraycopy(nodes, i + 1, nodes, i, count - i - 1);
                break;
            }
        }
    }
}

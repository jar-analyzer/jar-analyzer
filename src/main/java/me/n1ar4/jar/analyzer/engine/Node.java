/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.engine;

/**
 * LRU Cache Node
 */
class Node {
    String key;
    String value;
    Node prev, next;

    Node(String key, String value) {
        this.key = key;
        this.value = value;
    }

    Node() {
    }
}
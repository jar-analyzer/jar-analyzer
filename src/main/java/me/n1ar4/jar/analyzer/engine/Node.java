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
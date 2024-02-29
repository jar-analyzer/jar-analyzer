package org.vidar.test;

import java.util.ArrayList;
import java.util.List;

public class GraphCallExample {
    public static void main(String[] args) {
        List<GraphCall> discoveredCalls = new ArrayList<>();

        GraphCall graphCall = new GraphCall("handle");
        discoveredCalls.add(graphCall);

        graphCall.setName("123");

        // 从 discoveredCalls 中取出 graphCall 对象
        GraphCall retrievedCall = discoveredCalls.get(0);

        System.out.println("Name of retrieved call: " + retrievedCall.getName());
    }
}

class GraphCall {
    private String handle;
    private String name;

    public GraphCall(String handle) {
        this.handle = handle;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

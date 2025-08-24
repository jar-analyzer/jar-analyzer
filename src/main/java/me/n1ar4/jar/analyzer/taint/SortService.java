package me.n1ar4.jar.analyzer.taint;


import me.n1ar4.jar.analyzer.core.reference.MethodReference;

import java.util.*;

public class SortService {
    public static List<MethodReference.Handle> start(
            HashMap<MethodReference.Handle, HashSet<MethodReference.Handle>> methodCalls) {
        Map<MethodReference.Handle, Set<MethodReference.Handle>> outgoingReferences = new HashMap<>();
        for (HashMap.Entry<MethodReference.Handle, HashSet<MethodReference.Handle>> entry : methodCalls.entrySet()) {
            MethodReference.Handle method = entry.getKey();
            outgoingReferences.put(method, new HashSet<>(entry.getValue()));
        }
        Set<MethodReference.Handle> dfsStack = new HashSet<>();
        Set<MethodReference.Handle> visitedNodes = new HashSet<>();
        List<MethodReference.Handle> sortedMethods = new ArrayList<>(outgoingReferences.size());
        for (MethodReference.Handle root : outgoingReferences.keySet()) {
            dfsSort(outgoingReferences, sortedMethods, visitedNodes, dfsStack, root);
        }
        return sortedMethods;
    }

    private static void dfsSort(Map<MethodReference.Handle, Set<MethodReference.Handle>> outgoingReferences,
                                List<MethodReference.Handle> sortedMethods, Set<MethodReference.Handle> visitedNodes,
                                Set<MethodReference.Handle> stack, MethodReference.Handle node) {
        if (stack.contains(node)) {
            return;
        }
        if (visitedNodes.contains(node)) {
            return;
        }
        Set<MethodReference.Handle> outgoingRefs = outgoingReferences.get(node);
        if (outgoingRefs == null) {
            return;
        }
        stack.add(node);
        for (MethodReference.Handle child : outgoingRefs) {
            dfsSort(outgoingReferences, sortedMethods, visitedNodes, stack, child);
        }
        stack.remove(node);
        visitedNodes.add(node);
        sortedMethods.add(node);
    }
}

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

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In-memory inheritance graph used by EL search to evaluate the
 * {@code isSubClassOf} / {@code isSuperClassOf} predicates as a single
 * BFS closure instead of one DB round-trip per inspected method.
 * <p>
 * Built once at the start of a search via {@link #build(SqlSession)}:
 * a single pass over {@code class_table} (super links) and
 * {@code interface_table} (implemented interfaces). Resulting maps
 * use class internal names ({@code java/lang/Object} form).
 */
public final class ELInheritanceIndex {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Direct parents of every class -- super class plus declared
     * interfaces. Used to derive ancestor closures.
     */
    private final Map<String, Set<String>> directParents;

    /**
     * Direct children of every type -- the inverse view. Used to
     * derive descendant closures.
     */
    private final Map<String, Set<String>> directChildren;

    private ELInheritanceIndex(Map<String, Set<String>> parents,
                               Map<String, Set<String>> children) {
        this.directParents = parents;
        this.directChildren = children;
    }

    public static ELInheritanceIndex build(SqlSession session) {
        Map<String, Set<String>> parents = new HashMap<>(8192);
        Map<String, Set<String>> children = new HashMap<>(8192);
        // IMPORTANT: do NOT close the Connection returned by
        // SqlSession.getConnection() -- it is the session's own JDBC
        // connection. Closing it would return the connection to the
        // pool and break every subsequent Mapper call on the same
        // session. We only own the Statement / ResultSet objects.
        Connection conn = session.getConnection();
        try (Statement st = conn.createStatement()) {
            // class -> super_class_name
            try (ResultSet rs = st.executeQuery(
                    "SELECT class_name, super_class_name FROM class_table")) {
                while (rs.next()) {
                    String cn = rs.getString(1);
                    String sn = rs.getString(2);
                    if (cn == null || sn == null || sn.isEmpty()) {
                        continue;
                    }
                    parents.computeIfAbsent(cn, k -> new HashSet<>()).add(sn);
                    children.computeIfAbsent(sn, k -> new HashSet<>()).add(cn);
                }
            }
            // class -> interface_name
            try (ResultSet rs = st.executeQuery(
                    "SELECT class_name, interface_name FROM interface_table")) {
                while (rs.next()) {
                    String cn = rs.getString(1);
                    String in = rs.getString(2);
                    if (cn == null || in == null || in.isEmpty()) {
                        continue;
                    }
                    parents.computeIfAbsent(cn, k -> new HashSet<>()).add(in);
                    children.computeIfAbsent(in, k -> new HashSet<>()).add(cn);
                }
            }
        } catch (Exception e) {
            logger.warn("build inheritance index failed: {}", e.toString());
            return new ELInheritanceIndex(Collections.emptyMap(), Collections.emptyMap());
        }
        logger.info("inheritance index built: {} parents, {} children",
                parents.size(), children.size());
        return new ELInheritanceIndex(parents, children);
    }

    /**
     * Collects every descendant of {@code root} (transitive children +
     * the root itself, since "X is a sub-class of X" is conventionally
     * accepted by callers).
     */
    public Set<String> descendantsClosure(String root) {
        return closure(root, directChildren);
    }

    /**
     * Collects every ancestor of {@code root}: the transitive parents
     * including {@code root} itself.
     */
    public Set<String> ancestorsClosure(String root) {
        return closure(root, directParents);
    }

    private static Set<String> closure(String root, Map<String, Set<String>> graph) {
        if (root == null || root.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> seen = new HashSet<>();
        Deque<String> q = new ArrayDeque<>();
        q.add(root);
        seen.add(root);
        while (!q.isEmpty()) {
            String cur = q.removeFirst();
            Set<String> next = graph.get(cur);
            if (next == null) {
                continue;
            }
            for (String n : next) {
                if (seen.add(n)) {
                    q.addLast(n);
                }
            }
        }
        return seen;
    }

    /** Trims a closure to a fresh List for SQL IN-list usage. */
    public static List<String> toList(Set<String> set) {
        return new ArrayList<>(set);
    }
}

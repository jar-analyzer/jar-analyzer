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

import me.n1ar4.jar.analyzer.core.mapper.ELSearchMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Translates a {@link MethodEL} into a single SQL query plus a small
 * residual Java-side predicate.
 * <p>
 * This is the heart of the rewrite: instead of pulling every method
 * across the JDBC boundary and judging in Java, we push every filter
 * the database can answer back into one parameterized query.  The
 * residual chain only handles the few predicates that genuinely need
 * the parsed JVM descriptor (param-by-index types, return type) or
 * full regex semantics.
 */
public final class ELQueryPlanner {

    /** Holds the work split between SQL and Java. */
    public static final class Plan {
        public final Map<String, Object> sqlParams;
        public final Pattern nameRegex;       // null = no filter
        public final Pattern classNameRegex;  // null = no filter
        public final Integer paramsNum;       // null = no filter
        public final String returnType;       // null = no filter
        public final Map<Integer, String> paramTypes; // empty = no filter
        /**
         * When set, {@link #sqlParams} contains the empty-result
         * sentinel: the caller should skip the SQL entirely and
         * return zero rows. Used to short-circuit conditions that we
         * already know cannot be satisfied (e.g. {@code isSubClassOf}
         * referring to an unknown type).
         */
        public final boolean impossible;

        private Plan(Map<String, Object> p, Pattern nr, Pattern cnr,
                     Integer pn, String rt, Map<Integer, String> pt,
                     boolean imp) {
            this.sqlParams = p;
            this.nameRegex = nr;
            this.classNameRegex = cnr;
            this.paramsNum = pn;
            this.returnType = rt;
            this.paramTypes = pt;
            this.impossible = imp;
        }

        static Plan empty() {
            return new Plan(Collections.emptyMap(), null, null, null, null,
                    Collections.emptyMap(), true);
        }
    }

    private ELQueryPlanner() {
    }

    public static Plan plan(MethodEL c, SqlSession session, ELInheritanceIndex inh) {
        // Pre-populate every key the XML <if> blocks reference, even
        // when the value is null. MyBatis + OGNL on Map<String,Object>
        // will throw when a referenced key is absent from the map; a
        // null *value* is fine, but a missing key is not.
        Map<String, Object> p = new HashMap<>();
        p.put("classNameContains", null);
        p.put("classNameNotContains", null);
        p.put("methodNameContains", null);
        p.put("methodNameNotContains", null);
        p.put("startWith", null);
        p.put("endWith", null);
        p.put("isStatic", null);
        p.put("isPublic", null);
        p.put("isPublicInt", null);
        p.put("methodAnnoLike", null);
        p.put("excludedMethodAnnoLike", null);
        p.put("classAnnoLike", null);
        p.put("fieldLike", null);
        p.put("classNamesIn", null);
        p.put("containsCallerKeys", null);
        p.put("excludeCallerKeys", null);

        // Trivial string filters -- pushed down 1:1.
        putNonEmpty(p, "classNameContains", c.getClassNameContains());
        putNonEmpty(p, "classNameNotContains", c.getClassNameNotContains());
        putNonEmpty(p, "methodNameContains", c.getNameContains());
        putNonEmpty(p, "methodNameNotContains", c.getNameNotContains());
        putNonEmpty(p, "startWith", c.getStartWith());
        putNonEmpty(p, "endWith", c.getEndWith());

        if (c.getStatic() != null) {
            p.put("isStatic", c.getStatic() ? 1 : 0);
        }
        if (c.getPublic() != null) {
            p.put("isPublic", c.getPublic());
            p.put("isPublicInt", c.getPublic() ? 1 : 0);
        }

        putNonEmpty(p, "methodAnnoLike", c.getMethodAnno());
        putNonEmpty(p, "excludedMethodAnnoLike", c.getExcludedMethodAnno());
        putNonEmpty(p, "classAnnoLike", c.getClassAnno());
        putNonEmpty(p, "fieldLike", c.getField());

        // Inheritance: convert "isSubClassOf X" to "class IN
        // descendants(X)" using the in-memory closure. If X is unknown
        // we know up-front the result is empty.
        Set<String> classRestriction = null;
        if (isSet(c.getIsSubClassOf())) {
            String target = c.getIsSubClassOf().replace('.', '/');
            Set<String> desc = inh != null
                    ? inh.descendantsClosure(target) : null;
            if (desc == null || desc.isEmpty()) {
                return Plan.empty();
            }
            classRestriction = intersect(classRestriction, desc);
        }
        if (isSet(c.getIsSuperClassOf())) {
            String target = c.getIsSuperClassOf().replace('.', '/');
            Set<String> anc = inh != null
                    ? inh.ancestorsClosure(target) : null;
            if (anc == null || anc.isEmpty()) {
                return Plan.empty();
            }
            classRestriction = intersect(classRestriction, anc);
        }
        if (classRestriction != null) {
            if (classRestriction.isEmpty()) {
                return Plan.empty();
            }
            p.put("classNamesIn", new ArrayList<>(classRestriction));
        }

        // Reverse callee lookup: contains/exclude invoke. A single SQL
        // IN check replaces N getCallee() roundtrips.
        if (c.getContainsInvokeList() != null && !c.getContainsInvokeList().isEmpty()) {
            // AND across multiple invoke targets: caller must be in
            // EVERY target's caller set, i.e. intersection.
            Set<String> intersection = null;
            for (String[] tgt : c.getContainsInvokeList()) {
                if (tgt == null || tgt.length < 2) continue;
                Set<String> callers = fetchCallers(session, tgt[0], tgt[1]);
                if (intersection == null) {
                    intersection = new HashSet<>(callers);
                } else {
                    intersection.retainAll(callers);
                }
                if (intersection.isEmpty()) {
                    return Plan.empty();
                }
            }
            if (intersection != null) {
                p.put("containsCallerKeys", new ArrayList<>(intersection));
            }
        }
        if (c.getExcludeInvokeList() != null && !c.getExcludeInvokeList().isEmpty()) {
            // OR semantics for exclusion: caller must not be in ANY
            // target's caller set, i.e. union.
            Set<String> union = new HashSet<>();
            for (String[] tgt : c.getExcludeInvokeList()) {
                if (tgt == null || tgt.length < 2) continue;
                union.addAll(fetchCallers(session, tgt[0], tgt[1]));
            }
            if (!union.isEmpty()) {
                p.put("excludeCallerKeys", new ArrayList<>(union));
            }
        }

        // Residual Java-side predicates -- compile once.
        Pattern nameRegex = compile(c.getNameRegex());
        Pattern classRegex = compile(c.getClassNameRegex());

        Map<Integer, String> paramTypes = c.getParamTypes() == null
                ? Collections.<Integer, String>emptyMap()
                : c.getParamTypes();

        return new Plan(p, nameRegex, classRegex,
                c.getParamsNum(), c.getReturnType(), paramTypes, false);
    }

    private static Set<String> fetchCallers(SqlSession session, String classDot, String method) {
        ELSearchMapper mapper = session.getMapper(ELSearchMapper.class);
        String classSlash = classDot == null ? null : classDot.replace('.', '/');
        List<String> rows = mapper.selectCallerKeys(classSlash, method);
        return rows == null
                ? Collections.<String>emptySet()
                : new LinkedHashSet<>(rows);
    }

    private static Set<String> intersect(Set<String> base, Set<String> other) {
        if (base == null) {
            return new LinkedHashSet<>(other);
        }
        Set<String> out = new LinkedHashSet<>(base);
        out.retainAll(other);
        return out;
    }

    private static void putNonEmpty(Map<String, Object> p, String key, String v) {
        if (isSet(v)) {
            p.put(key, v);
        }
    }

    private static boolean isSet(String s) {
        return s != null && !s.isEmpty();
    }

    private static Pattern compile(String s) {
        if (!isSet(s)) {
            return null;
        }
        try {
            return Pattern.compile(s);
        } catch (PatternSyntaxException ex) {
            // Sentinel: never-matching pattern -- preserves prior
            // behavior of "invalid regex => 0 results".
            return Pattern.compile("(?!)");
        }
    }
}

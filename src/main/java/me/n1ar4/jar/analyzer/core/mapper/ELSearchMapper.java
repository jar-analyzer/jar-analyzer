/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Push-down query surface used by the EL search engine. Each method
 * builds a WHERE clause that the SQLite optimizer can satisfy with a
 * combination of index seeks instead of full-table scans.
 * <p>
 * Parameters are always passed via MyBatis {@code #{...}} placeholders
 * -- never concatenated -- so SQL injection is structurally
 * impossible. The dynamic fragments built with {@code &lt;if&gt;}
 * also use parameterized placeholders.
 */
public interface ELSearchMapper {

    /**
     * Returns the candidate methods that satisfy every push-downable
     * filter in {@code params}. Filters that cannot be evaluated in
     * SQL (e.g. param-by-index type matches, regex, or super/sub
     * inheritance closures) are applied later in Java.
     * <p>
     * Supported keys in {@code params}:
     * <ul>
     *   <li>{@code classNameContains, classNameNotContains}</li>
     *   <li>{@code methodNameContains, methodNameNotContains}</li>
     *   <li>{@code startWith, endWith}</li>
     *   <li>{@code paramsNum, isStatic, isPublic}</li>
     *   <li>{@code methodAnnoLike, classAnnoLike,
     *       excludedMethodAnnoLike, fieldLike}</li>
     *   <li>{@code classNamesIn} -- explicit set of class names; used
     *       when the inheritance closure has already been computed in
     *       Java and we want to constrain to those classes.</li>
     *   <li>{@code containsCallerKeys, excludeCallerKeys} -- list of
     *       String keys produced by the engine in the form
     *       {@code class\u0001name\u0001desc} for the IN/NOT IN
     *       intersection of caller candidates derived from reverse
     *       method-call lookup.</li>
     * </ul>
     */
    List<MethodResult> selectCandidates(Map<String, Object> params);

    /**
     * Reverse callee lookup. Returns the (caller_class, caller_method,
     * caller_desc) tuples encoded as {@code class\u0001method\u0001desc}
     * strings for a given target.
     */
    List<String> selectCallerKeys(@Param("calleeClass") String calleeClass,
                                  @Param("calleeMethod") String calleeMethod);
}

/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.tree;

/**
 * The semantic categories the file tree renders distinct icons for.
 * Resolved by {@link ClassKindResolver} from the class file's access
 * flags + direct super name.
 */
public enum ClassIconKind {
    /**
     * Plain concrete class (default).
     */
    CLASS,
    /**
     * abstract non-interface class.
     */
    ABSTRACT_CLASS,
    /**
     * regular interface.
     */
    INTERFACE,
    /**
     * annotation type ({@code @interface}).
     */
    ANNOTATION,
    /**
     * enum type.
     */
    ENUM,
    /**
     * record type (Java 16+).
     */
    RECORD,
    /**
     * class whose direct super is Throwable / Exception / RuntimeException / Error.
     */
    EXCEPTION,
    /**
     * failed to parse the class file.
     */
    UNKNOWN
}

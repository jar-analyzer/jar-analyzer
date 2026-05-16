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

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;

import java.util.Locale;

/**
 * Classifies a non-class file in the project tree into a coarse
 * {@code Kind} (XML, YAML, image, archive, ...) so the tree renderer
 * can pick a matching SVG icon. Pure pattern matching on the file name;
 * no I/O, no caching needed -- callers can invoke this from the EDT
 * inside the cell renderer cheaply.
 */
public final class ResourceFileKind {

    public enum Kind {
        XML,
        YAML,
        PROPERTIES,
        JSON,
        MANIFEST,
        TEXT,
        HTML,
        CSS,
        JS,
        SQL,
        SHELL,
        IMAGE,
        FONT,
        ARCHIVE,
        NESTED_JAR,
        NATIVE_LIB,
        JAVA_SOURCE,
        SPRING,
        FILE
    }

    private ResourceFileKind() {
    }

    /**
     * Resolves the kind for a leaf file name. Accepts either the bare
     * file name ("application.yml") or a full path -- only the trailing
     * segment is inspected.
     */
    public static Kind classify(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Kind.FILE;
        }
        // Strip any directory component so a path like
        // "META-INF/MANIFEST.MF" still matches the manifest rule.
        String name = fileName;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < name.length()) {
            name = name.substring(slash + 1);
        }
        String lower = name.toLowerCase(Locale.ROOT);

        // Special whole-filename matches first. Order matters: spring
        // descriptors and the jar manifest are recognized regardless of
        // what extension follows.
        if (lower.equals("manifest.mf") || lower.endsWith(".mf")) {
            return Kind.MANIFEST;
        }
        if (lower.equals("spring.factories")
                || lower.equals("spring.handlers")
                || lower.equals("spring.schemas")
                || lower.equals("spring.tooling")
                || lower.equals("spring-configuration-metadata.json")
                || lower.equals("additional-spring-configuration-metadata.json")) {
            return Kind.SPRING;
        }

        int dot = lower.lastIndexOf('.');
        if (dot < 0 || dot == lower.length() - 1) {
            return Kind.FILE;
        }
        String ext = lower.substring(dot + 1);

        switch (ext) {
            // --- markup / config -----------------------------------------
            case "xml":
            case "xsd":
            case "dtd":
            case "pom":
            case "tld":
            case "wsdl":
            case "xsl":
            case "xslt":
                return Kind.XML;
            case "yaml":
            case "yml":
                return Kind.YAML;
            case "properties":
            case "ini":
            case "cfg":
            case "conf":
                return Kind.PROPERTIES;
            case "json":
            case "json5":
                return Kind.JSON;
            // --- text / docs ---------------------------------------------
            case "txt":
            case "md":
            case "markdown":
            case "rst":
            case "adoc":
            case "asciidoc":
            case "log":
            case "readme":
            case "license":
            case "notice":
                return Kind.TEXT;
            // --- web -----------------------------------------------------
            case "html":
            case "htm":
            case "xhtml":
            case "jsp":
            case "jspx":
            case "ftl":
            case "vm":
            case "tag":
            case "tagx":
                return Kind.HTML;
            case "css":
            case "less":
            case "scss":
            case "sass":
                return Kind.CSS;
            case "js":
            case "mjs":
            case "cjs":
            case "ts":
            case "tsx":
            case "jsx":
            case "map":
                return Kind.JS;
            // --- data ----------------------------------------------------
            case "sql":
            case "ddl":
            case "dml":
                return Kind.SQL;
            // --- scripts -------------------------------------------------
            case "sh":
            case "bash":
            case "zsh":
            case "fish":
            case "bat":
            case "cmd":
            case "ps1":
            case "psm1":
                return Kind.SHELL;
            // --- binary assets -------------------------------------------
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
            case "webp":
            case "ico":
            case "svg":
            case "tiff":
            case "tif":
                return Kind.IMAGE;
            case "ttf":
            case "otf":
            case "woff":
            case "woff2":
            case "eot":
                return Kind.FONT;
            case "zip":
            case "tar":
            case "gz":
            case "tgz":
            case "bz2":
            case "xz":
            case "7z":
            case "rar":
                return Kind.ARCHIVE;
            case "jar":
            case "war":
            case "ear":
            case "aar":
                return Kind.NESTED_JAR;
            case "so":
            case "dll":
            case "dylib":
            case "jnilib":
                return Kind.NATIVE_LIB;
            // --- source --------------------------------------------------
            case "java":
            case "kt":
            case "kts":
            case "scala":
            case "groovy":
                return Kind.JAVA_SOURCE;
            default:
                return Kind.FILE;
        }
    }

    /**
     * Maps a {@link Kind} to its SVG icon. Unknown -> generic file
     * icon. Kept here (next to the classifier) so all icon plumbing
     * for resource files lives in one place.
     */
    public static FlatSVGIcon iconFor(Kind kind) {
        if (kind == null) {
            return SvgManager.FileIcon;
        }
        switch (kind) {
            case XML:
                return SvgManager.XmlIcon;
            case YAML:
                return SvgManager.YamlIcon;
            case PROPERTIES:
                return SvgManager.PropertiesIcon;
            case JSON:
                return SvgManager.JsonIcon;
            case MANIFEST:
                return SvgManager.ManifestIcon;
            case TEXT:
                return SvgManager.TextIcon;
            case HTML:
                return SvgManager.HtmlIcon;
            case CSS:
                return SvgManager.CssIcon;
            case JS:
                return SvgManager.JsIcon;
            case SQL:
                return SvgManager.SqlIcon;
            case SHELL:
                return SvgManager.ShellIcon;
            case IMAGE:
                return SvgManager.ImageFileIcon;
            case FONT:
                return SvgManager.FontIcon;
            case ARCHIVE:
                return SvgManager.ArchiveIcon;
            case NESTED_JAR:
                return SvgManager.NestedJarIcon;
            case NATIVE_LIB:
                return SvgManager.NativeIcon;
            case JAVA_SOURCE:
                return SvgManager.JavaSourceIcon;
            case SPRING:
                return SvgManager.SpringIcon;
            case FILE:
            default:
                return SvgManager.FileIcon;
        }
    }
}

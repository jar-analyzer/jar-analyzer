/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.markdown;

import java.io.IOException;
import java.io.Reader;

/**
 * Dependency-free Markdown-to-HTML renderer owned by Jar Analyzer.
 *
 * <p>The renderer accepts untrusted Markdown. HTML embedded in the source is
 * escaped and only HTTP(S), mail links, anchors and relative URLs are emitted.
 * Instances are immutable and thread-safe.</p>
 */
public final class MarkdownRenderer {
    private static final MarkdownRenderer DEFAULT =
            new MarkdownRenderer(MarkdownOptions.DEFAULT);

    private final MarkdownOptions options;

    public MarkdownRenderer(MarkdownOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }
        this.options = options;
    }

    public static String toHtml(String markdown) {
        return DEFAULT.render(markdown);
    }

    public String render(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        if (markdown.length() > options.getMaxInputCharacters()) {
            throw new IllegalArgumentException("Markdown input exceeds "
                    + options.getMaxInputCharacters() + " characters");
        }
        String normalized = normalize(markdown);
        return new MarkdownParser(options).render(normalized);
    }

    public String render(Reader reader) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }
        StringBuilder input = new StringBuilder();
        char[] buffer = new char[4096];
        int read;
        while ((read = reader.read(buffer)) >= 0) {
            if (read == 0) {
                continue;
            }
            if (input.length() + read > options.getMaxInputCharacters()) {
                throw new IllegalArgumentException("Markdown input exceeds "
                        + options.getMaxInputCharacters() + " characters");
            }
            input.append(buffer, 0, read);
        }
        return render(input.toString());
    }

    private static String normalize(String input) {
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\r') {
                if (i + 1 < input.length() && input.charAt(i + 1) == '\n') {
                    i++;
                }
                out.append('\n');
            } else if (c == 0) {
                out.append('\uFFFD');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}

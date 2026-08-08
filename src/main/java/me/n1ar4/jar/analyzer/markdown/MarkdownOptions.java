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

/**
 * Immutable limits and feature switches for Markdown rendering.
 *
 * <p>The defaults are deliberately safe for rendering untrusted AI output:
 * raw HTML is never interpreted and unsafe link schemes are discarded.</p>
 */
public final class MarkdownOptions {
    public static final MarkdownOptions DEFAULT = builder().build();

    private final int maxInputCharacters;
    private final int maxNestingDepth;
    private final boolean tables;
    private final boolean taskLists;
    private final boolean strikethrough;
    private final boolean autolinks;
    private final boolean images;
    private final boolean remoteImages;

    private MarkdownOptions(Builder builder) {
        this.maxInputCharacters = builder.maxInputCharacters;
        this.maxNestingDepth = builder.maxNestingDepth;
        this.tables = builder.tables;
        this.taskLists = builder.taskLists;
        this.strikethrough = builder.strikethrough;
        this.autolinks = builder.autolinks;
        this.images = builder.images;
        this.remoteImages = builder.remoteImages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getMaxInputCharacters() {
        return maxInputCharacters;
    }

    public int getMaxNestingDepth() {
        return maxNestingDepth;
    }

    public boolean isTablesEnabled() {
        return tables;
    }

    public boolean isTaskListsEnabled() {
        return taskLists;
    }

    public boolean isStrikethroughEnabled() {
        return strikethrough;
    }

    public boolean isAutolinksEnabled() {
        return autolinks;
    }

    public boolean isImagesEnabled() {
        return images;
    }

    public boolean isRemoteImagesEnabled() {
        return remoteImages;
    }

    public static final class Builder {
        private int maxInputCharacters = 2_000_000;
        private int maxNestingDepth = 32;
        private boolean tables = true;
        private boolean taskLists = true;
        private boolean strikethrough = true;
        private boolean autolinks = true;
        private boolean images = true;
        private boolean remoteImages = false;

        private Builder() {
        }

        public Builder maxInputCharacters(int value) {
            if (value < 1) {
                throw new IllegalArgumentException("maxInputCharacters must be positive");
            }
            this.maxInputCharacters = value;
            return this;
        }

        public Builder maxNestingDepth(int value) {
            if (value < 1 || value > 256) {
                throw new IllegalArgumentException("maxNestingDepth must be between 1 and 256");
            }
            this.maxNestingDepth = value;
            return this;
        }

        public Builder tables(boolean value) {
            this.tables = value;
            return this;
        }

        public Builder taskLists(boolean value) {
            this.taskLists = value;
            return this;
        }

        public Builder strikethrough(boolean value) {
            this.strikethrough = value;
            return this;
        }

        public Builder autolinks(boolean value) {
            this.autolinks = value;
            return this;
        }

        public Builder images(boolean value) {
            this.images = value;
            return this;
        }

        /**
         * Allows HTTP(S) image sources. Disabled by default so rendering an
         * untrusted AI response cannot silently make a network request.
         */
        public Builder remoteImages(boolean value) {
            this.remoteImages = value;
            return this;
        }

        public MarkdownOptions build() {
            return new MarkdownOptions(this);
        }
    }
}

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

import java.util.Locale;

final class HtmlSupport {
    private HtmlSupport() {
    }

    static String escapeText(String value) {
        StringBuilder out = new StringBuilder(value.length() + 16);
        appendEscaped(out, value, false);
        return out.toString();
    }

    static String escapeAttribute(String value) {
        StringBuilder out = new StringBuilder(value.length() + 16);
        appendEscaped(out, value, true);
        return out.toString();
    }

    private static void appendEscaped(StringBuilder out, String value,
                                      boolean attribute) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append(attribute ? "&quot;" : "\"");
                    break;
                case '\'':
                    out.append(attribute ? "&#39;" : "'");
                    break;
                case 0:
                    out.append('\uFFFD');
                    break;
                default:
                    out.append(c);
                    break;
            }
        }
    }

    /**
     * Returns an inert, escaped URL or {@code null} for a dangerous target.
     */
    static String safeUrl(String value, boolean image) {
        if (value == null) {
            return null;
        }
        String target = stripControlCharacters(value.trim());
        if (target.isEmpty()) {
            return "";
        }

        String inspected = decodeForSchemeCheck(target);
        int colon = inspected.indexOf(':');
        int boundary = firstPathBoundary(inspected);
        if (colon >= 0 && (boundary < 0 || colon < boundary)) {
            String scheme = inspected.substring(0, colon).toLowerCase(Locale.ROOT);
            boolean allowed = "http".equals(scheme) || "https".equals(scheme)
                    || (!image && "mailto".equals(scheme));
            if (!allowed) {
                return null;
            }
        }
        return escapeAttribute(target);
    }

    static boolean isRemoteUrl(String value) {
        if (value == null) {
            return false;
        }
        String inspected = decodeForSchemeCheck(value.trim())
                .toLowerCase(Locale.ROOT);
        return inspected.startsWith("http:") || inspected.startsWith("https:");
    }

    private static String stripControlCharacters(String value) {
        StringBuilder clean = null;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c <= 0x1f || c == 0x7f) {
                if (clean == null) {
                    clean = new StringBuilder(value.length());
                    clean.append(value, 0, i);
                }
            } else if (clean != null) {
                clean.append(c);
            }
        }
        return clean == null ? value : clean.toString();
    }

    private static int firstPathBoundary(String value) {
        int slash = value.indexOf('/');
        int query = value.indexOf('?');
        int hash = value.indexOf('#');
        int result = -1;
        if (slash >= 0) {
            result = slash;
        }
        if (query >= 0 && (result < 0 || query < result)) {
            result = query;
        }
        if (hash >= 0 && (result < 0 || hash < result)) {
            result = hash;
        }
        return result;
    }

    private static String decodeForSchemeCheck(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c <= 0x20 || c == 0x7f) {
                continue;
            }
            if (c == '%' && i + 2 < value.length()) {
                int high = Character.digit(value.charAt(i + 1), 16);
                int low = Character.digit(value.charAt(i + 2), 16);
                if (high >= 0 && low >= 0) {
                    out.append((char) ((high << 4) + low));
                    i += 2;
                    continue;
                }
            }
            if (c == '&') {
                int semicolon = value.indexOf(';', i + 1);
                if (semicolon > i && semicolon - i <= 10) {
                    Character decoded = decodeEntity(value.substring(i + 1, semicolon));
                    if (decoded != null) {
                        out.append(decoded.charValue());
                        i = semicolon;
                        continue;
                    }
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private static Character decodeEntity(String entity) {
        if ("colon".equalsIgnoreCase(entity)) {
            return ':';
        }
        try {
            if (entity.startsWith("#x") || entity.startsWith("#X")) {
                return (char) Integer.parseInt(entity.substring(2), 16);
            }
            if (entity.startsWith("#")) {
                return (char) Integer.parseInt(entity.substring(1));
            }
        } catch (NumberFormatException ignored) {
            // An invalid entity remains literal and therefore inert.
        }
        return null;
    }

    static String languageClass(String info) {
        if (info == null) {
            return "";
        }
        String token = info.trim();
        int space = token.indexOf(' ');
        if (space >= 0) {
            token = token.substring(0, space);
        }
        StringBuilder clean = new StringBuilder(Math.min(token.length(), 48));
        for (int i = 0; i < token.length() && clean.length() < 48; i++) {
            char c = token.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '+') {
                clean.append(c);
            }
        }
        return clean.toString();
    }
}

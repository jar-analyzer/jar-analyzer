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
import java.util.Map;

final class InlineParser {
    private static final String ESCAPABLE =
            "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    private final MarkdownOptions options;
    private final Map<String, LinkReference> references;

    InlineParser(MarkdownOptions options,
                 Map<String, LinkReference> references) {
        this.options = options;
        this.references = references;
    }

    String render(String source) {
        return render(source, 0, true);
    }

    private String render(String source, int depth, boolean allowLinks) {
        if (source.isEmpty()) {
            return "";
        }
        if (depth >= options.getMaxNestingDepth()) {
            return HtmlSupport.escapeText(source);
        }

        StringBuilder out = new StringBuilder(source.length() + 24);
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);

            if (c == '\\') {
                if (i + 1 < source.length() && source.charAt(i + 1) == '\n') {
                    out.append("<br />\n");
                    i += 2;
                } else if (i + 1 < source.length()
                        && ESCAPABLE.indexOf(source.charAt(i + 1)) >= 0) {
                    appendEscapedChar(out, source.charAt(i + 1));
                    i += 2;
                } else {
                    out.append('\\');
                    i++;
                }
                continue;
            }

            if (c == '`') {
                int run = countRun(source, i, '`');
                int close = findRun(source, i + run, '`', run);
                if (close >= 0) {
                    String code = source.substring(i + run, close)
                            .replace('\n', ' ');
                    if (code.length() >= 2 && code.charAt(0) == ' '
                            && code.charAt(code.length() - 1) == ' '
                            && !isAllSpaces(code)) {
                        code = code.substring(1, code.length() - 1);
                    }
                    out.append("<code>").append(HtmlSupport.escapeText(code))
                            .append("</code>");
                    i = close + run;
                    continue;
                }
            }

            if (allowLinks && c == '!' && i + 1 < source.length()
                    && source.charAt(i + 1) == '[') {
                ParsedLink image = parseLink(source, i + 1, true);
                if (image != null) {
                    String url = options.isImagesEnabled()
                            ? HtmlSupport.safeUrl(image.destination, true) : null;
                    if (!options.isRemoteImagesEnabled()
                            && HtmlSupport.isRemoteUrl(image.destination)) {
                        url = null;
                    }
                    if (options.isImagesEnabled() && url != null) {
                        out.append("<img src=\"").append(url).append("\" alt=\"")
                                .append(HtmlSupport.escapeAttribute(plainText(image.label)))
                                .append('"');
                        appendTitle(out, image.title);
                        out.append(" />");
                    } else {
                        out.append(HtmlSupport.escapeText(image.label));
                    }
                    i = image.end;
                    continue;
                }
            }

            if (allowLinks && c == '[') {
                ParsedLink link = parseLink(source, i, false);
                if (link != null) {
                    String url = HtmlSupport.safeUrl(link.destination, false);
                    if (url != null) {
                        out.append("<a href=\"").append(url).append('"');
                        appendTitle(out, link.title);
                        out.append('>').append(render(link.label, depth + 1, false))
                                .append("</a>");
                    } else {
                        out.append(render(link.label, depth + 1, false));
                    }
                    i = link.end;
                    continue;
                }
            }

            if (c == '<' && options.isAutolinksEnabled()) {
                int close = source.indexOf('>', i + 1);
                if (close > i + 1) {
                    String inside = source.substring(i + 1, close);
                    if (isWebUrl(inside)) {
                        appendLink(out, inside, inside);
                        i = close + 1;
                        continue;
                    }
                    if (isEmail(inside)) {
                        appendLink(out, "mailto:" + inside, inside);
                        i = close + 1;
                        continue;
                    }
                }
            }

            if (options.isAutolinksEnabled()
                    && (startsWithIgnoreCase(source, i, "http://")
                    || startsWithIgnoreCase(source, i, "https://"))) {
                int end = scanBareUrl(source, i);
                String url = source.substring(i, end);
                appendLink(out, url, url);
                i = end;
                continue;
            }

            if (options.isStrikethroughEnabled() && c == '~'
                    && i + 1 < source.length() && source.charAt(i + 1) == '~') {
                int close = findDelimiter(source, i + 2, "~~");
                if (close > i + 2) {
                    out.append("<del>")
                            .append(render(source.substring(i + 2, close), depth + 1, allowLinks))
                            .append("</del>");
                    i = close + 2;
                    continue;
                }
            }

            if ((c == '*' || c == '_') && i + 2 < source.length()
                    && source.charAt(i + 1) == c && source.charAt(i + 2) == c
                    && canOpenEmphasis(source, i, c)) {
                String delimiter = new String(new char[]{c, c, c});
                int close = findDelimiter(source, i + 3, delimiter);
                if (close > i + 3) {
                    out.append("<strong><em>")
                            .append(render(source.substring(i + 3, close), depth + 1, allowLinks))
                            .append("</em></strong>");
                    i = close + 3;
                    continue;
                }
            }

            if ((c == '*' || c == '_') && i + 1 < source.length()
                    && source.charAt(i + 1) == c && canOpenEmphasis(source, i, c)) {
                String delimiter = new String(new char[]{c, c});
                int close = findDelimiter(source, i + 2, delimiter);
                if (close > i + 2 && canCloseEmphasis(source, close, 2, c)) {
                    out.append("<strong>")
                            .append(render(source.substring(i + 2, close), depth + 1, allowLinks))
                            .append("</strong>");
                    i = close + 2;
                    continue;
                }
            }

            if ((c == '*' || c == '_') && canOpenEmphasis(source, i, c)) {
                String delimiter = String.valueOf(c);
                int close = findDelimiter(source, i + 1, delimiter);
                if (close > i + 1 && canCloseEmphasis(source, close, 1, c)) {
                    out.append("<em>")
                            .append(render(source.substring(i + 1, close), depth + 1, allowLinks))
                            .append("</em>");
                    i = close + 1;
                    continue;
                }
            }

            if (c == '\n') {
                int spaces = trailingSpaces(source, i);
                if (spaces >= 2) {
                    int remove = Math.min(spaces, trailingLiteralSpaces(out));
                    out.setLength(out.length() - remove);
                    out.append("<br />\n");
                } else {
                    out.append('\n');
                }
                i++;
                continue;
            }

            appendEscapedChar(out, c);
            i++;
        }
        return out.toString();
    }

    private ParsedLink parseLink(String source, int open, boolean image) {
        int labelEnd = findClosingBracket(source, open + 1);
        if (labelEnd < 0) {
            return null;
        }
        String label = source.substring(open + 1, labelEnd);
        int next = labelEnd + 1;

        if (next < source.length() && source.charAt(next) == '(') {
            Destination destination = parseDestination(source, next + 1);
            if (destination == null) {
                return null;
            }
            return new ParsedLink(label, destination.url,
                    destination.title, destination.end);
        }

        String id = null;
        int end = next;
        if (next < source.length() && source.charAt(next) == '[') {
            int idEnd = findClosingBracket(source, next + 1);
            if (idEnd < 0) {
                return null;
            }
            id = source.substring(next + 1, idEnd);
            if (id.isEmpty()) {
                id = label;
            }
            end = idEnd + 1;
        } else {
            id = label;
        }
        if (id != null) {
            LinkReference reference = references.get(normalizeReference(id));
            if (reference != null) {
                return new ParsedLink(label, reference.destination,
                        reference.title, end);
            }
        }
        return null;
    }

    private Destination parseDestination(String source, int start) {
        int i = start;
        while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
            i++;
        }
        String url;
        if (i < source.length() && source.charAt(i) == '<') {
            int close = source.indexOf('>', i + 1);
            if (close < 0 || source.substring(i + 1, close).indexOf('\n') >= 0) {
                return null;
            }
            url = unescape(source.substring(i + 1, close));
            i = close + 1;
        } else {
            int begin = i;
            int parentheses = 0;
            while (i < source.length()) {
                char c = source.charAt(i);
                if (c == '\\' && i + 1 < source.length()) {
                    i += 2;
                    continue;
                }
                if (c == '(') {
                    if (++parentheses > 3) {
                        return null;
                    }
                } else if (c == ')') {
                    if (parentheses == 0) {
                        break;
                    }
                    parentheses--;
                } else if (Character.isWhitespace(c) && parentheses == 0) {
                    break;
                }
                i++;
            }
            if (i == begin) {
                url = "";
            } else {
                url = unescape(source.substring(begin, i));
            }
        }

        while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
            i++;
        }
        String title = null;
        if (i < source.length() && (source.charAt(i) == '"'
                || source.charAt(i) == '\'' || source.charAt(i) == '(')) {
            char opener = source.charAt(i++);
            char closer = opener == '(' ? ')' : opener;
            int begin = i;
            while (i < source.length() && source.charAt(i) != closer
                    && source.charAt(i) != '\n') {
                if (source.charAt(i) == '\\' && i + 1 < source.length()) {
                    i += 2;
                } else {
                    i++;
                }
            }
            if (i >= source.length() || source.charAt(i) != closer) {
                return null;
            }
            title = unescape(source.substring(begin, i));
            i++;
            while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
                i++;
            }
        }
        if (i >= source.length() || source.charAt(i) != ')') {
            return null;
        }
        return new Destination(url, title, i + 1);
    }

    private static int findClosingBracket(String source, int start) {
        int nested = 0;
        for (int i = start; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '[') {
                if (++nested > 32) {
                    return -1;
                }
            } else if (c == ']') {
                if (nested == 0) {
                    return i;
                }
                nested--;
            }
        }
        return -1;
    }

    static String normalizeReference(String value) {
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(trimmed.length());
        boolean whitespace = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isWhitespace(c)) {
                whitespace = true;
            } else {
                if (whitespace && out.length() > 0) {
                    out.append(' ');
                }
                whitespace = false;
                out.append(c);
            }
        }
        return out.toString();
    }

    private static int findDelimiter(String source, int start, String delimiter) {
        for (int i = start; i <= source.length() - delimiter.length(); i++) {
            if (source.charAt(i) == '\\') {
                i++;
                continue;
            }
            if (source.startsWith(delimiter, i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean canOpenEmphasis(String source, int at, char marker) {
        if (at + 1 >= source.length() || Character.isWhitespace(source.charAt(at + 1))) {
            return false;
        }
        return marker != '_' || at == 0
                || !Character.isLetterOrDigit(source.charAt(at - 1));
    }

    private static boolean canCloseEmphasis(String source, int at,
                                            int length, char marker) {
        if (at == 0 || Character.isWhitespace(source.charAt(at - 1))) {
            return false;
        }
        return marker != '_' || at + length >= source.length()
                || !Character.isLetterOrDigit(source.charAt(at + length));
    }

    private static int countRun(String source, int at, char marker) {
        int i = at;
        while (i < source.length() && source.charAt(i) == marker) {
            i++;
        }
        return i - at;
    }

    private static int findRun(String source, int start, char marker, int length) {
        for (int i = start; i < source.length(); i++) {
            if (source.charAt(i) == marker && countRun(source, i, marker) == length) {
                return i;
            }
        }
        return -1;
    }

    private static int trailingSpaces(String source, int newline) {
        int count = 0;
        for (int i = newline - 1; i >= 0 && source.charAt(i) == ' '; i--) {
            count++;
        }
        return count;
    }

    private static int trailingLiteralSpaces(StringBuilder out) {
        int count = 0;
        for (int i = out.length() - 1; i >= 0 && out.charAt(i) == ' '; i--) {
            count++;
        }
        return count;
    }

    private static int scanBareUrl(String source, int start) {
        int end = start;
        while (end < source.length() && !Character.isWhitespace(source.charAt(end))
                && source.charAt(end) != '<' && source.charAt(end) != '>') {
            end++;
        }
        while (end > start) {
            char last = source.charAt(end - 1);
            if (last == '.' || last == ',' || last == ';' || last == ':'
                    || last == '!' || last == '?' || last == '\'') {
                end--;
            } else if (last == ')' && count(source, start, end, '(')
                    < count(source, start, end, ')')) {
                end--;
            } else {
                break;
            }
        }
        return end;
    }

    private static int count(String value, int start, int end, char wanted) {
        int count = 0;
        for (int i = start; i < end; i++) {
            if (value.charAt(i) == wanted) {
                count++;
            }
        }
        return count;
    }

    private static boolean startsWithIgnoreCase(String source, int at, String prefix) {
        return at + prefix.length() <= source.length()
                && source.regionMatches(true, at, prefix, 0, prefix.length());
    }

    private static boolean isWebUrl(String value) {
        return startsWithIgnoreCase(value, 0, "http://")
                || startsWithIgnoreCase(value, 0, "https://");
    }

    private static boolean isEmail(String value) {
        int at = value.indexOf('@');
        return at > 0 && at == value.lastIndexOf('@')
                && at + 1 < value.length()
                && value.indexOf('.', at + 2) > at + 1
                && value.indexOf(' ') < 0;
    }

    private static boolean isAllSpaces(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    private static String unescape(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()
                    && ESCAPABLE.indexOf(value.charAt(i + 1)) >= 0) {
                out.append(value.charAt(++i));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String plainText(String value) {
        StringBuilder out = new StringBuilder(value.length());
        boolean marker = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                out.append(value.charAt(++i));
            } else if (c == '*' || c == '_' || c == '~' || c == '`') {
                marker = true;
            } else {
                out.append(c);
                marker = false;
            }
        }
        if (marker) {
            // A trailing marker is formatting punctuation, not alternative text.
        }
        return out.toString();
    }

    private static void appendLink(StringBuilder out, String destination,
                                   String label) {
        String url = HtmlSupport.safeUrl(destination, false);
        if (url == null) {
            out.append(HtmlSupport.escapeText(label));
            return;
        }
        out.append("<a href=\"").append(url).append("\">")
                .append(HtmlSupport.escapeText(label)).append("</a>");
    }

    private static void appendTitle(StringBuilder out, String title) {
        if (title != null && !title.isEmpty()) {
            out.append(" title=\"").append(HtmlSupport.escapeAttribute(title)).append('"');
        }
    }

    private static void appendEscapedChar(StringBuilder out, char c) {
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
            case 0:
                out.append('\uFFFD');
                break;
            default:
                out.append(c);
                break;
        }
    }

    private static final class ParsedLink {
        final String label;
        final String destination;
        final String title;
        final int end;

        ParsedLink(String label, String destination, String title, int end) {
            this.label = label;
            this.destination = destination;
            this.title = title;
            this.end = end;
        }
    }

    private static final class Destination {
        final String url;
        final String title;
        final int end;

        Destination(String url, String title, int end) {
            this.url = url;
            this.title = title;
            this.end = end;
        }
    }
}

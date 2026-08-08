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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MarkdownParser {
    private static final Pattern ATX_HEADING = Pattern.compile(
            "^ {0,3}(#{1,6})(?:[ \\t]+(.*?)|[ \\t]*)$");
    private static final Pattern SETEXT = Pattern.compile(
            "^ {0,3}(=+|-+)[ \\t]*$");
    private static final Pattern REFERENCE = Pattern.compile(
            "^ {0,3}\\[([^]\\n]+)]\\s*:\\s*(?:<([^>\\n]*)>|(\\S+))"
                    + "(?:\\s+(?:\"([^\"]*)\"|'([^']*)'|\\(([^)]*)\\)))?\\s*$");
    private static final Pattern LIST_ITEM = Pattern.compile(
            "^( {0,3})([*+-]|(\\d{1,9})[.)])([ \\t]+)(.*)$");
    private static final Pattern TASK_ITEM = Pattern.compile(
            "^\\[([ xX])]\\s+(.*)$");
    private static final Pattern TABLE_DELIMITER = Pattern.compile(
            "^:?-{3,}:?$");

    private final MarkdownOptions options;
    private final Map<String, LinkReference> references = new LinkedHashMap<>();
    private InlineParser inline;

    MarkdownParser(MarkdownOptions options) {
        this.options = options;
    }

    String render(String markdown) {
        String[] raw = markdown.split("\\n", -1);
        List<String> lines = new ArrayList<>(raw.length);
        for (String line : raw) {
            lines.add(line);
        }
        collectReferences(lines);
        inline = new InlineParser(options, references);
        return renderBlocks(lines, 0);
    }

    private String renderBlocks(List<String> lines, int depth) {
        if (depth >= options.getMaxNestingDepth()) {
            return renderDepthLimited(lines);
        }
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            if (isBlank(line)) {
                i++;
                continue;
            }
            if (parseReference(line) != null) {
                i++;
                continue;
            }

            Fence fence = parseFence(line);
            if (fence != null) {
                StringBuilder code = new StringBuilder();
                i++;
                while (i < lines.size() && !isClosingFence(lines.get(i), fence)) {
                    code.append(lines.get(i));
                    if (i + 1 < lines.size()) {
                        code.append('\n');
                    }
                    i++;
                }
                if (i < lines.size()) {
                    i++;
                }
                String language = HtmlSupport.languageClass(fence.info);
                out.append("<pre><code");
                if (!language.isEmpty()) {
                    out.append(" class=\"language-")
                            .append(HtmlSupport.escapeAttribute(language)).append('"');
                }
                out.append('>').append(HtmlSupport.escapeText(code.toString()))
                        .append("</code></pre>\n");
                continue;
            }

            Matcher heading = ATX_HEADING.matcher(line);
            if (heading.matches()) {
                int level = heading.group(1).length();
                String value = heading.group(2) == null ? "" : heading.group(2);
                value = stripClosingHashes(value);
                out.append("<h").append(level).append('>')
                        .append(inline.render(value))
                        .append("</h").append(level).append(">\n");
                i++;
                continue;
            }

            if (i + 1 < lines.size() && !isBlank(line)) {
                Matcher setext = SETEXT.matcher(lines.get(i + 1));
                if (setext.matches()) {
                    int level = setext.group(1).charAt(0) == '=' ? 1 : 2;
                    out.append("<h").append(level).append('>')
                            .append(inline.render(line.trim()))
                            .append("</h").append(level).append(">\n");
                    i += 2;
                    continue;
                }
            }

            if (isThematicBreak(line)) {
                out.append("<hr />\n");
                i++;
                continue;
            }

            if (options.isTablesEnabled() && i + 1 < lines.size()
                    && looksLikeTableHeader(line)
                    && isTableDelimiter(lines.get(i + 1))) {
                i = renderTable(lines, i, out);
                continue;
            }

            if (isBlockQuote(line)) {
                List<String> quote = new ArrayList<>();
                while (i < lines.size()) {
                    String candidate = lines.get(i);
                    if (isBlockQuote(candidate)) {
                        quote.add(stripBlockQuote(candidate));
                        i++;
                    } else if (isBlank(candidate) && i + 1 < lines.size()
                            && isBlockQuote(lines.get(i + 1))) {
                        quote.add("");
                        i++;
                    } else {
                        break;
                    }
                }
                out.append("<blockquote>\n")
                        .append(renderBlocks(quote, depth + 1))
                        .append("</blockquote>\n");
                continue;
            }

            Matcher list = LIST_ITEM.matcher(line);
            if (list.matches()) {
                i = renderList(lines, i, depth, out);
                continue;
            }

            if (leadingSpaces(line) >= 4) {
                StringBuilder code = new StringBuilder();
                while (i < lines.size()) {
                    String candidate = lines.get(i);
                    if (leadingSpaces(candidate) >= 4) {
                        code.append(candidate.substring(4)).append('\n');
                        i++;
                    } else if (isBlank(candidate)) {
                        code.append('\n');
                        i++;
                    } else {
                        break;
                    }
                }
                trimTrailingBlankLines(code);
                out.append("<pre><code>").append(HtmlSupport.escapeText(code.toString()))
                        .append("</code></pre>\n");
                continue;
            }

            StringBuilder paragraph = new StringBuilder(stripParagraphIndent(line));
            i++;
            while (i < lines.size() && !isBlank(lines.get(i))
                    && !startsBlock(lines, i)) {
                paragraph.append('\n').append(stripParagraphIndent(lines.get(i)));
                i++;
            }
            out.append("<p>").append(inline.render(paragraph.toString()))
                    .append("</p>\n");
        }
        return out.toString();
    }

    private int renderList(List<String> lines, int start, int depth,
                           StringBuilder out) {
        Matcher first = LIST_ITEM.matcher(lines.get(start));
        if (!first.matches()) {
            return start + 1;
        }
        int baseIndent = first.group(1).length();
        boolean ordered = first.group(3) != null;
        String tag = ordered ? "ol" : "ul";
        out.append('<').append(tag);
        if (ordered) {
            long number = parseListNumber(first.group(3));
            if (number != 1) {
                out.append(" start=\"").append(number).append('"');
            }
        }
        out.append(">\n");

        int i = start;
        while (i < lines.size()) {
            Matcher item = LIST_ITEM.matcher(lines.get(i));
            if (!item.matches() || item.group(1).length() != baseIndent
                    || (item.group(3) != null) != ordered) {
                break;
            }
            int contentIndent = item.start(5);
            List<String> itemLines = new ArrayList<>();
            itemLines.add(item.group(5));
            i++;
            while (i < lines.size()) {
                String candidate = lines.get(i);
                Matcher next = LIST_ITEM.matcher(candidate);
                if (next.matches() && next.group(1).length() == baseIndent) {
                    break;
                }
                if (isBlank(candidate)) {
                    if (i + 1 >= lines.size()) {
                        i++;
                        break;
                    }
                    String following = lines.get(i + 1);
                    Matcher followingItem = LIST_ITEM.matcher(following);
                    if (followingItem.matches()
                            && followingItem.group(1).length() == baseIndent) {
                        itemLines.add("");
                        i++;
                        break;
                    }
                    if (leadingSpaces(following) <= baseIndent
                            && !isBlank(following)) {
                        i++;
                        break;
                    }
                    itemLines.add("");
                    i++;
                    continue;
                }
                int indentation = leadingSpaces(candidate);
                if (indentation <= baseIndent) {
                    break;
                }
                itemLines.add(stripIndent(candidate, contentIndent));
                i++;
            }

            boolean checked = false;
            boolean task = false;
            if (options.isTaskListsEnabled() && !itemLines.isEmpty()) {
                Matcher taskMatcher = TASK_ITEM.matcher(itemLines.get(0));
                if (taskMatcher.matches()) {
                    task = true;
                    checked = !" ".equals(taskMatcher.group(1));
                    itemLines.set(0, taskMatcher.group(2));
                }
            }

            out.append("<li");
            if (task) {
                out.append(" class=\"task-list-item\"");
            }
            out.append('>');
            if (task) {
                out.append("<input type=\"checkbox\" disabled=\"disabled\"");
                if (checked) {
                    out.append(" checked=\"checked\"");
                }
                out.append(" /> ");
            }
            out.append(compactListItem(renderBlocks(itemLines, depth + 1)))
                    .append("</li>\n");
        }
        out.append("</").append(tag).append(">\n");
        return i;
    }

    private int renderTable(List<String> lines, int start, StringBuilder out) {
        List<String> headers = splitTableRow(lines.get(start));
        List<String> delimiters = splitTableRow(lines.get(start + 1));
        int columns = Math.min(headers.size(), delimiters.size());
        List<String> alignments = new ArrayList<>(columns);
        for (int column = 0; column < columns; column++) {
            String delimiter = delimiters.get(column).trim();
            boolean left = delimiter.startsWith(":");
            boolean right = delimiter.endsWith(":");
            alignments.add(left && right ? "center" : right ? "right" : left ? "left" : "");
        }

        out.append("<table>\n<thead>\n<tr>\n");
        for (int column = 0; column < columns; column++) {
            out.append("<th");
            appendAlignment(out, alignments.get(column));
            out.append('>').append(inline.render(headers.get(column).trim()))
                    .append("</th>\n");
        }
        out.append("</tr>\n</thead>\n<tbody>\n");
        int i = start + 2;
        while (i < lines.size() && !isBlank(lines.get(i))
                && containsUnescapedPipe(lines.get(i))) {
            List<String> cells = splitTableRow(lines.get(i));
            out.append("<tr>\n");
            for (int column = 0; column < columns; column++) {
                String cell = column < cells.size() ? cells.get(column).trim() : "";
                out.append("<td");
                appendAlignment(out, alignments.get(column));
                out.append('>').append(inline.render(cell)).append("</td>\n");
            }
            out.append("</tr>\n");
            i++;
        }
        out.append("</tbody>\n</table>\n");
        return i;
    }

    private boolean startsBlock(List<String> lines, int at) {
        String line = lines.get(at);
        if (parseReference(line) != null || parseFence(line) != null
                || ATX_HEADING.matcher(line).matches()
                || isThematicBreak(line) || isBlockQuote(line)
                || LIST_ITEM.matcher(line).matches() || leadingSpaces(line) >= 4) {
            return true;
        }
        return options.isTablesEnabled() && at + 1 < lines.size()
                && looksLikeTableHeader(line) && isTableDelimiter(lines.get(at + 1));
    }

    private void collectReferences(List<String> lines) {
        Fence activeFence = null;
        for (String line : lines) {
            if (activeFence != null) {
                if (isClosingFence(line, activeFence)) {
                    activeFence = null;
                }
                continue;
            }
            Fence fence = parseFence(line);
            if (fence != null) {
                activeFence = fence;
                continue;
            }
            if (leadingSpaces(line) >= 4) {
                continue;
            }
            ReferenceDefinition definition = parseReference(line);
            if (definition != null && !references.containsKey(definition.id)) {
                references.put(definition.id,
                        new LinkReference(definition.destination, definition.title));
            }
        }
    }

    private static ReferenceDefinition parseReference(String line) {
        Matcher matcher = REFERENCE.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        String id = InlineParser.normalizeReference(matcher.group(1));
        if (id.isEmpty()) {
            return null;
        }
        String destination = matcher.group(2) != null
                ? matcher.group(2) : matcher.group(3);
        String title = firstNonNull(matcher.group(4), matcher.group(5), matcher.group(6));
        return new ReferenceDefinition(id, destination, title);
    }

    private static Fence parseFence(String line) {
        int indent = leadingSpaces(line);
        if (indent > 3 || indent >= line.length()) {
            return null;
        }
        char marker = line.charAt(indent);
        if (marker != '`' && marker != '~') {
            return null;
        }
        int count = 0;
        while (indent + count < line.length()
                && line.charAt(indent + count) == marker) {
            count++;
        }
        if (count < 3) {
            return null;
        }
        String info = line.substring(indent + count).trim();
        if (marker == '`' && info.indexOf('`') >= 0) {
            return null;
        }
        return new Fence(marker, count, info);
    }

    private static boolean isClosingFence(String line, Fence opening) {
        int indent = leadingSpaces(line);
        if (indent > 3 || indent >= line.length()
                || line.charAt(indent) != opening.marker) {
            return false;
        }
        int count = 0;
        while (indent + count < line.length()
                && line.charAt(indent + count) == opening.marker) {
            count++;
        }
        if (count < opening.length) {
            return false;
        }
        return line.substring(indent + count).trim().isEmpty();
    }

    private static boolean isThematicBreak(String line) {
        String trimmed = line.trim();
        if (leadingSpaces(line) > 3 || trimmed.length() < 3) {
            return false;
        }
        char marker = trimmed.charAt(0);
        if (marker != '*' && marker != '-' && marker != '_') {
            return false;
        }
        int count = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == marker) {
                count++;
            } else if (c != ' ' && c != '\t') {
                return false;
            }
        }
        return count >= 3;
    }

    private static boolean isBlockQuote(String line) {
        int indent = leadingSpaces(line);
        return indent <= 3 && indent < line.length() && line.charAt(indent) == '>';
    }

    private static String stripBlockQuote(String line) {
        int at = leadingSpaces(line) + 1;
        if (at < line.length() && line.charAt(at) == ' ') {
            at++;
        }
        return line.substring(at);
    }

    private static boolean looksLikeTableHeader(String line) {
        return containsUnescapedPipe(line) && !isBlank(line);
    }

    private static boolean isTableDelimiter(String line) {
        List<String> cells = splitTableRow(line);
        if (cells.isEmpty()) {
            return false;
        }
        for (String cell : cells) {
            if (!TABLE_DELIMITER.matcher(cell.trim()).matches()) {
                return false;
            }
        }
        return true;
    }

    private static List<String> splitTableRow(String line) {
        String value = line.trim();
        int start = value.startsWith("|") ? 1 : 0;
        int end = value.endsWith("|") && !isEscaped(value, value.length() - 1)
                ? value.length() - 1 : value.length();
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        int codeRun = 0;
        for (int i = start; i < end; i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < end && value.charAt(i + 1) == '|') {
                cell.append('|');
                i++;
            } else if (c == '`') {
                int run = 1;
                while (i + run < end && value.charAt(i + run) == '`') {
                    run++;
                }
                codeRun = codeRun == 0 ? run : codeRun == run ? 0 : codeRun;
                for (int j = 0; j < run; j++) {
                    cell.append('`');
                }
                i += run - 1;
            } else if (c == '|' && codeRun == 0) {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(cell.toString());
        return cells;
    }

    private static boolean containsUnescapedPipe(String line) {
        int codeRun = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '`') {
                int run = 1;
                while (i + run < line.length() && line.charAt(i + run) == '`') {
                    run++;
                }
                codeRun = codeRun == 0 ? run : codeRun == run ? 0 : codeRun;
                i += run - 1;
            } else if (c == '|' && codeRun == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEscaped(String value, int at) {
        int slashes = 0;
        for (int i = at - 1; i >= 0 && value.charAt(i) == '\\'; i--) {
            slashes++;
        }
        return (slashes & 1) == 1;
    }

    private static void appendAlignment(StringBuilder out, String alignment) {
        if (!alignment.isEmpty()) {
            out.append(" align=\"").append(alignment).append('"');
        }
    }

    private static String compactListItem(String html) {
        if (!html.startsWith("<p>")) {
            return html;
        }
        int close = html.indexOf("</p>\n");
        if (close < 0) {
            return html;
        }
        return html.substring(3, close) + html.substring(close + 5);
    }

    private static String stripClosingHashes(String value) {
        int end = value.length();
        while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) {
            end--;
        }
        int hashes = end;
        while (hashes > 0 && value.charAt(hashes - 1) == '#') {
            hashes--;
        }
        if (hashes < end && hashes > 0
                && Character.isWhitespace(value.charAt(hashes - 1))) {
            end = hashes;
            while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) {
                end--;
            }
        }
        return value.substring(0, end);
    }

    private static String stripIndent(String value, int amount) {
        int remove = 0;
        while (remove < value.length() && remove < amount
                && value.charAt(remove) == ' ') {
            remove++;
        }
        return value.substring(remove);
    }

    private static String stripParagraphIndent(String value) {
        int remove = 0;
        while (remove < value.length() && remove < 3
                && value.charAt(remove) == ' ') {
            remove++;
        }
        return value.substring(remove);
    }

    private static long parseListNumber(String value) {
        try {
            long number = Long.parseLong(value);
            return number > 0 ? number : 1;
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private static int leadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && count < 4 && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    private static boolean isBlank(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static void trimTrailingBlankLines(StringBuilder value) {
        while (value.length() >= 2 && value.charAt(value.length() - 1) == '\n'
                && value.charAt(value.length() - 2) == '\n') {
            value.setLength(value.length() - 1);
        }
    }

    private static String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String renderDepthLimited(List<String> lines) {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                source.append('\n');
            }
            source.append(lines.get(i));
        }
        return "<p>" + HtmlSupport.escapeText(source.toString()) + "</p>\n";
    }

    private static final class Fence {
        final char marker;
        final int length;
        final String info;

        Fence(char marker, int length, String info) {
            this.marker = marker;
            this.length = length;
            this.info = info;
        }
    }

    private static final class ReferenceDefinition {
        final String id;
        final String destination;
        final String title;

        ReferenceDefinition(String id, String destination, String title) {
            this.id = id;
            this.destination = destination;
            this.title = title;
        }
    }
}

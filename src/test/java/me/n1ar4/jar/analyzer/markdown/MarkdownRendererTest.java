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

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class MarkdownRendererTest {
    @Test
    void rendersCoreBlockAndInlineMarkdown() {
        String markdown = "# Title\r\n\r\n"
                + "A **bold** and *small* value with `a < b`.\r\n\r\n"
                + "> quoted\r\n> text\r\n\r\n---\r\n";

        String html = MarkdownRenderer.toHtml(markdown);

        assertTrue(html.contains("<h1>Title</h1>"));
        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<em>small</em>"));
        assertTrue(html.contains("<code>a &lt; b</code>"));
        assertTrue(html.contains("<blockquote>"));
        assertTrue(html.contains("<p>quoted\ntext</p>"));
        assertTrue(html.contains("<hr />"));
    }

    @Test
    void rendersSetextHeadingsAndEscapedFormattingCharacters() {
        String html = MarkdownRenderer.toHtml(
                "Heading\n=======\n\nSubheading\n---\n\n\\*literal\\*");

        assertTrue(html.contains("<h1>Heading</h1>"));
        assertTrue(html.contains("<h2>Subheading</h2>"));
        assertTrue(html.contains("<p>*literal*</p>"));
    }

    @Test
    void rendersFencedAndIndentedCodeWithoutInterpretingMarkup() {
        String html = MarkdownRenderer.toHtml(
                "```java extra metadata\n<script>alert(1)</script>\n```\n\n"
                        + "    if (a < b) {\n    }\n");

        assertTrue(html.contains("class=\"language-java\""));
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertTrue(html.contains("if (a &lt; b)"));
        assertFalse(html.contains("<script>"));
    }

    @Test
    void rendersOrderedNestedAndTaskLists() {
        String html = MarkdownRenderer.toHtml(
                "3. third\n4. fourth\n\n"
                        + "- [x] complete\n  - nested\n- [ ] pending\n");

        assertTrue(html.contains("<ol start=\"3\">"));
        assertTrue(html.contains("<li>third</li>"));
        assertTrue(html.contains("<ul>\n<li>nested</li>"));
        assertTrue(html.contains("class=\"task-list-item\""));
        assertTrue(html.contains("checked=\"checked\""));
        assertEquals(2, occurrences(html, "type=\"checkbox\""));
    }

    @Test
    void rendersGfmTableAlignmentAndInlineContent() {
        String html = MarkdownRenderer.toHtml(
                "| Name | Result | Notes |\n"
                        + "| :--- | :---: | ---: |\n"
                        + "| A | **ok** | `x|y` |\n");

        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<th align=\"left\">Name</th>"));
        assertTrue(html.contains("<th align=\"center\">Result</th>"));
        assertTrue(html.contains("<td align=\"right\"><code>x|y</code></td>"));
        assertTrue(html.contains("<strong>ok</strong>"));
    }

    @Test
    void rendersInlineReferenceAndAutomaticLinks() {
        String html = MarkdownRenderer.toHtml(
                "[OpenAI](https://openai.com \"site\") and <dev@example.com>\n\n"
                        + "[docs][guide] ![logo][pic]\n\n"
                        + "[guide]: /guide \"Guide\"\n"
                        + "[pic]: /images/a.png\n");

        assertTrue(html.contains("href=\"https://openai.com\" title=\"site\""));
        assertTrue(html.contains("href=\"mailto:dev@example.com\""));
        assertTrue(html.contains("href=\"/guide\" title=\"Guide\""));
        assertTrue(html.contains("<img src=\"/images/a.png\" alt=\"logo\" />"));
    }

    @Test
    void supportsStrikethroughHardBreaksAndBareUrls() {
        String html = MarkdownRenderer.toHtml(
                "~~old~~  \nnew https://example.com/path_(x).\n");

        assertTrue(html.contains("<del>old</del><br />"));
        assertTrue(html.contains("href=\"https://example.com/path_(x)\""));
        assertFalse(html.contains("path_(x).\""));
    }

    @Test
    void escapesRawHtmlAndAllAttributeContexts() {
        String html = MarkdownRenderer.toHtml(
                "<script>alert('x')</script>\n\n"
                        + "[x](https://example.com/\"onmouseover=\"x)\n\n"
                        + "![\" onerror=\"x](/images/x.png)");

        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertFalse(html.contains(" onmouseover=\"x"));
        assertFalse(html.contains(" onerror=\"x"));
        assertTrue(html.contains("&quot;"));
    }

    @Test
    void removesDangerousAndObfuscatedSchemes() {
        String html = MarkdownRenderer.toHtml(
                "[one](javascript:alert(1)) "
                        + "[two](JaVaScRiPt:alert(1)) "
                        + "[three](java&#x73;cript:alert(1)) "
                        + "[four](%6aavascript:alert(1)) "
                        + "![five](data:image/svg+xml,x) "
                        + "[six](file:///etc/passwd)");

        assertFalse(html.contains("javascript"));
        assertFalse(html.contains("JaVaScRiPt"));
        assertFalse(html.contains("data:image"));
        assertFalse(html.contains("file:"));
        assertFalse(html.contains("href="));
        assertFalse(html.contains("src="));
        assertTrue(html.contains("one two three four five six"));
    }

    @Test
    void doesNotDoubleInterpretEntityBasedSchemes() {
        String html = MarkdownRenderer.toHtml("[bad](javascript&colon;alert(1))");

        assertEquals("<p>bad</p>\n", html);
    }

    @Test
    void remoteImagesRequireExplicitOptIn() {
        String markdown = "![remote](https://example.com/image.png)";

        assertEquals("<p>remote</p>\n", MarkdownRenderer.toHtml(markdown));

        MarkdownRenderer trustedRenderer = new MarkdownRenderer(
                MarkdownOptions.builder().remoteImages(true).build());
        assertEquals("<p><img src=\"https://example.com/image.png\" "
                        + "alt=\"remote\" /></p>\n",
                trustedRenderer.render(markdown));
    }

    @Test
    void leavesMalformedMarkdownReadable() {
        String html = MarkdownRenderer.toHtml(
                "unclosed **bold and [link](missing and `code\nnext");

        assertTrue(html.startsWith("<p>unclosed **bold"));
        assertTrue(html.contains("[link](missing"));
        assertTrue(html.contains("`code"));
    }

    @Test
    void honorsFeatureSwitches() {
        MarkdownOptions options = MarkdownOptions.builder()
                .tables(false)
                .taskLists(false)
                .strikethrough(false)
                .autolinks(false)
                .images(false)
                .build();
        String html = new MarkdownRenderer(options).render(
                "~~x~~ https://example.com ![x](https://example.com/x.png)\n\n"
                        + "- [x] item\n\nA | B\n--- | ---");

        assertFalse(html.contains("<del>"));
        assertFalse(html.contains("<a "));
        assertFalse(html.contains("<img "));
        assertFalse(html.contains("checkbox"));
        assertFalse(html.contains("<table>"));
    }

    @Test
    void supportsReaderAndEmptyInputs() throws Exception {
        MarkdownRenderer renderer = new MarkdownRenderer(MarkdownOptions.DEFAULT);

        assertEquals("", MarkdownRenderer.toHtml(null));
        assertEquals("", MarkdownRenderer.toHtml(""));
        assertEquals("<p>hello</p>\n", renderer.render(new StringReader("hello")));
    }

    @Test
    void rejectsOversizedStringsAndReaders() {
        MarkdownRenderer renderer = new MarkdownRenderer(
                MarkdownOptions.builder().maxInputCharacters(5).build());

        assertThrows(IllegalArgumentException.class,
                () -> renderer.render("123456"));
        assertThrows(IllegalArgumentException.class,
                () -> renderer.render(new StringReader("123456")));
    }

    @Test
    void capsRecursiveBlockAndInlineNesting() {
        MarkdownRenderer renderer = new MarkdownRenderer(
                MarkdownOptions.builder().maxNestingDepth(2).build());
        String html = renderer.render("> > > <script>x</script>\n\n****deep****");

        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.length() < 1000);
    }

    @Test
    void optionsValidateResourceLimits() {
        assertThrows(IllegalArgumentException.class,
                () -> MarkdownOptions.builder().maxInputCharacters(0));
        assertThrows(IllegalArgumentException.class,
                () -> MarkdownOptions.builder().maxNestingDepth(0));
        assertThrows(IllegalArgumentException.class,
                () -> MarkdownOptions.builder().maxNestingDepth(257));
        assertThrows(IllegalArgumentException.class,
                () -> new MarkdownRenderer(null));
    }

    @Test
    void remainsStableForDeterministicAdversarialInput() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            Random random = new Random(0x4d61726b646f776eL);
            String alphabet = "*_[]()<>`~|\\#-+!&;:'\" abcXYZ012\n"
                    + '\0' + "中文";
            for (int sample = 0; sample < 500; sample++) {
                int length = random.nextInt(600);
                StringBuilder markdown = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                    markdown.append(alphabet.charAt(random.nextInt(alphabet.length())));
                }
                String html = MarkdownRenderer.toHtml(markdown.toString());
                assertFalse(html.toLowerCase().contains("<script"));
                assertFalse(html.toLowerCase().contains("javascript:"));
            }
        });
    }

    @Test
    void defaultRendererIsSafeForConcurrentAiResponses() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(6);
        try {
            List<Callable<String>> jobs = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                final int id = i;
                jobs.add(() -> MarkdownRenderer.toHtml(
                        "## Answer " + id + "\n\n- **safe**\n- `code`\n"));
            }
            List<Future<String>> results = pool.invokeAll(jobs);
            for (int i = 0; i < results.size(); i++) {
                String html = results.get(i).get();
                assertTrue(html.contains("<h2>Answer " + i + "</h2>"));
                assertTrue(html.contains("<strong>safe</strong>"));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void rendersBundledChangelogAndThanksDocuments() throws Exception {
        String changelog = renderResource("/CHANGELOG.MD");
        String thanks = renderResource("/thanks.md");

        assertTrue(changelog.length() > 10_000);
        assertTrue(changelog.contains("<h"));
        assertTrue(changelog.contains("<ul>"));
        assertTrue(thanks.contains("<ol>"));
        assertTrue(thanks.contains("JAR ANALYZER"));
        assertFalse(changelog.toLowerCase().contains("<script"));
        assertFalse(thanks.toLowerCase().contains("<script"));
    }

    private static int occurrences(String value, String needle) {
        int count = 0;
        int at = 0;
        while ((at = value.indexOf(needle, at)) >= 0) {
            count++;
            at += needle.length();
        }
        return count;
    }

    private static String renderResource(String name) throws Exception {
        InputStream stream = MarkdownRendererTest.class.getResourceAsStream(name);
        assertTrue(stream != null, "missing test resource " + name);
        try (InputStreamReader reader = new InputStreamReader(
                stream, StandardCharsets.UTF_8)) {
            return new MarkdownRenderer(MarkdownOptions.DEFAULT).render(reader);
        }
    }
}

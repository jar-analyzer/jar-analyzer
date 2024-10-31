/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.lucene;

import cn.hutool.core.io.FileUtil;
import me.n1ar4.jar.analyzer.entity.LuceneSearchResult;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.DirUtil;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuceneSearchWrapper {
    private static final List<String> files = new ArrayList<>();

    public static void initEnv() {
        files.clear();
        files.addAll(DirUtil.GetFiles(Paths.get(Const.tempDir).toAbsolutePath().toString()));
    }

    private static boolean matchesRegex(String fileName, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.find();
    }

    public static  List<LuceneSearchResult> searchFileName(String input) {
        List<LuceneSearchResult> results = new ArrayList<>();
        for (String file : files) {
            if (LuceneSearchForm.useContains()) {
                String fileName = FileUtil.getName(file);
                if (fileName.contains(input)) {
                    LuceneSearchResult result = new LuceneSearchResult();
                    result.setType(LuceneSearchResult.TYPE_CLASS_NAME);
                    result.setFileName(fileName);
                    result.setAbsPathStr(file);
                    result.setContentStr(null);
                    results.add(result);
                }
            }
            if (LuceneSearchForm.useRegex()) {
                String fileName = FileUtil.getName(file);
                if (matchesRegex(fileName, input)) {
                    LuceneSearchResult result = new LuceneSearchResult();
                    result.setType(LuceneSearchResult.TYPE_CLASS_NAME);
                    result.setFileName(fileName);
                    result.setAbsPathStr(file);
                    result.setContentStr(null);
                    results.add(result);
                }
            }
        }
        return results;
    }
}

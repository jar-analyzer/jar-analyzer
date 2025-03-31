/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.lucene;

import cn.hutool.core.io.FileUtil;
import me.n1ar4.jar.analyzer.engine.index.IndexEngine;
import me.n1ar4.jar.analyzer.engine.index.entity.Result;
import me.n1ar4.jar.analyzer.entity.LuceneSearchResult;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuceneSearchWrapper {
    private static final Logger logger = LogManager.getLogger();
    private static final List<String> files = new ArrayList<>();
    public final static String CurrentPath = System.getProperty("user.dir");
    public final static String DocumentPath = CurrentPath + FileUtil.FILE_SEPARATOR + Const.indexDir;

    public static void initEnv() {
        files.clear();
        files.addAll(DirUtil.GetFiles(Paths.get(Const.tempDir).toAbsolutePath().toString()));
    }

    private static boolean matchesRegex(String fileName, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.find();
    }

    public static List<LuceneSearchResult> searchLucene(String input) {
        List<LuceneSearchResult> results = new ArrayList<>();
        // FIX BUG
        if (input == null || input.isEmpty()) {
            return results;
        }
        if (!checkValid()) {
            return results;
        }
        try {
            Result internalResult;
            if (LuceneSearchForm.useContains()) {
                internalResult = IndexEngine.searchNormal(input);
            } else if (LuceneSearchForm.useRegex()) {
                internalResult = IndexEngine.searchRegex(input);
            } else {
                logger.error("invalid lucene search type");
                return results;
            }

            for (Map<String, Object> data : internalResult.getData()) {
                String content = (String) data.get("content");
                String codePath = (String) data.get("path");
                String title = (String) data.get("title");

                LuceneSearchResult res = new LuceneSearchResult();
                res.setType(LuceneSearchResult.TYPE_CONTENT);
                res.setAbsPathStr(codePath);
                res.setFileName(FileUtil.getName(codePath));
                res.setContentStr(content);
                res.setTitle(title);
                res.setSearchKey(input);
                results.add(res);
            }
        } catch (Exception ex) {
            logger.error("lucene search error: {}", ex.toString());
        }
        return results;
    }

    private static boolean checkValid() {
        Path docPath = Paths.get(DocumentPath);
        if (!Files.exists(docPath)) {
            return false;
        }
        if (!Files.isDirectory(docPath)) {
            return false;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(docPath)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry) && Files.size(entry) > 0) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static List<LuceneSearchResult> searchFileName(String input) {
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
                    result.setSearchKey(input);
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
                    result.setSearchKey(input);
                    results.add(result);
                }
            }
        }
        return results;
    }
}

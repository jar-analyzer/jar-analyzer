/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.exporter;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.util.List;

public class SearchResultCsvExporter implements Exporter {
    private static final Logger logger = LogManager.getLogger();
    private final List<MethodResult> results;
    private String fileName;

    public SearchResultCsvExporter(List<MethodResult> results) {
        this.results = results;
    }

    @Override
    public boolean doExport() {
        if (results == null || results.isEmpty()) {
            logger.warn("no search results to export");
            return false;
        }

        this.fileName = String.format("search-results-%d.csv", System.currentTimeMillis());
        try (FileWriter out = new FileWriter(fileName);
             CSVPrinter printer = new CSVPrinter(out,
                     CSVFormat.DEFAULT.builder().setHeader(
                             "ClassName", "MethodName", "MethodDesc", "LineNumber"
                     ).get())) {

            for (MethodResult result : results) {
                printer.printRecord(
                        result.getClassName(),
                        result.getMethodName(),
                        result.getMethodDesc(),
                        result.getLineNumber()
                );
            }
            return true;
        } catch (Exception ex) {
            logger.error("export search results to CSV failed: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }
}

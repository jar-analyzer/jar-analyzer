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

package me.n1ar4.jar.analyzer.engine.index;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.engine.index.entity.Result;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class IndexEngine {
    public static String addIndexCollection(Map<String, String> analyzerMap) throws IOException {
        IndexWriter indexWriter = IndexSingletonClass.getIndexWriter();
        Collection<Document> documents = new ArrayList<>();
        analyzerMap.forEach((key, value) -> {
            value = StrUtil.removeAllLineBreaks(value);
            String[] cut = StrUtil.cut(value, 5000);
            for (int i = 0; i < cut.length; i++) {
                Document doc = new Document();
                doc.add(new StringField("order", String.valueOf(i), Field.Store.YES));
                doc.add(new StringField("content", cut[i], Field.Store.YES));
                doc.add(new StringField("codePath", key, Field.Store.YES));
                doc.add(new StringField("title",
                        StrUtil.removeSuffix(FileUtil.getName(key), ".class"), Field.Store.YES));
                documents.add(doc);
            }
        });
        indexWriter.addDocuments(documents);
        indexWriter.commit();
        return null;
    }

    public static String createIndex(String documentPath) throws IOException {
        Directory directory = FSDirectory.open(Paths.get(documentPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, conf);
        Document doc = new Document();
        doc.add(new StringField("version", IndexPluginsSupport.VERSION, Field.Store.YES));
        indexWriter.addDocument(doc);
        indexWriter.commit();
        indexWriter.close();
        return null;
    }


    public static Result search(String keyword) throws IOException, ParseException {
        IndexReader reader = IndexSingletonClass.getReader();
        keyword = StrUtil.removeAllLineBreaks(keyword);

        WildcardQuery query = new WildcardQuery(new Term("content", "*" + keyword + "*"));

        TopDocs topDocs = IndexSingletonClass.getSearcher().search(query, 110);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        List<Map<String, Object>> arrayList = new ArrayList<>();
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docID = scoreDoc.doc;
            Document doc = reader.document(docID);
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("path", doc.get("codePath"));
            map.put("content", doc.get("content"));
            map.put("title", doc.get("title"));
            map.put("order", Integer.parseInt(doc.get("order")));
            arrayList.add(map);
        }
        Result result = new Result();
        result.setTotal(topDocs.totalHits);
        result.setData(arrayList);
        return result;
    }

    public static Result searchRegex(String keyword) throws IOException {
        IndexReader reader = IndexSingletonClass.getReader();
        keyword = StrUtil.removeAllLineBreaks(keyword);

        RegexpQuery query = new RegexpQuery(new Term("content", ".*" + Pattern.quote(keyword) + ".*"));

        TopDocs topDocs = IndexSingletonClass.getSearcher().search(query, 110);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        List<Map<String, Object>> arrayList = new ArrayList<>();
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docID = scoreDoc.doc;
            Document doc = reader.document(docID);
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("path", doc.get("codePath"));
            map.put("content", doc.get("content"));
            map.put("title", doc.get("title"));
            map.put("order", Integer.parseInt(doc.get("order")));
            arrayList.add(map);
        }
        Result result = new Result();
        result.setTotal(topDocs.totalHits);
        result.setData(arrayList);
        return result;
    }

    public static class IndexSingletonClass {
        private static volatile IndexSearcher searcher = null;
        private static volatile IndexReader reader = null;
        private static volatile IndexWriter indexWriter = null;

        public static IndexSearcher getSearcher() throws IOException {
            if (searcher == null) {
                synchronized (IndexSingletonClass.class) {
                    if (searcher == null) {
                        IndexReader reader1 = getReader();
                        searcher = new IndexSearcher(reader1);
                    }
                }
            }
            return searcher;
        }

        public static IndexReader getReader() throws IOException {
            if (reader == null) {
                synchronized (IndexSingletonClass.class) {
                    if (reader == null) {
                        Directory directory = FSDirectory.open(Paths.get(IndexPluginsSupport.DocumentPath));
                        reader = DirectoryReader.open(directory);
                    }
                }
            }
            return reader;
        }

        public static IndexWriter getIndexWriter() throws IOException {
            if (indexWriter == null) {
                synchronized (IndexSingletonClass.class) {
                    if (indexWriter == null) {
                        Directory directory = FSDirectory.open(Paths.get(IndexPluginsSupport.DocumentPath));
                        Analyzer analyzer = new StandardAnalyzer();
                        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
                        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                        indexWriter = new IndexWriter(directory, conf);
                    }
                }
            }
            return indexWriter;
        }
    }
}

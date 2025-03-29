/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.engine.index;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.engine.index.entity.Result;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat;
import org.apache.lucene.codecs.lucene70.Lucene70Codec;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
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
    public static String initIndex(Map<String, String> analyzerMap) throws IOException {
        IndexWriter indexWriter = IndexSingletonClass.getIndexWriter();
        Collection<Document> documents = new ArrayList<>();
        analyzerMap.forEach((key, value) -> {
            List<String> split = StrUtil.split(value, "\n");
            for (int i = 0; i < split.size(); i++) {
                Document doc = new Document();
                doc.add(new StringField("order", String.valueOf(i), Field.Store.YES));
                doc.add(new StringField("content", split.get(i).trim(), Field.Store.YES));
                doc.add(new StringField("content_lower", split.get(i).trim().toLowerCase(), Field.Store.YES));
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


    public static Result search(String keyword) throws IOException {
        IndexReader reader = IndexSingletonClass.getReader();
        keyword = StrUtil.removeAllLineBreaks(keyword);
        WildcardQuery query;
        //区分/忽略大小写查询
        if (LuceneSearchForm.useCaseSensitive()) {
            query = new WildcardQuery(new Term("content", "*" + keyword.trim() + "*"));
        } else {
            query = new WildcardQuery(new Term("content_lower", "*" + keyword.trim().toLowerCase() + "*"));
        }
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
        RegexpQuery query = null;
        //区分/忽略大小写查询
        if (LuceneSearchForm.useCaseSensitive()) {
            query = new RegexpQuery(new Term("content", ".*" + Pattern.quote(keyword).trim() + ".*"));
        } else {
            query = new RegexpQuery(new Term("content_lower", ".*" + Pattern.quote(keyword).trim().toLowerCase() + ".*"));
        }

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
                        //优化了索引文件编码，提高存储效率
                        conf.setCodec(new Lucene70Codec(Lucene50StoredFieldsFormat.Mode.BEST_COMPRESSION));
                        indexWriter = new IndexWriter(directory, conf);
                    }
                }
            }
            return indexWriter;
        }
    }
}

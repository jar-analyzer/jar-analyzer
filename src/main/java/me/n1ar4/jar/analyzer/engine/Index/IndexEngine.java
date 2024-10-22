package me.n1ar4.jar.analyzer.engine.Index;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IndexEngine {
    private static final Logger logger = LogManager.getLogger();

    private static IndexSearcher searcher;
    private static IndexReader reader;

    public static String addIndexCollection(
            String documentPath,
            Map<String, String> analyzerMap) throws IOException {
        // 根据 analyzerMap 创建索引
        Directory directory = FSDirectory.open(Paths.get(documentPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(directory, conf);

        Collection<Document> documents = new ArrayList<>();
        analyzerMap.forEach((key, value) -> {
            value = StrUtil.cleanBlank(StrUtil.removeAllLineBreaks(value));
            String[] cut = StrUtil.cut(value, 30000);
            for (String s : cut) {
                Document doc = new Document();
                doc.add(new StringField("content", s, Field.Store.YES));
                doc.add(new StringField("codeName", FileUtil.getName(key), Field.Store.YES));
                doc.add(new StringField("codePath", key, Field.Store.YES));
                documents.add(doc);
            }
        });

        indexWriter.addDocuments(documents);
        indexWriter.commit();
        indexWriter.close();
        return null;
    }

    public static String createIndex(String documentPath) throws IOException {
        boolean exist = FileUtil.exist(documentPath);
        if (!exist) {
            FileUtil.mkdir(documentPath);
        }

        Directory directory = FSDirectory.open(Paths.get(documentPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, conf);

        indexWriter.commit();
        indexWriter.close();
        return null;
    }

    public static boolean deleteIndex() {
        return false;
    }

    public static boolean updateIndex() {
        return false;
    }

    public static boolean addDocument(String documentPath) {
        return false;
    }

    public static String search(String documentPath, String keyword) throws IOException, ParseException {
        if (searcher == null && reader == null) {
            Directory directory = FSDirectory.open(Paths.get(documentPath));
            reader = DirectoryReader.open(directory);
            searcher = new IndexSearcher(reader);
        }
        keyword = StrUtil.cleanBlank(StrUtil.removeAllLineBreaks(keyword));
        WildcardQuery query = new WildcardQuery(new Term("content", "*" + keyword + "*"));
        TopDocs topDocs = searcher.search(query, 10);
        logger.info("本次搜索共找到: {} 条数据" + topDocs.totalHits);

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            // 取出文档编号
            int docID = scoreDoc.doc;
            // 根据编号去找文档
            Document doc = reader.document(docID);
            // TODO
        }
        return "false";
    }

    public static List<Object> getIndexes() {
        return null;
    }
}

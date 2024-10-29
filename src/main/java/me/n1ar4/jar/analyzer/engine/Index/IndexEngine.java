package me.n1ar4.jar.analyzer.engine.Index;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.engine.Index.entity.Result;
import me.n1ar4.jar.analyzer.starter.Const;
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
    // 运行时目录
    public final static String CurrentPath = System.getProperty("user.dir");
    // 索引目录
    public final static String DoucumentPath = CurrentPath + FileUtil.FILE_SEPARATOR + Const.indexDir;
    // CLASS临时目录
    public final static String TempPath = CurrentPath + FileUtil.FILE_SEPARATOR + Const.tempDir;


    /**
     * 向索引集合中添加文档
     *
     * @param documentPath 索引文件路径
     * @param analyzerMap  待添加的文档集合，键为文档路径，值为文档内容
     * @return null
     * @throws IOException 如果文件操作出现异常
     */
    public static String addIndexCollection(String documentPath, Map<String, String> analyzerMap) throws IOException {
        IndexWriter indexWriter = IndexSingletonClass.getIndexWriter();
        Collection<Document> documents = new ArrayList<>();
        analyzerMap.forEach((key, value) -> {
            value = StrUtil.removeAllLineBreaks(value);
            String[] cut = StrUtil.cut(value, 30000);
            for (int i = 0; i < cut.length; i++) {
                Document doc = new Document();
                doc.add(new StringField("order", i + "", Field.Store.YES));
                doc.add(new StringField("content", cut[i], Field.Store.YES));
                doc.add(new StringField("codePath", key, Field.Store.YES));
                documents.add(doc);
            }
        });
        indexWriter.addDocuments(documents);
        indexWriter.commit();
        return null;
    }

    /**
     * 创建索引
     *
     * @param documentPath 索引文件路径
     * @return null
     * @throws IOException 如果文件操作出现异常
     */
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

    public static boolean addDoucment(String documentPath) {
        return false;
    }

    /**
     * 在指定的索引文件中搜索包含指定关键字的文档
     *
     * @param documentPath 索引文件路径
     * @param keyword      搜索关键字
     * @return 搜索结果，包含符合条件的文档列表和总数
     * @throws IOException 如果文件操作出现异常
     * @throws ParseException 如果解析结果时出错
     */
    public static Result search(String documentPath, String keyword) throws IOException, ParseException {
        IndexReader reader = IndexSingletonClass.getReader();
        keyword = StrUtil.removeAllLineBreaks(keyword);

        WildcardQuery query = new WildcardQuery(new Term("content", "*" + keyword + "*"));
//        keyword = ReUtil.escape(keyword);
        //RegexpQuery query = new RegexpQuery(new Term("content", ".*" + keyword+".*"));
        TopDocs topDocs = IndexSingletonClass.getSearcher().search(query, 110);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        List<Map<String, Object>> arrayList = new ArrayList<>();
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docID = scoreDoc.doc;
            Document doc = reader.document(docID);
            Map<String, Object> map = new java.util.HashMap<>();
            map.put(doc.get("codePath"), doc.get("content"));
            map.put("order", Integer.parseInt(doc.get("order")));
            arrayList.add(map);
        }
        Result result = new Result();
        result.setTotal(topDocs.totalHits);
        result.setData(arrayList);
        return result;
    }

    public static List<Object> getIndexs() {
        return null;
    }

    // 单例内部类
    public static class IndexSingletonClass {
        private static volatile IndexSearcher searcher = null;
        private static volatile IndexReader reader = null;
        private static volatile IndexWriter indexWriter = null;

        /**
         * 获取索引搜索器
         *
         * @return 索引搜索器
         * @throws IOException 如果文件操作出现异常
         */
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

        /**
         * 获取索引读取器
         *
         * @return 索引读取器
         * @throws IOException 如果文件操作出现异常
         */
        public static IndexReader getReader() throws IOException {
            if (reader == null) {
                synchronized (IndexSingletonClass.class) {
                    if (reader == null) {
                        Directory directory = FSDirectory.open(Paths.get(DoucumentPath));
                        reader = DirectoryReader.open(directory);
                    }
                }
            }
            return reader;
        }

        /**
         * 获取索引写入器
         *
         * @return IndexWriter 实例
         * @throws IOException 如果在打开文件或创建目录时发生错误，则抛出此异常
         */
        public static IndexWriter getIndexWriter() throws IOException {
            if (indexWriter == null) {
                synchronized (IndexSingletonClass.class) {
                    if (indexWriter == null) {
                        Directory directory = FSDirectory.open(Paths.get(DoucumentPath));
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

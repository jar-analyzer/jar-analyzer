package me.n1ar4.jar.analyzer.engine.Index.test;

import me.n1ar4.jar.analyzer.engine.Index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.engine.Index.entity.Result;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;


public class IndexPluginsSupportTest {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        //initIndex();  //第一步创建索引
        search();  //第二步搜索即可
    }

    /**
     * 初始化索引，创建索引
     * ！！！请确保temp目录下有class文件
     *
     * @return 是否生成完成
     */
    public static boolean initIndex() throws IOException, InterruptedException {
        boolean b = IndexPluginsSupport.initIndex();
        return b;
    }

    /**
     * 搜索
     * 请确保IndexPluginsSupport.initIndex();已经执行,且temp目录下有class文件,且索引文件已经存在
     *
     * @return 结果
     */
    public static Result search() throws IOException, InterruptedException, ParseException {
        Result search = IndexPluginsSupport.search("!!!替换成搜索关键字!!!");
        //Result search = IndexPluginsSupport.search("ID.randomUUID().getMostSignificantBits() + \"_\" + (new Date()).get");
        return search;
    }

}

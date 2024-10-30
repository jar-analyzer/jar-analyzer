package me.n1ar4.index;

import me.n1ar4.jar.analyzer.engine.Index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.engine.Index.entity.Result;

import java.io.IOException;


public class IndexPluginsSupportTest {
    public static void main(String[] args) throws Exception {
        boolean support = initIndex();
        if (support) {
            Result result = search();
            System.out.println(result);
        } else {
            System.out.println("test failed");
        }
        System.exit(0);
    }

    public static boolean initIndex() throws IOException, InterruptedException {
        return IndexPluginsSupport.initIndex();
    }

    public static Result search() throws Exception {
        return IndexPluginsSupport.search("[*] agent password");
    }
}

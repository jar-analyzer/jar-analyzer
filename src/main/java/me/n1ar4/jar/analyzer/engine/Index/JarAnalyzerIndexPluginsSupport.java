package me.n1ar4.jar.analyzer.engine.Index;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.engine.Index.entity.Result;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;


public class JarAnalyzerIndexPluginsSupport {
    // 获取最大逻辑处理器数量
    private static final Integer MAX_CORE = Runtime.getRuntime().availableProcessors();
    // 每个线程处理的文件数量
    private static final Integer MAX_SIZE_GROUP = 40;
    // 线程池
    private static final ExecutorService executorService = ExecutorBuilder.create()
            .setCorePoolSize(MAX_CORE * 2)
            .setMaxPoolSize(MAX_CORE * 3)
            .setWorkQueue(new LinkedBlockingQueue<>())
            .build();

    /**
     * 获取所有支持Jar分析器插件的文件列表
     *
     * @return 包含所有支持Jar分析器插件的文件列表
     * @description 该方法会遍历指定目录（TempPath）下的所有文件（包括子目录），筛选出所有以.class结尾的文件，并返回这些文件的File对象列表。
     * 注意，这里使用的是FileUtil工具类中的loopFiles方法来实现文件遍历和筛选逻辑。
     * 目前只能支持jar包中的class文件，不支持jar包中的其他文件
     */
    public static List<File> getJarAnalyzerPluginsSupportAllFiles() {
        //获取目录中的所有文件，包含子目录，可以排除特定文件，返回File对象列表
        List<File> files = FileUtil.loopFiles(IndexEngine.TempPath, pathname -> {
            //只返回.class文件
            return pathname.getName().endsWith(".class");
        });
        return files;
    }

    /**
     * 清除生成标识
     *
     * @param file jar文件
     * @return
     */
    public static String getCode(File file) {
        String decompile = null;
        try {
            decompile = DecompileEngine.decompile(file.toPath());
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return StrUtil.isNotBlank(decompile) ? decompile.replace(DecompileEngine.getFERN_PREFIX(), "") : null;
    }


    /**
     * 初始化索引
     *
     * @throws IOException 如果文件操作出现异常
     * @throws InterruptedException 如果线程等待被中断
     */
    public static void initIndex() throws IOException, InterruptedException {
        FileUtil.del(IndexEngine.DoucumentPath);
        int size = MAX_SIZE_GROUP;
        List<File> jarAnalyzerPluginsSupportAllFiles = getJarAnalyzerPluginsSupportAllFiles();
        IndexEngine.createIndex(IndexEngine.DoucumentPath);
        List<List<File>> split = CollUtil.split(jarAnalyzerPluginsSupportAllFiles, size);
        CountDownLatch latch = new CountDownLatch(split.size());
        for (List<File> files : split) {
            executorService.execute(() -> {
                Map<String, String> codeMap = new HashMap<>();
                files.forEach(file -> {
                    String code = getCode(file);
                    if (StrUtil.isNotBlank(code)) {
                        codeMap.put(file.getPath(), code);
                    }
                });
                try {
                    IndexEngine.addIndexCollection(IndexEngine.DoucumentPath, codeMap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                    System.out.println("latch.getCount() = " + latch.getCount());
                }
            });
        }
        latch.await();
        System.out.println();
    }


    /**
     * 根据关键词搜索文档
     *
     * @param keyword 关键词
     * @return 搜索结果
     * @throws IOException 如果文件操作出现异常
     * @throws ParseException 如果解析结果时出错
     */
    public static Result search(String keyword) throws IOException, ParseException {
        return IndexEngine.search(IndexEngine.DoucumentPath, keyword);
    }

}

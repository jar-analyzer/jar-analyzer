package me.n1ar4.jar.analyzer.engine.Index;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.starter.Const;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JarAnalyzerIndexPluginsSupport {
    public final static String CurrentPath = System.getProperty("user.dir");
    public final static String DoucumentPath = CurrentPath + FileUtil.FILE_SEPARATOR + Const.indexDir;
    public final static String TempPath = CurrentPath + FileUtil.FILE_SEPARATOR + Const.tempDir;


    public static List<File> getJarAnalyzerPluginsSupportAllFiles() {
        //获取目录中的所有文件，包含子目录，可以排除特定文件，返回File对象列表
        List<File> files = FileUtil.loopFiles(TempPath, pathname -> {
            //只返回.class文件
            if (pathname.getName().endsWith(".class")) {
                return true;
            }
            return false;
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
        String decompile = DecompileEngine.decompile(file.toPath());
        return decompile.replace(DecompileEngine.getFERN_PREFIX(), "");
    }

    public static void initIndex() throws IOException {
        List<File> jarAnalyzerPluginsSupportAllFiles = getJarAnalyzerPluginsSupportAllFiles();
        Map<String, String> codeMap = new HashMap<>();
        jarAnalyzerPluginsSupportAllFiles.forEach(file -> {
            String code = getCode(file);
            String path = file.getPath();
            codeMap.put(path, code);
        });
        IndexEngine.createIndex(DoucumentPath);
        IndexEngine.addIndexCollection(DoucumentPath, codeMap);
    }

    public static String search(String keyword) throws IOException, ParseException {
        return IndexEngine.search(DoucumentPath, keyword);
    }

}

package me.n1ar4.jar.analyzer.engine.Index;

import cn.hutool.core.io.FileUtil;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.starter.Const;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexPluginsSupport {
    public final static String CurrentPath = System.getProperty("user.dir");
    public final static String DocumentPath = CurrentPath + FileUtil.FILE_SEPARATOR + Const.indexDir;
    public final static String TempPath = CurrentPath + FileUtil.FILE_SEPARATOR + Const.tempDir;

    static {
        Path curPath = Paths.get(CurrentPath);
        Path indexPath = curPath.resolve(Const.indexDir);
        Path tempPath = curPath.resolve(Const.tempDir);
        // MKDIR TEMP DIR
        if (!Files.exists(tempPath)) {
            try {
                Files.createDirectories(tempPath);
            } catch (Exception ignored) {
            }
        }
        // MKDIR INDEX DIR
        if (!Files.exists(indexPath)) {
            try {
                Files.createDirectories(indexPath);
            } catch (Exception ignored) {
            }
        }
    }

    public static List<File> getJarAnalyzerPluginsSupportAllFiles() {
        // 获取目录中的所有文件 包含子目录 可以排除特定文件 返回File对象列表
        return FileUtil.loopFiles(TempPath, pathname -> {
            //只返回.class文件
            return pathname.getName().endsWith(".class");
        });
    }

    /**
     * 清除生成标识
     *
     * @param file jar文件
     * @return code
     */
    public static String getCode(File file) {
        String decompile = DecompileEngine.decompile(file.toPath());
        if (decompile == null) {
            return null;
        }
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
        IndexEngine.createIndex(DocumentPath);
        IndexEngine.addIndexCollection(DocumentPath, codeMap);
    }

    public static String search(String keyword) throws IOException, ParseException {
        return IndexEngine.search(DocumentPath, keyword);
    }
}

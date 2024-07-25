package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class DirUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final List<String> filenames = new ArrayList<>();

    public static List<String> GetFiles(String path) {
        filenames.clear();
        return getFiles(path);
    }

    private static List<String> getFiles(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return filenames;
            }
            for (File value : files) {
                if (value.isDirectory()) {
                    getFiles(value.getPath());
                } else {
                    filenames.add(value.getAbsolutePath());
                }
            }
        } else {
            filenames.add(file.getAbsolutePath());
        }
        return filenames;
    }

    public static boolean removeDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = removeDir(new File(dir, child));
                    if (!success) {
                        logger.debug("remove dir {} not success", dir.toString());
                        // 由于 DLL 文件不能删除
                        // 这里应该继续删除不能返回
                    }
                }
            }
        }
        if (!dir.delete()) {
            logger.debug("remove dir {} not success", dir.toString());
            return false;
        } else {
            return true;
        }
    }
}

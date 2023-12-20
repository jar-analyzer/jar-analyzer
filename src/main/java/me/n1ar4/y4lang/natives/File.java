package me.n1ar4.y4lang.natives;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.exception.Y4LangException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class File {
    private static final Logger logger = LogManager.getLogger();
    public static int writeFile(String filename, String data) {
        try {
            Path path = Paths.get(filename);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Files.write(path, data.getBytes(StandardCharsets.UTF_8));
            return Environment.TRUE;
        } catch (Exception e) {
            logger.error("write file error: {}",e.toString());
        }
        return Environment.FALSE;
    }

    public static String readFile(String filename) {
        try {
            Path path = Paths.get(filename);
            if (!Files.exists(path)) {
                throw new Y4LangException("file not exist");
            }
            byte[] data = Files.readAllBytes(path);
            return new String(data);
        } catch (Exception e) {
            logger.error("read file error: {}",e.toString());
        }
        return "";
    }
}

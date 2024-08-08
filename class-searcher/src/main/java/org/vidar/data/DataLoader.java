package org.vidar.data;

import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author zhchen
 */
public class DataLoader {
    public static <T> List<T> loadData(Path filePath, DataFactory<T> factory) throws IOException {
        if (!filePath.toFile().exists()) {
            return Collections.EMPTY_LIST;
        }
        final List<String> lines = Files.readLines(filePath.toFile(), StandardCharsets.UTF_8);
        final List<T> values = new ArrayList<T>(lines.size());
        for (String line : lines) {
            values.add(factory.parse(line.split("\t", -1)));
        }
        return values;
    }

    public static <T> void saveData(Path filePath, DataFactory<T> factory, Collection<T> values) throws IOException {
        try (BufferedWriter writer = Files.newWriter(filePath.toFile(), StandardCharsets.UTF_8)) {
            for (T value : values) {
                final String[] fields = factory.serialize(value);
                if (fields == null) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                for (String field : fields) {
                    if (field == null) {
                        sb.append("\t");
                    } else {
                        sb.append("\t").append(field);
                    }
                }
                writer.write(sb.substring(1));
                writer.write("\n");
            }
        }
    }

    /**
     * 从classes.dat加载类信息
     *
     * @return
     */
    public static Map<ClassReference.Handle, ClassReference> loadClasses() {
        try {
            Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
            for (ClassReference classReference : loadData(Paths.get("classes.dat"), new ClassReference.Factory())) {
                classMap.put(classReference.getHandle(), classReference);
            }
            return classMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从methods.dat加载所有方法信息
     *
     * @return
     */
    public static Map<MethodReference.Handle, MethodReference> loadMethods() {
        try {
            Map<MethodReference.Handle, MethodReference> methodMap = new HashMap<>();
            for (MethodReference methodReference : loadData(Paths.get("methods.dat"), new MethodReference.Factory())) {
                methodMap.put(methodReference.getHandle(), methodReference);
            }
            return methodMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从calls.dat中加载方法call信息
     *
     * @return
     */
    public static HashMap<MethodReference.Handle, GraphCall> loadCalls() {
        try {
            HashMap<MethodReference.Handle, GraphCall> callMap = new HashMap<>();
            for (GraphCall graphCall : loadData(Paths.get("calls.dat"), new GraphCall.Factory())) {
                callMap.put(graphCall.getMethod(), graphCall);
            }
            return callMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

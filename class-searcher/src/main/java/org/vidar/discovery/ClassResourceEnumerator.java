package org.vidar.discovery;


import com.google.common.reflect.ClassPath;
import org.vidar.ConfigHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 获取用于分析的类文件(ClassResource)
 *
 * @author zhchen
 */
public class ClassResourceEnumerator {
    private final ClassLoader classLoader;

    public ClassResourceEnumerator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * java runtime所有的classes(rt.jar)和指定的jar或war中的所有class
     *
     * @return
     * @throws IOException
     */
    public Collection<ClassResource> getAllClasses() throws IOException {
        ArrayList<ClassResource> result = new ArrayList<>(getRuntimeClasses());
        if (ConfigHelper.onlyJDK) {
            return result;
        }
        for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getAllClasses()) {
            result.add(new ClassLoaderClassResource(classLoader, classInfo.getResourceName()));
        }
        return result;
    }

    private Collection<ClassResource> getRuntimeClasses() throws IOException {
        // 加载rt.jar的所有class,适用于java8
        URL stringClassUrl = Object.class.getResource("String.class");
        URLConnection connection = stringClassUrl.openConnection();
        Collection<ClassResource> result = new ArrayList<>();
        if (connection instanceof JarURLConnection) {
            URL runtimeURL = ((JarURLConnection) connection).getJarFileURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{runtimeURL});

            for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getAllClasses()) {
                result.add(new ClassLoaderClassResource(classLoader, classInfo.getResourceName()));
            }
        }
        if (!result.isEmpty()) {
            return result;
        }

        // 如果result为空，则尝试Java9的find classes方法
        try {
            FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
            fs.getPath("/").forEach(path -> {
                if (path.toString().toLowerCase().endsWith(".class")) {
                    result.add(new PathClassResource(path));
                }
            });
        } catch (ProviderNotFoundException e) {
            // Do nothing; this is expected on versions below Java9
        }

        return result;
    }
}

interface ClassResource {
    public InputStream getInputStream() throws IOException;

    public String getName();
}

class ClassLoaderClassResource implements ClassResource {
    private final ClassLoader classLoader;
    private final String resourceName;

    ClassLoaderClassResource(ClassLoader classLoader, String resourceName) {
        this.classLoader = classLoader;
        this.resourceName = resourceName;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return classLoader.getResourceAsStream(resourceName);
    }

    @Override
    public String getName() {
        return resourceName;
    }
}

class PathClassResource implements ClassResource {
    private final Path path;

    PathClassResource(Path path) {
        this.path = path;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public String getName() {
        return path.toString();
    }
}

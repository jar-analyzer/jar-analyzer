/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.analyze.frame;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SimpleFrameEngine {
    private static final Logger logger = LogManager.getLogger();

    public static String start(InputStream is,
                               String methodName,
                               String methodDesc,
                               StringBuilder builder) throws Exception {
        ClassReader cr = new ClassReader(is);
        ClassNode cn = new ClassNode();
        cr.accept(cn, Const.GlobalASMOptions);
        String owner = cn.name;
        List<MethodNode> methods = cn.methods;
        MethodNode method = null;
        for (MethodNode mn : methods) {
            if (mn.name.equals(methodName) && mn.desc.equals(methodDesc)) {
                method = mn;
            }
        }
        if (method != null) {
            SimpleVerifier simpleVerifier = null;
            try {
                simpleVerifier = new SimpleVerifier();
                Field loaderField = simpleVerifier.getClass().getDeclaredField("loader");
                loaderField.setAccessible(true);

                List<URL> urls = new ArrayList<>();
                for (String path : MainForm.getEngine().getJarsPath()) {
                    urls.add(new URL(String.format("%s:/%s", "file", path)));
                    logger.info("add url: {}", Paths.get(path).getFileName());
                }

                URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]),
                        ClassLoader.getSystemClassLoader());
                loaderField.set(simpleVerifier, classLoader);

                logger.info("change classloader finish");
            } catch (Exception ex) {
                logger.error("hack simple verifier error: {}", ex.getMessage());
            }

            print(owner, method, simpleVerifier, builder,
                    item -> ValueUtils.fromBasicValue2String(item) + "@" + System.identityHashCode(item));
        }
        return null;
    }

    public static <V extends Value, T> void print(String owner,
                                                  MethodNode mn,
                                                  Interpreter<V> interpreter,
                                                  StringBuilder builder,
                                                  Function<V, T> func) throws AnalyzerException {
        Analyzer<V> analyzer = new Analyzer<>(interpreter);
        FrameUtils.printFrames(owner, mn, analyzer, func, builder);
    }
}

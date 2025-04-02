/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;

public class JavaWebClassVisitor extends ClassVisitor {
    private final ArrayList<String> interceptors;
    private final ArrayList<String> servlets;
    private final ArrayList<String> filters;
    private final ArrayList<String> listeners;

    public JavaWebClassVisitor(
            ArrayList<String> interceptors,
            ArrayList<String> servlets,
            ArrayList<String> filters,
            ArrayList<String> listeners) {
        super(Const.ASMVersion);
        this.interceptors = interceptors;
        this.servlets = servlets;
        this.filters = filters;
        this.listeners = listeners;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        String currentClassName = name.replace('/', '.');

        if (interfaces != null) {
            for (String iface : interfaces) {
                String ifaceName = iface.replace('/', '.');

                if (ifaceName.equals("org.springframework.web.servlet.HandlerInterceptor") ||
                        ifaceName.equals("org.springframework.web.servlet.AsyncHandlerInterceptor")) {
                    interceptors.add(currentClassName.replace(".", "/"));
                }

                if (ifaceName.equals("javax.servlet.Servlet") ||
                        ifaceName.equals("jakarta.servlet.Servlet")) {
                    servlets.add(currentClassName.replace(".", "/"));
                }

                if (ifaceName.equals("javax.servlet.Filter") ||
                        ifaceName.equals("jakarta.servlet.Filter")) {
                    filters.add(currentClassName.replace(".", "/"));
                }

                if (ifaceName.equals("javax.servlet.ServletContextListener") ||
                        ifaceName.equals("jakarta.servlet.ServletContextListener") ||
                        ifaceName.equals("javax.servlet.ServletRequestListener") ||
                        ifaceName.equals("jakarta.servlet.ServletRequestListener") ||
                        ifaceName.equals("javax.servlet.http.HttpSessionListener") ||
                        ifaceName.equals("jakarta.servlet.http.HttpSessionListener")) {
                    listeners.add(currentClassName.replace(".", "/"));
                }
            }
        }

        if (superName != null) {
            String superClassName = superName.replace('/', '.');
            if (superClassName.equals("javax.servlet.http.HttpServlet") ||
                    superClassName.equals("jakarta.servlet.http.HttpServlet")) {
                servlets.add(currentClassName.replace(".", "/"));
            }
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }
}

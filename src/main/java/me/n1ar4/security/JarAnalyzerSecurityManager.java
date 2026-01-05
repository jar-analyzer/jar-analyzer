/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.security;

import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.IPUtil;

import javax.swing.*;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;

@SuppressWarnings("all")
public class JarAnalyzerSecurityManager extends SecurityManager {
    @Override
    public void checkAccept(String host, int port) {
    }

    @Override
    public void checkAccess(Thread t) {
    }

    @Override
    public void checkAccess(ThreadGroup g) {
    }

    @Override
    public void checkConnect(String host, int port) {
        if (!GlobalOptions.isSecurityMode()) {
            return;
        }
        if (port < 1) {
            return;
        }
        if (host.equals("127.0.0.1") || host.equals("localhost")) {
            return;
        }
        if (IPUtil.isValidIPAddress(host) && port > 0) {
            SecurityLog.log(String.format("JVM CONNECT %s:%d", host, port));
        }
        int result = JOptionPane.showConfirmDialog(
                null,
                String.format("<html>" +
                        "<p>jar-analyzer security 检测到正在发起网络连接 请进行检查</p>" +
                        "<p>host: %s</p>" +
                        "<p>port: %d</p>" +
                        "</html>", host, port),
                "JAVA SECURITY WARNING",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.NO_OPTION) {
            throw new SecurityException("user stop");
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        checkConnect(host, port);
    }

    @Override
    public void checkCreateClassLoader() {
    }

    @Override
    public void checkDelete(String file) {
        if (!GlobalOptions.isSecurityMode()) {
            return;
        }
        // 正常行为
        String tempDir = System.getProperty("java.io.tmpdir");
        Path filePath = Paths.get(file).toAbsolutePath();
        Path tempPath = Paths.get(tempDir).toAbsolutePath();
        // 正常行为
        Path constTempPath = Paths.get(Const.tempDir).toAbsolutePath();
        if (filePath.startsWith(tempPath) || filePath.startsWith(constTempPath)) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(
                null,
                String.format("<html>" +
                        "<p>jar-analyzer security 检测到正在删除文件 请进行检查</p>" +
                        "<p>file name: %s</p>" +
                        "</html>", file),
                "JAVA SECURITY WARNING",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.NO_OPTION) {
            throw new SecurityException("user stop");
        }
    }

    @Override
    public void checkExec(String cmd) {
        SecurityLog.log(String.format("JVM RUN CMD: %s", cmd));
        if (!GlobalOptions.isSecurityMode()) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(
                null,
                String.format("<html>" +
                        "<p>jar-analyzer security 检测到正在执行系统命令 请进行检查</p>" +
                        "<p>command: %s</p>" +
                        "</html>", cmd),
                "JAVA SECURITY WARNING",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.NO_OPTION) {
            throw new SecurityException("user stop");
        }
    }

    @Override
    public void checkExit(int status) {
    }

    @Override
    public void checkLink(String lib) {
    }

    @Override
    public void checkListen(int port) {
        SecurityLog.log(String.format("JVM LISTEN PORT: %d", port));
        if (!GlobalOptions.isSecurityMode()) {
            return;
        }
        if (port < 1) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(
                null,
                String.format("<html>" +
                        "<p>jar-analyzer security 检测到正在监听端口 请进行检查</p>" +
                        "<p>port: %d</p>" +
                        "</html>", port),
                "JAVA SECURITY WARNING",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.NO_OPTION) {
            throw new SecurityException("user stop");
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
    }

    @Override
    public void checkPackageAccess(String pkg) {
    }

    @Override
    public void checkPackageDefinition(String pkg) {
    }

    @Override
    public void checkPermission(Permission perm) {
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }

    @Override
    public void checkPrintJobAccess() {
    }

    @Override
    public void checkPropertiesAccess() {
    }

    @Override
    public void checkPropertyAccess(String key) {
    }

    @Override
    public void checkRead(String file) {
    }

    @Override
    public void checkRead(FileDescriptor fd) {
    }

    @Override
    public void checkRead(String file, Object context) {
    }

    @Override
    public void checkSecurityAccess(String target) {
    }

    @Override
    public void checkSetFactory() {
    }

    @Override
    public void checkWrite(String file) {
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
    }
}

package me.n1ar4.security;

import me.n1ar4.jar.analyzer.utils.IPUtil;

import java.io.FileDescriptor;
import java.net.InetAddress;
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
        if (IPUtil.isValidIPAddress(host) && port > 0) {
            SecurityLog.log(String.format("JVM CONNECT %s:%d", host, port));
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
    }

    @Override
    public void checkCreateClassLoader() {
    }

    @Override
    public void checkDelete(String file) {
    }

    @Override
    public void checkExec(String cmd) {
        SecurityLog.log(String.format("JVM RUN CMD: %s", cmd));
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

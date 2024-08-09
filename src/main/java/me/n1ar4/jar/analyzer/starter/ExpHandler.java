package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ExpHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            Class<?> edtClass = Class.forName("java.awt.EventDispatchThread");
            if (edtClass.isInstance(t)) {
                if (e instanceof ArrayIndexOutOfBoundsException) {
                    // 这里是一处已知的异常
                    // java/util/Vector elementAt ArrayIndexOutOfBoundsException
                    return;
                }
            }
            // 处理下异常：不抛出异常 记录到当前目录
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] items = e.getStackTrace();
            for (StackTraceElement item : items) {
                String info = String.format("%s.%s:%d\n",
                        item.getClassName(),
                        item.getMethodName(),
                        item.getLineNumber());
                sb.append(info);
            }
            Files.write(Paths.get("JAR-ANALYZER-ERROR.txt"), sb.toString().getBytes());
            logger.error("UNCAUGHT EXCEPTION LOGGED IN JAR-ANALYZER-ERROR.txt");
        } catch (Exception ex) {
            logger.warn("handle thread error: {}", ex.toString());
        }
    }
}

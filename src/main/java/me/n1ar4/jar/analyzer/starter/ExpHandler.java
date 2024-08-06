package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

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
            logger.warn("thread error: {}", e.toString());
        } catch (Exception ex) {
            logger.warn("handle thread error: {}", ex.toString());
        }
    }
}

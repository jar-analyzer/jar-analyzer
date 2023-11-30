package me.n1ar4.y4json.log;

@SuppressWarnings("all")
public class Logger {
    public void info(String message) {
        Log.info(message);
    }

    public void error(String message) {
        Log.error(message);
    }

    public void debug(String message) {
        Log.debug(message);
    }

    public void warn(String message) {
        Log.warn(message);
    }
}

package me.n1ar4.y4lang.natives;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.exception.Y4LangException;
import me.n1ar4.y4lang.function.NativeFunction;

import java.lang.reflect.Method;

public class Natives {
    public Environment environment(Environment env) {
        appendNatives(env);
        return env;
    }

    protected void appendNatives(Environment env) {
        // Print
        append(env, "print", Print.class, "print",
                Object.class);

        // Util
        append(env, "length", Util.class, "length",
                Object.class);

        // Input
        append(env, "input", Input.class, "input");

        // Convert
        append(env, "toInt", Convert.class, "toInt",
                Object.class);
        append(env, "toStr", Convert.class, "toStr",
                Object.class);

        // Time
        append(env, "currentTime", Time.class, "currentTime");
        append(env, "formatTime", Time.class, "formatTime");

        // File
        append(env, "readFile", File.class, "readFile",
                String.class);
        append(env, "writeFile", File.class, "writeFile",
                String.class, String.class);

        // Collection
        append(env, "newMap", Collection.class, "newMap");
        append(env, "putMap", Collection.class, "putMap",
                Object.class, Object.class, Object.class);
        append(env, "getMap", Collection.class, "getMap",
                Object.class, Object.class);
        append(env, "clearMap", Collection.class, "clearMap",
                Object.class);
    }

    protected void append(Environment env, String name, Class<?> clazz,
                          String methodName, Class<?>... params) {
        Method m;
        try {
            m = clazz.getMethod(methodName, params);
        } catch (Exception e) {
            throw new Y4LangException("cannot find a native function: " + methodName);
        }
        env.put(name, new NativeFunction(methodName, m));
    }
}

package com.n1ar4.agent.util;

public class ThreadUtils {

    public static Thread[] getThreads(){
        return (Thread[]) ReflectUtils.callStaticMethod(Thread.class, "getThreads");
    }

}

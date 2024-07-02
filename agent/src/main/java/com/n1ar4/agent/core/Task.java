package com.n1ar4.agent.core;

import com.n1ar4.agent.Agent;
import com.n1ar4.agent.transform.*;
import com.n1ar4.agent.util.FilterObjectInputStream;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Task implements Runnable {
    private final Socket socket;

    public Task(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            handleSocket();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSocket() throws Exception {
        ObjectInputStream ois;
        try {
            ois = new FilterObjectInputStream(socket.getInputStream());
        } catch (Exception ex) {
            return;
        }
        String targetClass = (String) ois.readObject();

        if (targetClass.startsWith("<ALL>")) {
            String PASS = targetClass.split("<ALL>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            List<String> classNameList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                if (c.getName().startsWith("[")) {
                    continue;
                }
                if (c.getName().contains("$$Lambda")) {
                    continue;
                }
                classNameList.add(c.getName());
            }
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classNameList);
            System.out.println("write data to socket: " + classNameList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<FILTERS>")) {
            String PASS = targetClass.split("<FILTERS>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            List<String> classList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                try {
                    ClassLoader classLoader;
                    if (c.getClassLoader() != null) {
                        classLoader = c.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }

                    Class<?> clsFilter = null;
                    try {
                        clsFilter = classLoader.loadClass("javax.servlet.Filter");
                    } catch (Exception ignored) {
                    }

                    if (clsFilter != null && clsFilter.isAssignableFrom(c)) {
                        classList.add(c.getName());
                    }
                } catch (Exception ignored) {
                }
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classList);
            System.out.println("write data to socket: " + classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<VALVES>")) {
            String PASS = targetClass.split("<VALVES>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            List<String> classList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                try {
                    ClassLoader classLoader;
                    if (c.getClassLoader() != null) {
                        classLoader = c.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }

                    Class<?> clsFilter = null;
                    try {
                        clsFilter = classLoader.loadClass("org.apache.catalina.Valve");
                    } catch (Exception ignored) {
                    }

                    if (clsFilter != null && clsFilter.isAssignableFrom(c)) {
                        classList.add(c.getName());
                    }
                } catch (Exception ignored) {
                }
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classList);
            System.out.println("write data to socket: " + classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<SERVLETS>")) {
            String PASS = targetClass.split("<SERVLETS>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            List<String> classList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                try {
                    ClassLoader classLoader;
                    if (c.getClassLoader() != null) {
                        classLoader = c.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }

                    Class<?> clsFilter = null;
                    try {
                        clsFilter = classLoader.loadClass("javax.servlet.Servlet");
                    } catch (Exception ignored) {
                    }

                    if (clsFilter != null && clsFilter.isAssignableFrom(c)) {
                        classList.add(c.getName());
                    }
                } catch (Exception ignored) {
                }
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classList);
            System.out.println("write data to socket: " + classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<LISTENERS>")) {
            String PASS = targetClass.split("<LISTENERS>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            List<String> classList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                try {
                    ClassLoader classLoader;
                    if (c.getClassLoader() != null) {
                        classLoader = c.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }

                    Class<?> clsFilter = null;
                    try {
                        clsFilter = classLoader.loadClass("javax.servlet.ServletRequestListener");
                    } catch (Exception ignored) {
                    }

                    if (clsFilter != null && clsFilter.isAssignableFrom(c)) {
                        classList.add(c.getName());
                    }
                } catch (Exception ignored) {
                }
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classList);
            System.out.println("write data to socket: " + classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<KILL-FILTER>")) {
            String f = targetClass.split("<KILL-FILTER>")[1];
            if (!f.split("\\|")[0].equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            f = f.split("\\|")[1];
            System.out.println("kill filter: " + f);
            FilterKill fk = new FilterKill(f);
            for (Class<?> c : Agent.staticClasses) {
                if (c.getName().equals(f)) {
                    Agent.staticIns.addTransformer(fk, true);
                    Agent.staticIns.retransformClasses(c);
                    Agent.staticIns.removeTransformer(fk);
                }
            }
        }

        if (targetClass.startsWith("<KILL-SERVLET>")) {
            String s = targetClass.split("<KILL-SERVLET>")[1];
            if (!s.split("\\|")[0].equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            s = s.split("\\|")[1];
            System.out.println("kill servlet: " + s);
            ServletKill sk = new ServletKill(s);
            for (Class<?> c : Agent.staticClasses) {
                if (c.getName().equals(s)) {
                    Agent.staticIns.addTransformer(sk, true);
                    Agent.staticIns.retransformClasses(c);
                    Agent.staticIns.removeTransformer(sk);
                }
            }
        }

        if (targetClass.startsWith("<KILL-LISTENER>")) {
            String s = targetClass.split("<KILL-LISTENER>")[1];
            if (!s.split("\\|")[0].equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            s = s.split("\\|")[1];
            System.out.println("kill listener: " + s);
            ListenerKill sk = new ListenerKill(s);
            for (Class<?> c : Agent.staticClasses) {
                if (c.getName().equals(s)) {
                    Agent.staticIns.addTransformer(sk, true);
                    Agent.staticIns.retransformClasses(c);
                    Agent.staticIns.removeTransformer(sk);
                }
            }
        }

        if (targetClass.startsWith("<KILL-VALVE>")) {
            String s = targetClass.split("<KILL-VALVE>")[1];
            if (!s.split("\\|")[0].equals(Agent.PASSWORD)) {
                System.out.println("!!! ERROR PASSWORD");
                return;
            }
            s = s.split("\\|")[1];
            System.out.println("kill valve: " + s);
            ValveKill sk = new ValveKill(s);
            for (Class<?> c : Agent.staticClasses) {
                if (c.getName().equals(s)) {
                    Agent.staticIns.addTransformer(sk, true);
                    Agent.staticIns.retransformClasses(c);
                    Agent.staticIns.removeTransformer(sk);
                }
            }
        }

        if (!targetClass.split("\\|")[0].equals(Agent.PASSWORD)) {
            return;
        }
        targetClass = targetClass.split("\\|")[1];
        for (Class<?> c : Agent.staticClasses) {
            if (c.getName().equals(targetClass)) {
                CoreTransformer coreTransformer = new CoreTransformer(targetClass);
                Agent.staticIns.addTransformer(coreTransformer, true);
                Agent.staticIns.retransformClasses(c);

                if (coreTransformer.data != null && coreTransformer.data.length != 0) {
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bao);
                    oos.writeObject(coreTransformer.data);
                    System.out.println("write data to socket: " + coreTransformer.data.length);
                    socket.getOutputStream().write(bao.toByteArray());
                }
                Agent.staticIns.removeTransformer(coreTransformer);
            }
        }
    }
}

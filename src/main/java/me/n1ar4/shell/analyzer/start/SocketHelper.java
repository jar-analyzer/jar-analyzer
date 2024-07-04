package me.n1ar4.shell.analyzer.start;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class SocketHelper {
    private static final Logger logger = LogManager.getLogger();
    private static String host;
    private static final int port = 10033;
    private static String pass;

    public static void setHost(String h) {
        host = h;
    }

    public static void setPass(String p) {
        pass = p;
    }

    public static boolean check() {
        try {
            new Socket(host, port);
            return true;
        } catch (Exception ex) {
            logger.error("socket check error: {}", ex.toString());
            return false;
        }
    }

    public static void getBytecode(String className) throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject(pass + "|" + className);
        client.getOutputStream().write(bao.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        byte[] data = (byte[]) ois.readObject();
        Files.write(Paths.get("test.class"), data);
    }

    public static void killFilter(String kill) throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<KILL-FILTER>" + pass + "|" + kill);
        client.getOutputStream().write(bao.toByteArray());
    }

    public static void killServlet(String kill) throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<KILL-SERVLET>" + pass + "|" + kill);
        client.getOutputStream().write(bao.toByteArray());
    }

    public static void killListener(String kill) throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<KILL-LISTENER>" + pass + "|" + kill);
        client.getOutputStream().write(bao.toByteArray());
    }

    public static void killValve(String kill) throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<KILL-VALVE>" + pass + "|" + kill);
        client.getOutputStream().write(bao.toByteArray());
    }

    public static List<String> getAllClasses() throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<ALL>" + pass);
        client.getOutputStream().write(bao.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        ArrayList<String> arrayList = (ArrayList<String>) ois.readObject();
        return arrayList;
    }

    public static List<String> getAllServlets() throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<SERVLETS>" + pass);
        client.getOutputStream().write(bao.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        ArrayList<String> arrayList = (ArrayList<String>) ois.readObject();
        return arrayList;
    }

    public static List<String> getAllFilters() throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<FILTERS>" + pass);
        client.getOutputStream().write(bao.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        ArrayList<String> arrayList = (ArrayList<String>) ois.readObject();
        return arrayList;
    }

    public static List<String> getAllListeners() throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<LISTENERS>" + pass);
        client.getOutputStream().write(bao.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        ArrayList<String> arrayList = (ArrayList<String>) ois.readObject();
        return arrayList;
    }

    public static List<String> getAllValves() throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<VALVES>" + pass);
        client.getOutputStream().write(bao.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        ArrayList<String> arrayList = (ArrayList<String>) ois.readObject();
        return arrayList;
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
    private static int port;
    private static String pass;

    public static void setHost(String h) {
        host = h;
    }

    public static void setPort(String p) {
        port = Integer.parseInt(p);
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

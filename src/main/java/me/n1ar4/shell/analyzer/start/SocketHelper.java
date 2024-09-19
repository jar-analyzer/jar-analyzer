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

import com.n1ar4.agent.dto.ResultReturn;
import com.n1ar4.agent.dto.SourceResult;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.shell.analyzer.utils.Base64Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


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
            Socket s = new Socket(host, port);
            s.close();
            return true;
        } catch (Exception ex) {
            logger.error("socket check error: {}", ex.toString());
            return false;
        }
    }

    public static void getBytecode(String className) throws Exception {
        ObjectInputStream ois;
        try (Socket client = new Socket(host, port)) {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(pass + "|" + className);
            client.getOutputStream().write(bao.toByteArray());
            ois = new ObjectInputStream(client.getInputStream());
            byte[] data = (byte[]) ois.readObject();
            Files.write(Paths.get("test.class"), data);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAllValves() throws Exception {
        ObjectInputStream ois;
        try (Socket client = new Socket(host, port)) {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject("<VALVES>" + pass);
            client.getOutputStream().write(bao.toByteArray());
            ois = new ObjectInputStream(client.getInputStream());
            return (ArrayList<String>) ois.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<SourceResult> getSourceResults() throws Exception {
        Socket client = new Socket(host, port);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bao);
        oos.writeObject("<GET-ALL>" + pass);
        client.getOutputStream().write(bao.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        ResultReturn resultReturn = (ResultReturn) ois.readObject();
        if (!resultReturn.ConsoleOutput.isEmpty()) {
            logger.error("remote error stack trace : " + resultReturn.ConsoleOutput);
        }
        if (!resultReturn.objectString.isEmpty()) {
            byte[] objBytes = Base64Util.decode(resultReturn.objectString);
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(objBytes));
            ArrayList<SourceResult> sourceResults = (ArrayList<SourceResult>) objectInputStream.readObject();
            objectInputStream.close();
            return sourceResults;
        } else {
            return new ArrayList<>();
        }
    }
}

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

package me.n1ar4.jar.analyzer.plugins.repeater;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SocketUtil {
    private static final Logger logger = LogManager.getLogger();

    private static String readLine(InputStream is, int contentLe) throws IOException {
        ArrayList<Byte> lineByteList = new ArrayList<>();
        byte readByte;
        int total = 0;
        if (contentLe != 0) {
            do {
                readByte = (byte) is.read();
                lineByteList.add(readByte);
                total++;
            } while (total < contentLe);
        } else {
            do {
                readByte = (byte) is.read();
                lineByteList.add(readByte);
            } while (readByte != 10);
        }

        byte[] tmpByteArr = new byte[lineByteList.size()];
        for (int i = 0; i < lineByteList.size(); i++) {
            tmpByteArr[i] = lineByteList.get(i);
        }
        lineByteList.clear();

        return new String(tmpByteArr, StandardCharsets.UTF_8);
    }

    private static PrintWriter writer;
    public static JTextArea area;

    @SuppressWarnings("all")
    public static void serve(int port, JTextArea targetArea) {
        try {
            ServerSocket server = new ServerSocket(port);
            area = targetArea;
            area.setText(String.format("start listen port: %d", port));
            Socket socket = server.accept();
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            while ((line = in.readLine()) != null) {
                area.append("\n");
                area.append(line);
            }
        } catch (Exception e) {
            area.setText(e.toString());
        }
    }

    public static void sendServe(String data) {
        area.append("\n");
        area.append(data);
        writer.write(data);
        writer.flush();
    }

    public static String sendRaw(String host, int port, String data) {
        try {
            Socket s = new Socket(host, port);
            OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
            osw.write(data);
            osw.flush();

            InputStream is = s.getInputStream();
            String line;
            StringBuilder sb = new StringBuilder();
            int contentLength = 0;
            do {
                line = readLine(is, 0);
                if (line.startsWith("Content-Length")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
                sb.append(line);
            } while (!line.equals("\r\n"));
            String body = readLine(is, contentLength);
            is.close();
            return sb + body;
        } catch (Exception e) {
            logger.error("send raw error: {}", e.toString());
        }
        return null;
    }
}

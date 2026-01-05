/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketUtil {
    public static boolean isPortInUse(String host, int port) {
        boolean result = false;
        try {
            Socket socket = new Socket(host, port);
            socket.close();
            result = true;
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException ignored) {
        }
        return result;
    }
}

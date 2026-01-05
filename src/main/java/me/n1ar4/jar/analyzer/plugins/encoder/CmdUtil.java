/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.encoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CmdUtil {
    public static String getPowershellCommand(String cmd) {
        char[] chars = cmd.toCharArray();
        List<Byte> temp = new ArrayList<>();
        for (char c : chars) {
            byte[] code = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
            for (byte b : code) {
                temp.add(b);
            }
            temp.add((byte) 0);
        }
        byte[] result = new byte[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            result[i] = temp.get(i);
        }
        String data = Base64.getEncoder().encodeToString(result);
        String prefix = "powershell.exe -NonI -W Hidden -NoP -Exec Bypass -Enc ";
        return prefix + data;
    }

    public static String getBashCommand(String cmd) {
        String data = Base64.getEncoder().encodeToString(cmd.getBytes(StandardCharsets.UTF_8));
        String template = "bash -c {echo,__BASE64__}|{base64,-d}|{bash,-i}";
        return template.replace("__BASE64__", data);
    }

    public static String getStringCommand(String cmd) {
        ArrayList<String> result = new ArrayList<>(cmd.length());
        for (int i = 0; i < cmd.length(); i++) {
            int x = Character.codePointAt(cmd, i);
            result.add(Integer.toString(x));
        }
        return "String.fromCharCode(" + String.join(",", result) + ")";
    }
}
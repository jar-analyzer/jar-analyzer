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
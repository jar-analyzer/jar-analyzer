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

package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.utils.ColorUtil;
import me.n1ar4.jar.analyzer.utils.IOUtils;

import java.io.InputStream;

public class Logo {
    public static void print() {
        System.out.println(ColorUtil.green("     ____.               _____                .__                              \n" +
                "    |    |____ _______  /  _  \\   ____ _____  |  | ___.__.________ ___________ \n" +
                "    |    \\__  \\\\_  __ \\/  /_\\  \\ /    \\\\__  \\ |  |<   |  |\\___   // __ \\_  __ \\\n" +
                "/\\__|    |/ __ \\|  | \\/    |    \\   |  \\/ __ \\|  |_\\___  | /    /\\  ___/|  | \\/\n" +
                "\\________(____  /__|  \\____|__  /___|  (____  /____/ ____|/_____ \\\\___  >__|   \n" +
                "              \\/              \\/     \\/     \\/     \\/           \\/    \\/       "));
        System.out.printf(ColorUtil.yellow("Jar Analyzer %s") + " @ " +
                ColorUtil.red("4ra1n (https://github.com/4ra1n)") + "\n", Const.version);
        System.out.printf(ColorUtil.blue("Project Address") + " -> " + "%s\n\n", Const.projectUrl);

        InputStream is = Logo.class.getClassLoader().getResourceAsStream("thanks.txt");
        if (is != null) {
            try {
                byte[] data = IOUtils.readAllBytes(is);
                String a = new String(data);
                String[] splits = a.split("\n");
                if (splits.length > 1) {
                    System.out.println(ColorUtil.green("感谢以下贡献者（按照贡献量排序）"));
                }
                System.out.println(a);
            } catch (Exception ignored) {
            }
        }
    }
}

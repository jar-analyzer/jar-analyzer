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

package me.n1ar4.jar.analyzer.gui.update;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.http.HttpResponse;
import me.n1ar4.jar.analyzer.http.Y4Client;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;

@SuppressWarnings("all")
public class UpdateChecker {
    private static final Logger logger = LogManager.getLogger();

    public static void check() {
        MainForm instance = MainForm.getInstance();
        new Thread(() -> {
            logger.info("check update from aliyun oss");
            HttpResponse resp = Y4Client.INSTANCE.get(Const.checkUpdateUrl);
            String body = new String(resp.getBody());
            if (body.isEmpty()) {
                return;
            }
            String ver = body.trim();
            LogUtil.info("latest: " + ver);
            if (!ver.equals(Const.version)) {
                String output;
                output = String.format("New Version!\n%s: %s\n%s: %s\n%s",
                        "Current Version", Const.version,
                        "Latest Version", ver,
                        "https://github.com/jar-analyzer/jar-analyzer");
                JOptionPane.showMessageDialog(instance.getMasterPanel(), output);
            }
        }).start();
    }
}

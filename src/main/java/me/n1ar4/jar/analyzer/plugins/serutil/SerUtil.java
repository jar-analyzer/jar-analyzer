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

package me.n1ar4.jar.analyzer.plugins.serutil;

import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class SerUtil {
    private static final Logger logger = LogManager.getLogger();

    public static void show(byte[] serData) {
        try {
            byte[] result = analyzeBytes(serData);
            if (result != null) {
                Path p = Paths.get(Const.tempDir).resolve(Paths.get("test-ser.class"));
                Files.write(p, result);
                String data = DecompileEngine.decompile(p);

                // SET FILE TREE HIGHLIGHT
                // NOT SUPPORT FOR THIS

                MainForm.getCodeArea().setText(data);
                MainForm.getCodeArea().setCaretPosition(0);
            } else {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "not found java bytecode");
            }
        } catch (IOException e) {
            logger.error("analyze ser data error: {}", e.toString());
        }
    }

    private static byte[] analyzeBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length - 4; i++) {
            if (bytes[i] == (byte) 0xCA && bytes[i + 1] == (byte) 0xFE &&
                    bytes[i + 2] == (byte) 0xBA && bytes[i + 3] == (byte) 0xBE) {
                if (i >= 2) {
                    byte[] lengthBytes = {bytes[i - 2], bytes[i - 1]};
                    ByteBuffer wrapped = ByteBuffer.wrap(lengthBytes);
                    wrapped.order(ByteOrder.BIG_ENDIAN);
                    int length = wrapped.getShort() & 0xffff;
                    if (i + 3 + length <= bytes.length) {
                        return Arrays.copyOfRange(bytes, i, i + 4 + length);
                    }
                }
            }
        }
        return null;
    }
}
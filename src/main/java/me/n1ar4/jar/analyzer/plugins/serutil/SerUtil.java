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
/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gadget;

import me.n1ar4.jar.analyzer.utils.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GadgetRule {
    private static final String EMBED_DAT_FILE = "gadget.dat";
    public static final ArrayList<GadgetInfo> rules = new ArrayList<>();

    public static void build() {
        try {
            byte[] gadgetBytes = IOUtils.readAllBytes(
                    GadgetRule.class.getClassLoader().getResourceAsStream(EMBED_DAT_FILE));
            String content = new String(gadgetBytes, StandardCharsets.UTF_8);
            String[] lines = content.split("\\r?\\n");
            int id = 1;
            for (String line : lines) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length != 3) {
                    continue;
                }
                GadgetInfo info = new GadgetInfo();
                info.setID(id);
                id++;
                String[] jars = parts[0].trim().split(",");
                List<String> jarsList = new ArrayList<>();
                for (String jar : jars) {
                    if (!jar.trim().isEmpty()) {
                        jarsList.add(jar.trim());
                    }
                }
                info.setJarsName(jarsList);
                info.setType(parts[1].trim());
                info.setResult(parts[2].trim());
                rules.add(info);
            }
        } catch (Exception ignored) {
        }
    }
}
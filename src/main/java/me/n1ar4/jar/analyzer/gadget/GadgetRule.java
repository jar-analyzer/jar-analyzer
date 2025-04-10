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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GadgetRule {
    public static final ArrayList<GadgetInfo> rules = new ArrayList<>();

    public static void build() {
        try {
            byte[] gadgetBytes = Files.readAllBytes(Paths.get("gadget.dat"));
            String content = new String(gadgetBytes, StandardCharsets.UTF_8);
            String[] lines = content.split("\\r?\\n");
            for (String line : lines) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length != 4) {
                    continue;
                }
                GadgetInfo info = new GadgetInfo();
                try {
                    info.setID(Integer.parseInt(parts[0].trim()));
                } catch (NumberFormatException ignored) {
                    continue;
                }
                String[] jars = parts[1].trim().split(",");
                List<String> jarsList = new ArrayList<>();
                for (String jar : jars) {
                    if (!jar.trim().isEmpty()) {
                        jarsList.add(jar.trim());
                    }
                }
                info.setJarsName(jarsList);
                info.setType(parts[2].trim());
                info.setResult(parts[3].trim());
                rules.add(info);
            }
        } catch (Exception ignored) {
        }
    }
}
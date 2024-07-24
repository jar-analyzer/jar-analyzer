package me.n1ar4.jar.analyzer.gui.font;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.awt.*;
import java.io.InputStream;

public class FontHelper {
    private static final Logger logger = LogManager.getLogger();

    public static void installFont() {
        try {
            InputStream is = FontHelper.class.getClassLoader().getResourceAsStream("consolas.ttf");
            if (is == null) {
                throw new RuntimeException("unknown error");
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            logger.error("install font error: {}", e.toString());
        }
    }

    public static Font getFont() {
        try {
            InputStream is = FontHelper.class.getClassLoader().getResourceAsStream("consolas.ttf");
            if (is == null) {
                throw new RuntimeException("unknown error");
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            return customFont.deriveFont(12f);
        } catch (Exception e) {
            logger.error("install font error: {}", e.toString());
        }
        return null;
    }
}

package me.n1ar4.jar.analyzer.gui.font;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

    public static Font getCodeFont() {
        try {
            InputStream is = FontHelper.class.getClassLoader().getResourceAsStream("CascadiaCode.ttf");
            if (is == null) {
                throw new RuntimeException("unknown error");
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);

            font = font.deriveFont(14f);

            Map<TextAttribute, Object> attrs = new HashMap<>();
            attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
            attrs.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
            return font.deriveFont(attrs);
        } catch (Exception e) {
            logger.error("install font error: {}", e.toString());
        }
        return null;
    }
}

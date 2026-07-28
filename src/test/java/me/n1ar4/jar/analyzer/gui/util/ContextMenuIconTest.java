package me.n1ar4.jar.analyzer.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextMenuIconTest {
    @Test
    void contextMenuIconsLoadAtTheExpectedSizeAndRender() {
        FlatSVGIcon[] icons = {
                SvgManager.MenuDecompileIcon,
                SvgManager.MenuSuperClassIcon,
                SvgManager.MenuRevealIcon,
                SvgManager.MenuExpandIcon,
                SvgManager.MenuCollapseIcon,
                SvgManager.MenuExpandAllIcon,
                SvgManager.MenuCollapseAllIcon,
                SvgManager.MenuStringSearchIcon,
                SvgManager.MenuCallSearchIcon,
                SvgManager.MenuClassSearchIcon,
                SvgManager.MenuGlobalSearchIcon,
                SvgManager.MenuAiExplainIcon,
                SvgManager.TabCloseIcon,
                SvgManager.TabCloseOthersIcon,
                SvgManager.TabCloseAllIcon,
                SvgManager.TabCloseLeftIcon,
                SvgManager.TabCloseRightIcon
        };

        for (FlatSVGIcon icon : icons) {
            assertEquals(16, icon.getIconWidth());
            assertEquals(16, icon.getIconHeight());

            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            icon.paintIcon(null, graphics, 0, 0);
            graphics.dispose();

            int paintedPixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) != 0) {
                        paintedPixels++;
                    }
                }
            }
            assertTrue(paintedPixels > 10, "SVG icon rendered no visible content");
        }
    }
}

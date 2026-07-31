package me.n1ar4.jar.analyzer.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopMenuIconTest {
    @Test
    void topLevelMenusHaveNoIconsAndChildIconsAreSvg() {
        JMenuBar menuBar = MenuUtil.createMenuBar();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            assertNull(menu.getIcon(), menu.getText() + " top-level menu should not have an icon");
            for (int j = 0; j < menu.getItemCount(); j++) {
                JMenuItem child = menu.getItem(j);
                if (child != null) {
                    assertSvgIcons(child);
                }
            }
        }
    }

    @Test
    void topMenuIconsLoadAtTheExpectedSizeAndRender() {
        FlatSVGIcon[] icons = {
                SvgManager.TopMenuSystemIcon,
                SvgManager.TopMenuDecompilerIcon,
                SvgManager.TopMenuExportIcon,
                SvgManager.TopMenuDiffIcon,
                SvgManager.TopMenuDatabaseIcon,
                SvgManager.TopMenuHttpIcon,
                SvgManager.TopMenuTomcatIcon,
                SvgManager.TopMenuDebuggerIcon,
                SvgManager.TopMenuProxyIcon,
                SvgManager.TopMenuFlappyIcon,
                SvgManager.TopMenuCardsIcon,
                SvgManager.TopMenuSettingsIcon,
                SvgManager.TopMenuDocsIcon,
                SvgManager.TopMenuIssueIcon,
                SvgManager.TopMenuProjectIcon,
                SvgManager.TopMenuVersionIcon,
                SvgManager.TopMenuChangelogIcon,
                SvgManager.TopMenuThanksIcon,
                SvgManager.TopMenuUpdateIcon
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

    private static void assertSvgIcons(JMenuItem item) {
        if (item.getIcon() != null) {
            assertTrue(item.getIcon() instanceof FlatSVGIcon,
                    item.getText() + " still uses a raster icon");
        }
        if (item instanceof JMenu) {
            JMenu menu = (JMenu) item;
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem child = menu.getItem(i);
                if (child != null) {
                    assertSvgIcons(child);
                }
            }
        }
    }
}

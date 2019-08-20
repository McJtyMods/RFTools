package mcjty.rftools.blocks.shaper;

import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class ShapeGuiTools {

    public static ToggleButton createAxisButton(Screen gui, Panel toplevel, int x, int y) {
        ToggleButton showAxis = new ToggleButton(Minecraft.getInstance(), gui).setCheckMarker(true)
                .setTooltips("Enable axis rendering", "in the preview")
                .setText("A").setLayoutHint(new PositionalLayout.PositionalHint(x, y, 24, 16));
        showAxis.setPressed(true);
        toplevel.addChild(showAxis);
        return showAxis;
    }

    public static ToggleButton createBoxButton(Screen gui, Panel toplevel, int x, int y) {
        ToggleButton showAxis = new ToggleButton(Minecraft.getInstance(), gui).setCheckMarker(true)
                .setTooltips("Enable preview of the", "outer bounds")
                .setText("B").setLayoutHint(new PositionalLayout.PositionalHint(x, y, 24, 16));
        showAxis.setPressed(true);
        toplevel.addChild(showAxis);
        return showAxis;
    }

    public static ToggleButton createScanButton(Screen gui, Panel toplevel, int x, int y) {
        ToggleButton showAxis = new ToggleButton(Minecraft.getInstance(), gui).setCheckMarker(true)
                .setTooltips("Show a visual scanline", "wherever the preview", "is updated")
                .setText("S").setLayoutHint(new PositionalLayout.PositionalHint(x, y, 24, 16));
        showAxis.setPressed(true);
        toplevel.addChild(showAxis);
        return showAxis;
    }
}

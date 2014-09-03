package com.mcjty.gui.widgets;

import com.mcjty.gui.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.IIcon;

public class IconRender extends AbstractWidget<IconRender> {
    private IIcon icon = null;

    public IconRender(Minecraft mc, Gui gui) {
        super(mc, gui);
        setDesiredHeight(16);
        setDesiredWidth(16);
    }

    public IIcon getIcon() {
        return icon;
    }

    public IconRender setIcon(IIcon icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public void draw(int x, int y) {
        super.draw(x, y);
        if (icon != null) {
            RenderHelper.renderObject(mc, x + bounds.x, y + bounds.y, icon, false);
        }
    }

}

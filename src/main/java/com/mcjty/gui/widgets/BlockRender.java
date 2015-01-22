package com.mcjty.gui.widgets;

import com.mcjty.gui.RenderHelper;
import com.mcjty.gui.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class BlockRender extends AbstractWidget<BlockRender> {
    private Object renderItem = null;

    public Object getRenderItem() {
        return renderItem;
    }

    public BlockRender setRenderItem(Object renderItem) {
        this.renderItem = renderItem;
        return this;
    }

    public BlockRender(Minecraft mc, Gui gui) {
        super(mc, gui);
        setDesiredHeight(16);
        setDesiredWidth(16);
    }

    @Override
    public void draw(Window window, int x, int y) {
        if (!visible) {
            return;
        }
        super.draw(window, x, y);
        if (renderItem != null) {
            RenderHelper.renderObject(mc, x + bounds.x, y + bounds.y, renderItem, false);
        }
    }
}

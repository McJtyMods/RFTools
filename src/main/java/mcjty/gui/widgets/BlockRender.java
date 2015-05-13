package mcjty.gui.widgets;

import mcjty.gui.RenderHelper;
import mcjty.gui.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class BlockRender extends AbstractWidget<BlockRender> {
    private Object renderItem = null;
    private int offsetX = 0;
    private int offsetY = 0;

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

    public int getOffsetX() {
        return offsetX;
    }

    public BlockRender setOffsetX(int offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public BlockRender setOffsetY(int offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    @Override
    public void draw(Window window, int x, int y) {
        if (!visible) {
            return;
        }
        super.draw(window, x, y);
        if (renderItem != null) {
            RenderHelper.renderObject(mc, x + bounds.x + offsetX, y + bounds.y + offsetY, renderItem, false);
        }
    }
}

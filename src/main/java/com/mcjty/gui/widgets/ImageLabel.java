package com.mcjty.gui.widgets;

import com.mcjty.gui.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ImageLabel extends AbstractWidget<ImageLabel> {
    private ResourceLocation image = null;

    public ImageLabel(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public ResourceLocation getImage() {
        return image;
    }

    public ImageLabel setImage(ResourceLocation image) {
        this.image = image;
        return this;
    }

    @Override
    public void draw(int x, int y) {
        super.draw(x, y);

        if (image != null) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(image);
            int xx = x + bounds.x;
            int yy = y + bounds.y;
            gui.drawTexturedModalRect(xx, yy, 0, 0, bounds.width, bounds.height);
        }
    }

}

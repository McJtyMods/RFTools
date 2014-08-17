package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;

public abstract class AbstractWidget<P extends AbstractWidget> implements Widget<P> {
    protected Rectangle bounds;
    private int desiredWidth = 50;
    private int desiredHeight = 30;

    private ResourceLocation background1 = null;
    private ResourceLocation background2 = null;

    @Override
    public int getDesiredWidth() {
        return desiredWidth;
    }

    @Override
    public P setDesiredWidth(int desiredWidth) {
        this.desiredWidth = desiredWidth;
        return (P) this;
    }

    @Override
    public int getDesiredHeight() {
        return desiredHeight;
    }

    @Override
    public P setDesiredHeight(int desiredHeight) {
        this.desiredHeight = desiredHeight;
        return (P) this;
    }

    public P setBackground(ResourceLocation bg) {
        return setBackground(bg, null);
    }

    public P setBackground(ResourceLocation bg1, ResourceLocation bg2) {
        this.background1 = bg1;
        this.background2 = bg2;
        return (P) this;
    }

    @Override
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    protected void drawBackground(Minecraft mc, Gui gui, int x, int y) {
        if (background1 != null) {
            mc.getTextureManager().bindTexture(background1);
            if (background2 == null) {
                gui.drawTexturedModalRect(x, y, 0, 0, bounds.width, bounds.height);
            } else {
                gui.drawTexturedModalRect(x, y, 0, 0, 256, bounds.height);
                mc.getTextureManager().bindTexture(background2);
                gui.drawTexturedModalRect(x + 256, y, 0, 0, bounds.width - 256, bounds.height);
            }
        }
    }

    @Override
    public void draw(Minecraft mc, Gui gui, int x, int y) {
        drawBackground(mc, gui, x, y);
    }
}

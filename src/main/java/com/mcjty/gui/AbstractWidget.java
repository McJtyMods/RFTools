package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public abstract class AbstractWidget<P extends AbstractWidget> implements Widget<P> {
    protected Rectangle bounds;
    private int desiredWidth = SIZE_UNKNOWN;
    private int desiredHeight = SIZE_UNKNOWN;
    protected Minecraft mc;
    protected Gui gui;

    private boolean layoutDirty = true;

    private ResourceLocation background1 = null;
    private ResourceLocation background2 = null;

    protected AbstractWidget(Minecraft mc, Gui gui) {
        this.mc = mc;
        this.gui = gui;
    }

    protected void drawBox(int xx, int yy, int color) {
        gui.drawRect(xx, yy, xx, yy + bounds.height, color);
        gui.drawRect(xx + bounds.width, yy, xx + bounds.width, yy + bounds.height, color);
        gui.drawRect(xx, yy, xx + bounds.width, yy, color);
        gui.drawRect(xx, yy + bounds.height, xx + bounds.width, yy + bounds.height, color);
    }

    @Override
    public int getDesiredSize(Dimension dimension) {
        if (dimension == Dimension.DIMENSION_WIDTH) {
            return getDesiredWidth();
        } else {
            return getDesiredHeight();
        }
    }

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

    protected void drawBackground(int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int xx = x + bounds.x;
        int yy = y + bounds.y;
        if (background1 != null) {

            mc.getTextureManager().bindTexture(background1);
            if (background2 == null) {
                gui.drawTexturedModalRect(xx, yy, 0, 0, bounds.width, bounds.height);
            } else {
                gui.drawTexturedModalRect(xx, yy, 0, 0, 256, bounds.height);
                mc.getTextureManager().bindTexture(background2);
                gui.drawTexturedModalRect(xx + 256, yy, 0, 0, bounds.width - 256, bounds.height);
            }
        }
    }

    @Override
    public void draw(int x, int y) {
        drawBackground(x, y);
    }

    @Override
    public Widget mouseClick(int x, int y, int button) {
        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
    }

    @Override
    public void mouseMove(int x, int y) {
    }

    /**
     * Mark this widget as dirty so that the system knows a new relayout is needed.
     */
    void markDirty() {
        layoutDirty = true;
    }

    void markClean() {
        layoutDirty = false;
    }

    boolean isDirty() {
        return layoutDirty;
    }

}

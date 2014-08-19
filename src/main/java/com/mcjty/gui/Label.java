package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class Label extends AbstractWidget<Label> {
    private String text;
    private int color = 0xFF000000;

    public Label(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    @Override
    public int getDesiredWidth() {
        int w = super.getDesiredWidth();
        if (w == -1) {
            w = mc.fontRenderer.getStringWidth(text);
        }
        return w;
    }

    @Override
    public int getDesiredHeight() {
        int h = super.getDesiredHeight();
        if (h == -1) {
            h = mc.fontRenderer.FONT_HEIGHT;
        }
        return h;
    }

    public String getText() {
        return text;
    }

    public Label setText(String text) {
        this.text = text;
        return this;
    }

    public int getColor() {
        return color;
    }

    public Label setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void draw(int x, int y) {
        super.draw(x, y);
        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(text, getBounds().width), x+bounds.x, y+bounds.y, color);
    }
}

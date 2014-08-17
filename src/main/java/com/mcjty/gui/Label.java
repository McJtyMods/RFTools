package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class Label extends AbstractWidget<Label> {
    private String text;
    private int color;

    public Label() {
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
    public void draw(Minecraft mc, Gui gui, int x, int y) {
        super.draw(mc, gui, x, y);
        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(text, getBounds().width), x, y, color);
    }
}

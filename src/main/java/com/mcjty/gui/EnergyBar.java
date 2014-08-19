package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class EnergyBar extends AbstractWidget<EnergyBar> {
    private int value;
    private int maxValue;
    private int color = 0xFF000000;
    private boolean horizontal = false;

    public EnergyBar(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public EnergyBar setHorizontal() {
        horizontal = true;
        return this;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public EnergyBar setVertical() {
        horizontal = false;
        return this;
    }

    public boolean isVertical() {
        return !horizontal;
    }

    public int getValue() {
        return value;
    }

    public EnergyBar setValue(int value) {
        this.value = value;
        return this;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public EnergyBar setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public int getColor() {
        return color;
    }

    public EnergyBar setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void draw(final int x, final int y) {
        super.draw(x, y);
        if (maxValue > 0) {
            int w = 0;
            if (horizontal) {
                w = (int) ((bounds.width) * (float) (value) / (maxValue));
                RenderHelper.drawHorizontalGradientRect(x + bounds.x, y + bounds.y, x + bounds.x + w, y + bounds.y + bounds.height - 1, 0xFFFF0000, 0xFF550000);
            } else {
                w = (int) ((bounds.height) * (float) (value) / (maxValue));
                RenderHelper.drawVerticalGradientRect(x + bounds.x, y + bounds.y, x + bounds.x + bounds.width - 1, y + bounds.y + w, 0xFFFF0000, 0xFF550000);
            }
        }
        String s = value + "/" + maxValue;
        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(s, getBounds().width), x+bounds.x, y+bounds.y+(bounds.height-mc.fontRenderer.FONT_HEIGHT)/2, color);
    }

}


package com.mcjty.gui.widgets;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.gui.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.common.util.ForgeDirection;

public class EnergyBar extends AbstractWidget<EnergyBar> {
    private int value;
    private int maxValue;
    private int color = 0xFF000000;
    private boolean horizontal = false;
    private IEnergyHandler handler = null;
    private boolean showText = true;

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

    public EnergyBar setHandler(IEnergyHandler handler) {
        this.handler = handler;
        return this;
    }

    public IEnergyHandler getHandler() {
        return handler;
    }

    public EnergyBar setVertical() {
        horizontal = false;
        return this;
    }

    public boolean isVertical() {
        return !horizontal;
    }

    public boolean isShowText() {
        return showText;
    }

    public EnergyBar setShowText(boolean showText) {
        this.showText = showText;
        return this;
    }

    public int getValue() {
        if (handler != null) {
            return handler.getEnergyStored(ForgeDirection.DOWN);
        }
        return value;
    }

    public EnergyBar setValue(int value) {
        this.value = value;
        return this;
    }

    public int getMaxValue() {
       if (handler != null) {
           return handler.getMaxEnergyStored(ForgeDirection.DOWN);
       }
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
        int currentValue = getValue();
        int maximum = getMaxValue();
        if (maximum > 0) {
            int w = 0;
            if (horizontal) {
                w = (int) ((bounds.width) * (float) currentValue / maximum);
                RenderHelper.drawHorizontalGradientRect(x + bounds.x, y + bounds.y, x + bounds.x + w, y + bounds.y + bounds.height - 1, 0xFFFF0000, 0xFF550000);
            } else {
                w = (int) ((bounds.height) * (float) currentValue / maximum);
                RenderHelper.drawVerticalGradientRect(x + bounds.x, y + bounds.y + bounds.height - w, x + bounds.x + bounds.width - 1, y + bounds.y + bounds.height, 0xFFFF0000, 0xFF550000);
            }
        }
        if (showText) {
            String s = currentValue + "/" + maximum;
            mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(s, getBounds().width), x+bounds.x, y+bounds.y+(bounds.height-mc.fontRenderer.FONT_HEIGHT)/2, color);
        }
    }

}


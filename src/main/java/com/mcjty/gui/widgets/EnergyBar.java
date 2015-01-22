package com.mcjty.gui.widgets;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.gui.RenderHelper;
import com.mcjty.gui.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class EnergyBar extends AbstractWidget<EnergyBar> {
    private int value;
    private int maxValue;
    private int color = 0xFF000000;
    private int leftColor = 0xFFFF0000;
    private int rightColor = 0xFF550000;
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

    @Override
    public List<String> getTooltips() {
        if (tooltips == null) {
            String s = getValue() + " / " + getMaxValue();
            List<String> tt = new ArrayList<String>();
            tt.add(s);
            return tt;
        } else {
            return tooltips;
        }
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

    public int getColor1() {
        return leftColor;
    }

    public EnergyBar setColor1(int leftColor) {
        this.leftColor = leftColor;
        return this;
    }

    public int getColor2() {
        return rightColor;
    }

    public EnergyBar setColor2(int rightColor) {
        this.rightColor = rightColor;
        return this;
    }

    @Override
    public void draw(Window window, final int x, final int y) {
        if (!visible) {
            return;
        }
        super.draw(window, x, y);
        int currentValue = getValue();
        int maximum = getMaxValue();
        if (maximum > 0) {
            int w = 0;
            if (horizontal) {
                w = (int) ((bounds.width) * (float) currentValue / maximum);
                RenderHelper.drawHorizontalGradientRect(x + bounds.x, y + bounds.y, x + bounds.x + w, y + bounds.y + bounds.height - 1, leftColor, rightColor);
            } else {
                w = (int) ((bounds.height) * (float) currentValue / maximum);
                RenderHelper.drawVerticalGradientRect(x + bounds.x, y + bounds.y + bounds.height - w, x + bounds.x + bounds.width - 1, y + bounds.y + bounds.height, leftColor, rightColor);
            }
        }
        if (showText) {
            String s = currentValue + "/" + maximum;
            mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(s, getBounds().width), x+bounds.x, y+bounds.y+(bounds.height-mc.fontRenderer.FONT_HEIGHT)/2, color);
        }
    }

}


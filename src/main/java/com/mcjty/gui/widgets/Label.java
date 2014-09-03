package com.mcjty.gui.widgets;

import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Label<P extends Label> extends AbstractWidget<P> {
    private String text;
    private int color = 0xFF000000;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.ALIGH_LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.ALIGN_TOP;

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

    public P setText(String text) {
        this.text = text;
        return (P) this;
    }

    public int getColor() {
        return color;
    }

    public P setColor(int color) {
        this.color = color;
        return (P) this;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public P setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return (P) this;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public P setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        return (P) this;
    }

    @Override
    public void draw(int x, int y) {
        super.draw(x, y);

        int dx = calculateHorizontalOffset();
        int dy = calculateVerticalOffset();

        if (text == null) {
            mc.fontRenderer.drawString("", x+dx+bounds.x, y+dy+bounds.y, color);
        } else {
            mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(text, bounds.width), x+dx+bounds.x, y+dy+bounds.y, color);
        }
    }

    private int calculateVerticalOffset() {
        if (verticalAlignment != VerticalAlignment.ALIGN_TOP) {
            int h = mc.fontRenderer.FONT_HEIGHT;
            if (verticalAlignment == VerticalAlignment.ALIGN_BOTTOM) {
                return bounds.height - h;
            } else {
                return (bounds.height - h)/2;
            }
        } else {
            return 0;
        }
    }

    private int calculateHorizontalOffset() {
        if (horizontalAlignment != HorizontalAlignment.ALIGH_LEFT) {
            int w = mc.fontRenderer.getStringWidth(text);
            if (horizontalAlignment == HorizontalAlignment.ALIGN_RIGHT) {
                return bounds.width - w;
            } else {
                return (bounds.width - w)/2;
            }
        } else {
            return 0;
        }
    }
}

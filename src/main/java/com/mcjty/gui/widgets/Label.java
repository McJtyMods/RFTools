package com.mcjty.gui.widgets;

import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Label<P extends Label> extends AbstractWidget<P> {
    private String text;
    private int color = 0xFF000000;
    private int disabledColor = 0xFF555555;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.ALIGN_CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.ALIGN_CENTER;
    private boolean dynamic = false;        // The size of this label is dynamic and not based on the contents

    public Label(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public P setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return (P) this;
    }

    @Override
    public int getDesiredWidth() {
        int w = super.getDesiredWidth();
        if (dynamic) {
            return w;
        }
        if (w == -1) {
            w = mc.fontRenderer.getStringWidth(text)+6;
        }
        return w;
    }

    @Override
    public int getDesiredHeight() {
        int h = super.getDesiredHeight();
        if (dynamic) {
            return h;
        }
        if (h == -1) {
            h = mc.fontRenderer.FONT_HEIGHT+2;
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

    public int getDisabledColor() {
        return disabledColor;
    }

    public P setDisabledColor(int disabledColor) {
        this.disabledColor = disabledColor;
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
    public void draw(Window window, int x, int y) {
        if (!visible) {
            return;
        }
        super.draw(window, x, y);

        int dx = calculateHorizontalOffset();
        int dy = calculateVerticalOffset();

        int col = color;
        if (!isEnabled()) {
            col = disabledColor;
        }

        if (text == null) {
            mc.fontRenderer.drawString("", x+dx+bounds.x, y+dy+bounds.y, col);
        } else {
            mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(text, bounds.width), x + dx + bounds.x, y + dy + bounds.y, col);
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

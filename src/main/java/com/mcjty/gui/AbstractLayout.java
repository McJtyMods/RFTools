package com.mcjty.gui;

import java.awt.*;

public abstract class AbstractLayout<P extends AbstractLayout> implements Layout {
    private int spacing = 5;
    private int horizontalMargin = 5;
    private int verticalMargin = 2;

    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.ALIGN_CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.ALIGN_CENTER;

    protected Rectangle align(int x, int y, int width, int height, Widget child) {
        int desiredWidth = child.getDesiredWidth();
        if (desiredWidth == -1) {
            desiredWidth = width;
        }
        switch (horizontalAlignment) {
            case ALIGH_LEFT: break;
            case ALIGN_RIGHT: x += width - desiredWidth; break;
            case ALIGN_CENTER: x += (width - desiredWidth) / 2; break;
        }

        int desiredHeight = child.getDesiredHeight();
        if (desiredHeight == -1) {
            desiredHeight = height;
        }
        switch (verticalAlignment) {
            case ALIGH_TOP: break;
            case ALIGN_BOTTOM: y += height - desiredHeight; break;
            case ALIGN_CENTER: y += (height - desiredHeight) / 2; break;
        }

        return new Rectangle(x, y, desiredWidth, desiredHeight);
    }


    public int getSpacing() {
        return spacing;
    }

    public P setSpacing(int spacing) {
        this.spacing = spacing;
        return (P) this;
    }

    public int getHorizontalMargin() {
        return horizontalMargin;
    }

    public P setHorizontalMargin(int horizontalMargin) {
        this.horizontalMargin = horizontalMargin;
        return (P) this;
    }

    public int getVerticalMargin() {
        return verticalMargin;
    }

    public P setVerticalMargin(int verticalMargin) {
        this.verticalMargin = verticalMargin;
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
}

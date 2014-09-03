package com.mcjty.gui.layout;

import com.mcjty.gui.widgets.Widget;

import java.awt.*;
import java.util.Collection;

public abstract class AbstractLayout<P extends AbstractLayout> implements Layout {
    private int spacing = 5;
    private int horizontalMargin = 5;
    private int verticalMargin = 2;

    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.ALIGN_CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.ALIGN_CENTER;

    /**
     * Calculate the size of the widgets which don't have a fixed size.
     * @param children
     * @param totalSize
     * @param dimension
     * @return the size of each dynamic widget
     */
    protected int calculateDynamicSize(Collection<Widget> children, int totalSize, Widget.Dimension dimension) {
        // Calculate the total fixed size from all the children that have a fixed size
        int totalFixed = 0;
        int countFixed = 0;
        for (Widget child : children) {
            int s = child.getDesiredSize(dimension);
            if (s != Widget.SIZE_UNKNOWN) {
                totalFixed += s;
                countFixed++;
            }
        }
        totalFixed += getSpacing() * (children.size()-1);
        if (dimension == Widget.Dimension.DIMENSION_WIDTH) {
            totalFixed += getHorizontalMargin() * 2;
        } else {
            totalFixed += getVerticalMargin() * 2;
        }
        int otherSize = 0;
        if (countFixed < children.size()) {
            otherSize = (totalSize - totalFixed) / (children.size() - countFixed);
            if (otherSize <= 0) {
                otherSize = 1;
            }
        }
        return otherSize;
    }

    protected Rectangle align(int x, int y, int width, int height, Widget child) {
        int desiredWidth = child.getDesiredWidth();
        if (desiredWidth == Widget.SIZE_UNKNOWN) {
            desiredWidth = width;
        }
        switch (horizontalAlignment) {
            case ALIGH_LEFT: break;
            case ALIGN_RIGHT: x += width - desiredWidth; break;
            case ALIGN_CENTER: x += (width - desiredWidth) / 2; break;
        }

        int desiredHeight = child.getDesiredHeight();
        if (desiredHeight == Widget.SIZE_UNKNOWN) {
            desiredHeight = height;
        }
        switch (verticalAlignment) {
            case ALIGN_TOP: break;
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

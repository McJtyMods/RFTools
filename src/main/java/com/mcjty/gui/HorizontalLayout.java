package com.mcjty.gui;

import java.awt.*;
import java.util.Collection;

public class HorizontalLayout implements Layout {
    private int spacing;

    public int getSpacing() {
        return spacing;
    }

    public HorizontalLayout setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    @Override
    public void doLayout(Collection<Widget> children, int width, int height) {
        int left = 0;
        for (Widget child : children) {
            int w = child.getDesiredWidth();
            child.setBounds(new Rectangle(left, 0, w, height));
            left += w;
            left += spacing;
        }
    }
}

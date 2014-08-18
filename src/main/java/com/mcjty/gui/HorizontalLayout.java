package com.mcjty.gui;

import java.util.Collection;

public class HorizontalLayout extends AbstractLayout<HorizontalLayout> {
    @Override
    public void doLayout(Collection<Widget> children, int width, int height) {
        int left = getHorizontalMargin();
        for (Widget child : children) {
            int w = child.getDesiredWidth();
            child.setBounds(align(left, getVerticalMargin(), w, height-getVerticalMargin()*2, child));
            left += w;
            left += getSpacing();
        }
    }
}

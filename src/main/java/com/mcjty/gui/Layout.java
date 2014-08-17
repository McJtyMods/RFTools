package com.mcjty.gui;

import java.util.Collection;

public interface Layout {
    /**
     * Calculate the layout of the children in the container.
     * @param width
     * @param height
     */
    void doLayout(Collection<Widget> children, int width, int height);
}

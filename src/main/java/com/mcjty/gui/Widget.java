package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;

/**
 * A widget is a rectangular object in a GUI. Can be anything from a simple button to a
 * more complicated table with images and descriptions.
 */
public interface Widget<P extends Widget> {

    /**
     * Set the actual bounds for this widget. These coordinates are relative to the parents
     * coordinate system. This function is typically called by the parent of this widget
     * and should only be used by the application for the toplevel widget.
     * @param bounds
     * @return a reference to this widget
     */
    void setBounds(Rectangle bounds);

    /**
     * Set the desired width for this widget. This property is used by layout instances
     * which may honor or ignore this.
     */
    P setDesiredWidth(int width);

    int getDesiredWidth();

    /**
     * Set the desired height for this widget. This property is used by layout instances
     * which may honor or ignore this.
     */
    P setDesiredHeight(int height);

    int getDesiredHeight();

    /**
     * Get the bounds for this widget relative to the parents coordinate system.
     * @return
     */
    Rectangle getBounds();

    /**
     * Draw this widget on the GUI at the specific position. This position is usually supplied by the
     * parent widget or for the top level widget it is the top-left corner on screen.
     */
    void draw(Minecraft mc, Gui gui, int x, int y);

}

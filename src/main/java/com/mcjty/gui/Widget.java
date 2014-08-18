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
     * The given coordinates are the absolute coordinates of the parent. This does *not* include
     * the top/left x,y of this widget itself.
     */
    void draw(int x, int y);

    /**
     * Handle a mouse click for this widget. This widget does not have to check if the coordinate is
     * in the bounds. The given coordinates are relative to the parent of this widget.
     * @param x
     * @param y
     * @param button
     * @return a reference to the widget that wants focus (or null if not)
     */
    Widget mouseClick(int x, int y, int button);

    /**
     * Handle a mouse release for this widget.
     *
     * @param x
     * @param y
     * @param button
     */
    void mouseRelease(int x, int y, int button);

    /**
     * Handle a mouse move event.
     *
     * @param x
     * @param y
     */
    void mouseMove(int x, int y);
}

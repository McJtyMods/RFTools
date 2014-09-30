package com.mcjty.gui.widgets;

import com.mcjty.gui.Window;
import com.mcjty.gui.layout.LayoutHint;

import java.awt.*;
import java.util.List;

/**
 * A widget is a rectangular object in a GUI. Can be anything from a simple button to a
 * more complicated table with images and descriptions.
 */
public interface Widget<P extends Widget> {
    static final int SIZE_UNKNOWN = -1;

    static enum Dimension {
        DIMENSION_WIDTH,
        DIMENSION_HEIGHT
    }

    /**
     * Set the actual bounds for this widget. These coordinates are relative to the parents
     * coordinate system. This function is typically called by the parent of this widget
     * and should only be used by the application for the toplevel widget.
     * @param bounds
     * @return a reference to this widget
     */
    void setBounds(Rectangle bounds);

    /**
     * Version of getDesiredWidth/getDesiredHeight that accepts a dimension parameter.
     * @param dimension
     * @return
     */
    int getDesiredSize(Dimension dimension);

    /**
     * Set the desired width for this widget. This property is used by layout instances
     * which may honor or ignore this. You can also use one of the SIZE_... constants.
     */
    P setDesiredWidth(int width);

    int getDesiredWidth();

    /**
     * Set the desired height for this widget. This property is used by layout instances
     * which may honor or ignore this. You can also use one of the SIZE_... constants.
     */
    P setDesiredHeight(int height);

    int getDesiredHeight();

    /**
     * Set the tooltip for this widget.
     */
    P setTooltips(String... tooltips);

    List<String> getTooltips();

    /**
     * Enable or disable mouse interaction with this widget. This is true by default.
     */
    P setEnabled(boolean enabled);

    boolean isEnabled();

    /**
     * Get the bounds for this widget relative to the parents coordinate system.
     * Can be null in case the layout for this widget hasn't been set yet.
     * @return
     */
    Rectangle getBounds();

    /**
     * Check if a coordinate is in the bounds of this widget.
     */
    boolean in(int x, int y);

    /**
     * Find the widget at the given position. It can be assumed in this function that it
     * is only called for valid coordaintes that are guaranteed to be in this widget. So widgets
     * that don't have children should just return themselves.
     */
    Widget getWidgetAtPosition(int x, int y);

    /**
     * Draw this widget on the GUI at the specific position. This position is usually supplied by the
     * parent widget or for the top level widget it is the top-left corner on screen.
     * The given coordinates are the absolute coordinates of the parent. This does *not* include
     * the top/left x,y of this widget itself.
     */
    void draw(Window window, int x, int y);

    /**
     * Handle a mouse click for this widget. This widget does not have to check if the coordinate is
     * in the bounds. The given coordinates are relative to the parent of this widget.
     *
     *
     * @param window
     * @param x
     * @param y
     * @param button
     * @return a reference to the widget that wants focus (or null if not)
     */
    Widget mouseClick(Window window, int x, int y, int button);

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

    /**
     * Handle a keyboard event.
     *
     * @param window
     * @param typedChar
     * @param keyCode
     * @return true if key was handled
     */
    boolean keyTyped(Window window, char typedChar, int keyCode);

    /**
     * Some layout managers need a layout hint.
     *
     * @param hint
     * @return this widget
     */
    P setLayoutHint(LayoutHint hint);

    LayoutHint getLayoutHint();
}

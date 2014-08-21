package com.mcjty.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

/**
 * This class represents a window. It contains a single Widget which
 * represents the contents of this window. That widget is usually a Panel.
 */
public class Window {

    private final Widget toplevel;
    private final GuiScreen gui;

    public Window(GuiScreen gui, Widget toplevel) {
        this.gui = gui;
        this.toplevel = toplevel;
    }

    public Widget getToplevel() {
        return toplevel;
    }

    public void mouseClicked(int x, int y, int button) {
        if (toplevel.getBounds().contains(x, y)) {
            toplevel.mouseClick(x, y, button);
        }
    }

    public void handleMouseInput() {
        int x = Mouse.getEventX() * gui.width / gui.mc.displayWidth;
        int y = gui.height - Mouse.getEventY() * gui.height / gui.mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        if (k == -1) {
            mouseMovedOrUp(x, y, k);
        }
    }

    public void mouseMovedOrUp(int x, int y, int button) {
        // -1 == mouse move
        if (button != -1) {
            toplevel.mouseRelease(x, y, button);
        } else {
            toplevel.mouseMove(x, y);
        }
    }

    public void draw() {
        toplevel.draw(0, 0);
    }
}

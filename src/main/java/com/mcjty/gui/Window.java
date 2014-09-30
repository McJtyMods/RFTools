package com.mcjty.gui;

import com.mcjty.gui.widgets.Widget;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.util.List;

/**
 * This class represents a window. It contains a single Widget which
 * represents the contents of this window. That widget is usually a Panel.
 */
public class Window {

    private final Widget toplevel;
    private final GuiScreen gui;
    private Widget textFocus = null;

    public Window(GuiScreen gui, Widget toplevel) {
        this.gui = gui;
        this.toplevel = toplevel;
    }

    public Widget getToplevel() {
        return toplevel;
    }

    public void mouseClicked(int x, int y, int button) {
        textFocus = null;
        if (toplevel.in(x, y)) {
            toplevel.mouseClick(this, x, y, button);
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

    public void setTextFocus(Widget focus) {
        textFocus = focus;
    }

    public Widget getTextFocus() {
        return textFocus;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        if (textFocus != null) {
            return textFocus.keyTyped(this, typedChar, keyCode);
        }
        return false;
    }

    public void draw() {
        toplevel.draw(this, 0, 0);
    }

    public List<String> getTooltips() {
        int x = Mouse.getEventX() * gui.width / gui.mc.displayWidth;
        int y = gui.height - Mouse.getEventY() * gui.height / gui.mc.displayHeight - 1;
        if (toplevel.in(x, y)) {
            Widget w = toplevel.getWidgetAtPosition(x, y);
            List<String> tooltips = w.getTooltips();
            if (tooltips != null) {
                return tooltips;
            }
        }
        return null;
    }
}

package com.mcjty.gui.widgets;

import com.mcjty.gui.RenderHelper;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.List;

public class Button extends Label<Button> {
    private List<ButtonEvent> buttonEvents = null;
    private boolean pressed = false;

    public Button(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    @Override
    public void draw(Window window, int x, int y) {
        int xx = x + bounds.x;
        int yy = y + bounds.y;

        if (enabled) {
            if (pressed) {
                RenderHelper.drawBeveledBox(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, 0xff666666, 0xff333333, 0xffeeeeee);
            } else {
                RenderHelper.drawBeveledBox(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, 0xffeeeeee, 0xff333333, 0xff666666);
            }
        } else {
            RenderHelper.drawBeveledBox(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, 0xff888888, 0xff555555, 0xff666666);
        }

        super.draw(window, x, y);
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        if (enabled) {
            pressed = true;
            return this;
        }
        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);
        if (pressed) {
            pressed = false;
            if (enabled) {
                fireButtonEvents();
            }
        }
    }

    public Button addButtonEvent(ButtonEvent event) {
        if (buttonEvents == null) {
            buttonEvents = new ArrayList<ButtonEvent>();
        }
        buttonEvents.add(event);
        return this;
    }

    public void removeButtonEvent(ButtonEvent event) {
        if (buttonEvents != null) {
            buttonEvents.remove(event);
        }
    }

    private void fireButtonEvents() {
        if (buttonEvents != null) {
            for (ButtonEvent event : buttonEvents) {
                event.buttonClicked(this);
            }
        }
    }
}

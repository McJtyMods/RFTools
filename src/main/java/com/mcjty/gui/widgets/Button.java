package com.mcjty.gui.widgets;

import com.mcjty.gui.RenderHelper;
import com.mcjty.gui.events.ButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.List;

public class Button extends Label<Button> {
    private List<ButtonEvent> buttonEvents = null;

    public Button(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    @Override
    public void draw(int x, int y) {
        int xx = x + bounds.x;
        int yy = y + bounds.y;

        if (enabled) {
            RenderHelper.drawBeveledBox(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, 0xffeeeeee, 0xff333333, 0xff666666);
        } else {
            RenderHelper.drawBeveledBox(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, 0xff888888, 0xff555555, 0xff666666);
        }

        super.draw(x, y);
    }

    @Override
    public Widget mouseClick(int x, int y, int button) {
        if (enabled) {
            fireButtonEvents();
        }
        return null;
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

package com.mcjty.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;

public class AbstractContainerWidget<P extends AbstractContainerWidget> extends AbstractWidget<P> {
    protected ArrayList<Widget> children = new ArrayList<Widget>();

    public AbstractContainerWidget(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        markDirty();
        super.setBounds(bounds);
    }

    @Override
    public Widget getWidgetAtPosition(int x, int y) {
        x -= bounds.x;
        y -= bounds.y;

        for (Widget child : children) {
            if (child.in(x, y) && child.isVisible()) {
                return child.getWidgetAtPosition(x, y);
            }
        }

        return this;
    }

    public P addChild(Widget child) {
        if (child == null) {
            throw new RuntimeException("THIS IS NOT POSSIBLE!");
        }
        children.add(child);
        markDirty();
        return (P) this;
    }

    public P removeChild(Widget child) {
        children.remove(child);
        markDirty();
        return (P) this;
    }

    public void removeChildren() {
        children.clear();
        markDirty();
    }

}

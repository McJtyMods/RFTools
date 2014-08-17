package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;

public class Panel extends AbstractWidget<Panel> {

    private ArrayList<Widget> children = new ArrayList<Widget>();
    private Layout layout = new HorizontalLayout();
    private boolean layoutDirty = true;

    public Panel() {
    }

    @Override
    public void setBounds(Rectangle bounds) {
        layoutDirty = true;
        super.setBounds(bounds);
    }

    public Panel addChild(Widget child) {
        children.add(child);
        layoutDirty = true;
        return this;
    }

    public Panel removeChild(Widget child) {
        children.remove(child);
        layoutDirty = true;
        return this;
    }

    public Panel setLayout(Layout layout) {
        this.layout = layout;
        layoutDirty = true;
        return this;
    }


    @Override
    public void draw(Minecraft mc, Gui gui, int x, int y) {
        super.draw(mc, gui, x, y);

        if (layoutDirty) {
            layout.doLayout(children, bounds.width, bounds.height);
            layoutDirty = false;
        }

        for (Widget child : children) {
            child.draw(mc, gui, x + child.getBounds().x, y + child.getBounds().y);
        }
    }
}

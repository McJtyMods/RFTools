package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;

public class Panel extends AbstractWidget<Panel> {

    private ArrayList<Widget> children = new ArrayList<Widget>();
    private Layout layout = new HorizontalLayout();
    private boolean layoutDirty = true;

    private Widget focus = null;

    public Panel(Minecraft mc, Gui gui) {
        super(mc, gui);
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
    public void draw(int x, int y) {
        super.draw(x, y);
        int xx = x + bounds.x;
        int yy = y + bounds.y;
//        drawBox(xx, yy, 0xffff0000);


        if (layoutDirty) {
            layout.doLayout(children, bounds.width, bounds.height);
            layoutDirty = false;
        }

        for (Widget child : children) {
            child.draw(xx, yy);
        }
    }

    @Override
    public Widget mouseClick(int x, int y, int button) {
        super.mouseClick(x, y, button);

        x -= bounds.x;
        y -= bounds.y;

        for (Widget child : children) {
            if (child.getBounds().contains(x, y)) {
                focus = child.mouseClick(x, y, button);
                return this;
            }
        }

        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);
        x -= bounds.x;
        y -= bounds.y;

        if (focus != null) {
            focus.mouseRelease(x, y, button);
            focus = null;
        } else {
            for (Widget child : children) {
                if (child.getBounds().contains(x, y)) {
                    child.mouseRelease(x, y, button);
                    return;
                }
            }
        }
    }

    @Override
    public void mouseMove(int x, int y) {
        super.mouseMove(x, y);

        x -= bounds.x;
        y -= bounds.y;

        if (focus != null) {
            focus.mouseMove(x, y);
        } else {
            for (Widget child : children) {
                if (child.getBounds().contains(x, y)) {
                    child.mouseMove(x, y);
                    return;
                }
            }
        }

    }
}

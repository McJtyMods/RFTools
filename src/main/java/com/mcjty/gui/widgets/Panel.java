package com.mcjty.gui.widgets;

import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.Layout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Panel extends AbstractContainerWidget<Panel> {

    private Layout layout = new HorizontalLayout();

    private Widget focus = null;

    public Panel(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public Panel setLayout(Layout layout) {
        this.layout = layout;
        markDirty();
        return this;
    }

    @Override
    public void draw(Window window, int x, int y) {
        super.draw(window, x, y);
        int xx = x + bounds.x;
        int yy = y + bounds.y;
//        drawBox(xx, yy, 0xffff0000);


        if (isDirty()) {
            layout.doLayout(children, bounds.width, bounds.height);
            markClean();
        }

        for (Widget child : children) {
            child.draw(window, xx, yy);
        }
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        super.mouseClick(window, x, y, button);

        x -= bounds.x;
        y -= bounds.y;

        for (Widget child : children) {
            if (child.in(x, y)) {
                focus = child.mouseClick(window, x, y, button);
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
                if (child.in(x, y)) {
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
                if (child.in(x, y)) {
                    child.mouseMove(x, y);
                    return;
                }
            }
        }
    }
}

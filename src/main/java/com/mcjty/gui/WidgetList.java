package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class WidgetList extends AbstractContainerWidget<WidgetList> implements Scrollable {
    private int rowheight = 10;
    private int first = 0;

    public WidgetList(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public int getRowheight() {
        return rowheight;
    }

    public WidgetList setRowheight(int rowheight) {
        this.rowheight = rowheight;
        return this;
    }

    @Override
    public void draw(int x, int y) {
        super.draw(x, y);
        int xx = x + bounds.x;
        int yy = y + bounds.y;
        int top = 0;        // Margin@@@?
//        drawBox(xx, yy, 0xffff0000);

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Widget child = children.get(i);
            child.setBounds(new Rectangle(0 /*@@@ margin?*/, top, bounds.width, rowheight));
            child.draw(xx, yy);
            top += rowheight;

        }
    }

    @Override
    public int getMaximum() {
        return children.size();
    }

    @Override
    public int getCountSelected() {
        return bounds.height / rowheight;
    }

    @Override
    public int getFirstSelected() {
        return first;
    }

    @Override
    public void setFirstSelected(int first) {
        this.first = first;
    }
}

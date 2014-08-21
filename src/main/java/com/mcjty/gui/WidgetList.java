package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class WidgetList extends AbstractContainerWidget<WidgetList> implements Scrollable {
    private int rowheight = 10;
    private int first = 0;
    private int selected = -1;
    private List<SelectionEvent> selectionEvents = null;

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

    public int getSelected() {
        return selected;
    }

    public WidgetList setSelected(int selected) {
        this.selected = selected;
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
            if (i == selected) {
                RenderHelper.drawHorizontalGradientRect(xx, yy+top, xx+bounds.width, yy+top+rowheight, 0xff666666, 0xff444444);
            }
            child.draw(xx, yy);
            top += rowheight;
        }
    }

    @Override
    public Widget mouseClick(int x, int y, int button) {
        int newSelected = -1;
        int top = 0;        // Margin@@@?

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Rectangle r = new Rectangle(0, top, bounds.width, rowheight);
            if (r.contains(x, y)) {
                newSelected = i;
                break;
            }
            top += rowheight;
        }
        if (newSelected != selected) {
            selected = newSelected;
            fireSelectionEvents(selected);
        }
        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);
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

    public WidgetList addSelectionEvent(SelectionEvent event) {
        if (selectionEvents == null) {
            selectionEvents = new ArrayList<SelectionEvent> ();
        }
        selectionEvents.add(event);
        return this;
    }

    public void removeSelectionEvent(SelectionEvent event) {
        if (selectionEvents != null) {
            selectionEvents.remove(event);
        }
    }

    private void fireSelectionEvents(int index) {
        if (selectionEvents != null) {
            for (SelectionEvent event : selectionEvents) {
                event.select(this, index);
            }
        }
    }
}

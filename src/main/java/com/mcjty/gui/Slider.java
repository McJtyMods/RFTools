package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Slider extends AbstractWidget<Slider> {
    private boolean dragging = false;
    private int dx, dy;
    private boolean horizontal = false;

    private Scrollable scrollable;

    public Slider(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public Scrollable getScrollable() {
        return scrollable;
    }

    public Slider setScrollable(Scrollable scrollable) {
        this.scrollable = scrollable;
        return this;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isVertical() {
        return !horizontal;
    }

    public Slider setHorizontal() {
        this.horizontal = true;
        return this;
    }

    public Slider setVertical() {
        this.horizontal = false;
        return this;
    }

    @Override
    public void draw(int x, int y) {
        super.draw(x, y);

        int xx = x + bounds.x;
        int yy = y + bounds.y;

        Gui.drawRect(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, 0xff000000);

        if (horizontal) {
            int size = (scrollable.getCountSelected() * (bounds.width-4)) / scrollable.getMaximum();
            int first = (scrollable.getFirstSelected() * (bounds.width-4-size)) / scrollable.getMaximum();

            Gui.drawRect(xx+2 + first, yy+2, xx+2 + first + size-1, yy+bounds.height-4, 0xff777777);
        } else {
            int size = (scrollable.getCountSelected() * (bounds.height-4)) / scrollable.getMaximum();
            int first = (scrollable.getFirstSelected() * (bounds.height-4-size)) / scrollable.getMaximum();

            Gui.drawRect(xx+2, yy+2 + first, xx+bounds.width-4, yy + 2 + first + size-1, 0xff777777);
        }
    }

    private void updateScrollable(int x, int y) {
        int first;
        if (horizontal) {
            first = ((x-bounds.x-dx) * scrollable.getMaximum()) / (bounds.width-4);
        } else {
            first = ((y-bounds.y-dy) * scrollable.getMaximum()) / (bounds.height-4);
        }
        if (first < 0) {
            first = 0;
        } else if (first > scrollable.getMaximum()) {
            first = scrollable.getMaximum();
        }
        scrollable.setFirstSelected(first);
    }

    @Override
    public Widget mouseClick(int x, int y, int button) {
        super.mouseClick(x, y, button);
        dragging = true;
        dx = x-bounds.x;
        dy = y-bounds.y;
        return this;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);
        if (dragging) {
            updateScrollable(x, y);
            dragging = false;
        }
    }

    @Override
    public void mouseMove(int x, int y) {
        super.mouseMove(x, y);
        if (dragging) {
            updateScrollable(x, y);
//            System.out.println("x = " + x + "," + y);
        }
    }
}

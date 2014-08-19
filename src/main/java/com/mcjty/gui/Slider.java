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

        int divider = scrollable.getMaximum() - scrollable.getCountSelected();

        if (horizontal) {
            int size;
            int first;
            if (divider <= 0) {
                size = bounds.width - 4;
                first = 0;
            } else {
                size = (scrollable.getCountSelected() * (bounds.width-4)) / scrollable.getMaximum();
                first = (scrollable.getFirstSelected() * (bounds.width-4-size)) / divider;
            }
            Gui.drawRect(xx+2 + first, yy+2, xx+2 + first + size-1, yy+bounds.height-4, 0xff777777);
        } else {
            int size;
            int first;
            if (divider <= 0) {
                size = bounds.height - 4;
                first = 0;
            } else {
                size = (scrollable.getCountSelected() * (bounds.height-4)) / scrollable.getMaximum();
                first = (scrollable.getFirstSelected() * (bounds.height-4-size)) / divider;
            }

            Gui.drawRect(xx+2, yy+2 + first, xx+bounds.width-4, yy + 2 + first + size-1, 0xff777777);
        }
    }

    private void updateScrollable(int x, int y) {
        int first;
        int divider = scrollable.getMaximum() - scrollable.getCountSelected();
        if (divider <= 0) {
            first = 0;
        } else {
            if (horizontal) {
                int size = (scrollable.getCountSelected() * (bounds.width-4)) / scrollable.getMaximum();
                first = ((x-bounds.x-dx) * divider) / (bounds.width-4-size);
            } else {
                int size = (scrollable.getCountSelected() * (bounds.height-4)) / scrollable.getMaximum();
                first = ((y-bounds.y-dy) * divider) / (bounds.height-4-size);
            }
        }
        if (first > divider) {
            first = divider;
        }
        if (first < 0) {
            first = 0;
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

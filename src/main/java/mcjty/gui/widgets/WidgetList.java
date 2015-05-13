package mcjty.gui.widgets;

import mcjty.gui.RenderHelper;
import mcjty.gui.Scrollable;
import mcjty.gui.Window;
import mcjty.gui.events.SelectionEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class WidgetList extends AbstractContainerWidget<WidgetList> implements Scrollable {
    private int rowheight = 16;
    private int first = 0;
    private int selected = -1;
    private long prevTime = -1;
    private boolean propagateEventsToChildren = false;
    private List<SelectionEvent> selectionEvents = null;
    private Set<Integer> hilightedRows = new HashSet<Integer>();
    private boolean noselection = false;
    private int leftMargin = 2;
    private int topMargin = 1;

    public WidgetList(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public int getRowheight() {
        return rowheight;
    }

    // Setting rowheight to -1 will use variable height depending on the desired height of every row
    public WidgetList setRowheight(int rowheight) {
        this.rowheight = rowheight;
        return this;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public WidgetList setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
        return this;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public WidgetList setTopMargin(int topMargin) {
        this.topMargin = topMargin;
        return this;
    }

    public int getSelected() {
        return selected;
    }

    public WidgetList setSelected(int selected) {
        this.selected = selected;
        return this;
    }

    public boolean isPropagateEventsToChildren() {
        return propagateEventsToChildren;
    }

    public WidgetList setNoSelectionMode(boolean m) {
        this.noselection = m;
        return this;
    }

    public boolean isNoSelection() {
        return noselection;
    }

    public WidgetList setPropagateEventsToChildren(boolean propagateEventsToChildren) {
        this.propagateEventsToChildren = propagateEventsToChildren;
        return this;
    }

    public void addHilightedRow(int index) {
        hilightedRows.add(index);
    }

    public void clearHilightedRows() {
        hilightedRows.clear();
    }

    @Override
    public void draw(Window window, int x, int y) {
        if (!visible) {
            return;
        }

        // Use the function above to force reset of the slider in case the contents changed so that it doesn't look weird.
        mouseWheel(0, x, y);

        super.draw(window, x, y);
        int xx = x + bounds.x + leftMargin;
        int yy = y + bounds.y + topMargin;
        int top = 0;        // Margin@@@?
//        drawBox(xx, yy, 0xffff0000);

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Widget child = children.get(i);
            int rh = rowheight == -1 ? child.getDesiredHeight() : rowheight;
            child.setBounds(new Rectangle(0 /*@@@ margin?*/, top, bounds.width, rh));
            boolean hilighted = hilightedRows.contains(i);
            if (i == selected && hilighted) {
                RenderHelper.drawHorizontalGradientRect(xx, yy + top, xx + bounds.width - 4, yy + top + rh, 0xff888844, 0xff666622);
            } else if (i == selected) {
                RenderHelper.drawHorizontalGradientRect(xx, yy + top, xx + bounds.width - 4, yy + top + rh, 0xff666666, 0xff444444);
            } else if (hilighted) {
                RenderHelper.drawHorizontalGradientRect(xx, yy + top, xx + bounds.width - 4, yy + top + rh, 0xffbbbb00, 0xff999900);
            }
            if (isEnabledAndVisible()) {
                child.draw(window, xx, yy);
            } else {
                boolean en = child.isEnabled();
                child.setEnabled(false);
                child.draw(window, xx, yy);
                child.setEnabled(en);
            }
            top += rh;
        }
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        if (!isEnabledAndVisible()) {
            return null;
        }

        if (noselection) {
            return null;
        }

        int newSelected = -1;
        int top = bounds.y;        // Margin@@@?

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            int rh = rowheight == -1 ? children.get(i).getDesiredHeight() : rowheight;
            Rectangle r = new Rectangle(bounds.x, top, bounds.width, rh);
            if (r.contains(x, y)) {
                newSelected = i;
                break;
            }
            top += rh;
        }
        if (newSelected != selected) {
            selected = newSelected;
            fireSelectionEvents(selected);
        }

        if (propagateEventsToChildren && selected != -1) {
            Widget child = children.get(selected);
            int xx = x-bounds.x;
            int yy = y-bounds.y;
            if (child.in(xx, yy) && child.isVisible()) {
                child.mouseClick(window, xx, yy, button);
            }
        }

        long t = System.currentTimeMillis();
        if (prevTime != -1 && (t - prevTime) < 250) {
            fireDoubleClickEvent(selected);
        }
        prevTime = t;

        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);
    }

    @Override
    public boolean mouseWheel(int amount, int x, int y) {
        int divider = getMaximum() - getCountSelected();
        if (divider <= 0) {
            first = 0;
        } else {
            if (amount > 0) {
                first -= 3;
            } else if (amount < 0) {
                first += 3;
            }
        }
        if (first > divider) {
            first = divider;
        }
        if (first < 0) {
            first = 0;
        }

        return true;
    }

    @Override
    public int getMaximum() {
        return children.size();
    }

    @Override
    public int getCountSelected() {
        if (rowheight != -1) {
            return bounds.height / rowheight;
        } else {
            int totalh = 0;
            int cnt = 0;
            for (int i = first; i < children.size(); i++) {
                int rh = children.get(i).getDesiredHeight();
                if (totalh + rh > bounds.height) {
                    break;
                }
                totalh += rh;
                cnt++;
            }
            return cnt;
        }
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

    private void fireDoubleClickEvent(int index) {
        if (selectionEvents != null) {
            for (SelectionEvent event : selectionEvents) {
                event.doubleClick(this, index);
            }
        }
    }
    @Override
    public WidgetList removeChild(Widget child) {
        int index = children.indexOf(child);
        if (index != -1) {
            Set<Integer> newHighlights = new HashSet<Integer>();
            for (Integer i : hilightedRows) {
                if (i < index) {
                    newHighlights.add(i);
                } else if (i > index) {
                    newHighlights.add(i-1);
                }
            }
            hilightedRows = newHighlights;
        }
        return super.removeChild(child);
    }

    @Override
    public void removeChildren() {
        super.removeChildren();
        hilightedRows.clear();
    }
}

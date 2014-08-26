package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.List;

public class ScrollableLabel extends Label<ScrollableLabel> implements Scrollable {
    private int realmin = 0;
    private int realmax = 100;
    private int first = 0;
    private String suffix = "";
    private List<ValueEvent> valueEvents = null;

    public ScrollableLabel(Minecraft mc, Gui gui) {
        super(mc, gui);
        setFirstSelected(0);
    }

    public String getSuffix() {
        return suffix;
    }

    public ScrollableLabel setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public ScrollableLabel setRealMaximum(int realmax) {
        this.realmax = realmax;

        if (first > realmax-realmin) {
            first = realmax-realmin;
        }
        setFirstSelected(first);

        return this;
    }

    public int getRealMaximum() {
        return realmax;
    }

    public int getRealValue() {
        return first + realmin;
    }

    public ScrollableLabel setRealValue(int value) {
        int f = value - realmin;
        if (f < 0) {
            f = 0;
        }
        setFirstSelected(f);
        return this;
    }

    public ScrollableLabel setRealMinimum(int realmin) {
        this.realmin = realmin;

        if (first < 0) {
            first = 0;
        }
        setFirstSelected(first);

        return this;
    }

    public int getRealMinimum() {
        return realmin;
    }

    @Override
    public int getMaximum() {
        return (realmax-realmin+1);
    }

    @Override
    public int getCountSelected() {
        return 1;
    }

    @Override
    public int getFirstSelected() {
        return first;
    }

    @Override
    public void setFirstSelected(int first) {
        this.first = first;
        setText(getRealValue() + suffix);
        fireValueEvents(getRealValue());
    }

    public ScrollableLabel addValueEvent(ValueEvent event) {
        if (valueEvents == null) {
            valueEvents = new ArrayList<ValueEvent>();
        }
        valueEvents.add(event);
        return this;
    }

    public void removeValueEvent(ValueEvent event) {
        if (valueEvents != null) {
            valueEvents.remove(event);
        }
    }

    private void fireValueEvents(int value) {
        if (valueEvents != null) {
            for (ValueEvent event : valueEvents) {
                event.valueChanged(this, value);
            }
        }
    }

}

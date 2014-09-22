package com.mcjty.gui;

/**
 * Numeric scrollable implementation which can be used for a slider.
 */
public class NumericScrollable implements Scrollable {
    private int maximum;
    private int countSelected;
    private int firstSelected;

    public NumericScrollable(int maximum, int countSelected, int firstSelected) {
        this.maximum = maximum;
        this.countSelected = countSelected;
        this.firstSelected = firstSelected;
    }

    @Override
    public int getMaximum() {
        return maximum;
    }

    @Override
    public int getCountSelected() {
        return countSelected;
    }

    @Override
    public int getFirstSelected() {
        return firstSelected;
    }

    @Override
    public void setFirstSelected(int first) {
        this.firstSelected = first;
    }
}

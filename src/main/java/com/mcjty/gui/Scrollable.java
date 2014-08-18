package com.mcjty.gui;

public interface Scrollable {
    /**
     * Get the maximum amount of items.
     * @return
     */
    int getMaximum();

    /**
     * Get the amount of 'selected' items.
     */
    int getCountSelected();

    /**
     * Get the first selected item.
     */
    int getFirstSelected();

    /**
     * Set the first selected item.
     */
    void setFirstSelected(int first);
}

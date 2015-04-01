package mcjty.gui;

/**
 * Implement this interface if you want something to be controllable by a slider.
 */
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

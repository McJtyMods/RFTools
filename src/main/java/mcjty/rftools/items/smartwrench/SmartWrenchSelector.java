package mcjty.rftools.items.smartwrench;

public interface SmartWrenchSelector {

    /**
     * This is only called server side. Select a block for this tile entity.
     * @param x
     * @param y
     * @param z
     */
    void selectBlock(int x, int y, int z);
}

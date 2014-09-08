package com.mcjty.container;

public class SlotFactory {
    private final SlotType slotType;
    private final int index;
    private final int x;
    private final int y;

    private final String inventoryName;

    public SlotFactory(SlotType slotType, String inventoryName, int index, int x, int y) {
        this.inventoryName = inventoryName;
        this.slotType = slotType;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public int getIndex() {
        return index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getInventoryName() {
        return inventoryName;
    }
}

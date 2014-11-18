package com.mcjty.container;

public class SlotFactory {
    private final SlotDefinition slotDefinition;
    private final int index;
    private final int x;
    private final int y;

    private final String inventoryName;

    public SlotFactory(SlotDefinition slotDefinition, String inventoryName, int index, int x, int y) {
        this.inventoryName = inventoryName;
        this.slotDefinition = slotDefinition;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public SlotDefinition getSlotDefinition() {
        return slotDefinition;
    }

    public SlotType getSlotType() {
        return slotDefinition.getType();
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

package com.mcjty.container;

import com.sun.istack.internal.NotNull;

import java.util.*;

public class ContainerFactory {
    private Map<Integer,SlotType> indexToType = new HashMap<Integer, SlotType>();
    private Map<SlotType,SlotRanges> slotRangesMap = new HashMap<SlotType,SlotRanges>();
    private List<SlotFactory> slots = new ArrayList<SlotFactory>();

    public Map<SlotType, SlotRanges> getSlotRangesMap() {
        return slotRangesMap;
    }

    public Iterable<SlotFactory> getSlots() {
        return slots;
    }

    /**
     * Return the type of this slot for the given index.
     * @param index
     * @return
     */
    @NotNull
    public SlotType getSlotType(int index) {
        SlotType slotType = indexToType.get(index);
        if (slotType == null) {
            return SlotType.SLOT_UNKNOWN;
        }
        return slotType;
    }

    public boolean isOutputSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_OUTPUT;
    }

    public boolean isInputSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_INPUT;
    }

    public boolean isGhostSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_GHOST;
    }

    public boolean isPlayerInventorySlot(int index) {
        return getSlotType(index) == SlotType.SLOT_PLAYERINV;
    }

    public boolean isPlayerHotbarSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_PLAYERHOTBAR;
    }

    public void addSlot(SlotType slotType, String inventoryName, int index, int x, int y) {
        SlotFactory slotFactory = new SlotFactory(slotType, inventoryName, index, x, y);
        int slotIndex = slots.size();
        slots.add(slotFactory);

        SlotRanges slotRanges = slotRangesMap.get(slotType);
        if (slotRanges == null) {
            slotRanges = new SlotRanges(slotType);
            slotRangesMap.put(slotType, slotRanges);
        }
        slotRanges.addSingle(slotIndex);
        indexToType.put(slotIndex, slotType);
    }

    public int addSlotRange(SlotType slotType, String inventoryName, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(slotType, inventoryName, index, x, y);
            x += dx;
            index++;
        }
        return index;
    }

    public int addSlotBox(SlotType slotType, String inventoryName, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(slotType, inventoryName, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }


}

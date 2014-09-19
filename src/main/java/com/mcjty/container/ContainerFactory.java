package com.mcjty.container;

import com.sun.istack.internal.NotNull;

import java.util.*;

public class ContainerFactory {
    private Map<Integer,SlotType> indexToType = new HashMap<Integer, SlotType>();
    private Map<SlotType,SlotRanges> slotRangesMap = new HashMap<SlotType,SlotRanges>();
    private List<SlotFactory> slots = new ArrayList<SlotFactory>();

    public static final String CONTAINER_PLAYER = "player";

    private boolean slotsSetup = false;
    protected int[] accessibleSlots;
    protected int[] accessibleInputSlots;
    protected int[] accessibleOutputSlots;

    protected void setupAccessibleSlots() {
        if (slotsSetup) {
            return;
        }
        slotsSetup = true;
        List<Integer> s = new ArrayList<Integer>();
        List<Integer> si = new ArrayList<Integer>();
        List<Integer> so = new ArrayList<Integer>();
        int index = 0;
        for (SlotFactory slotFactory : slots) {
            if (slotFactory.getSlotType() == SlotType.SLOT_INPUT) {
                s.add(index);
                si.add(index);
            }
            if (slotFactory.getSlotType() == SlotType.SLOT_OUTPUT) {
                s.add(index);
                so.add(index);
            }
            index++;
        }
        accessibleSlots = convertList(s);
        accessibleInputSlots = convertList(si);
        accessibleOutputSlots = convertList(so);
    }

    private static int[] convertList(List<Integer> list) {
        int[] s = new int[list.size()];
        for (int i = 0 ; i < list.size() ; i++) {
            s[i] = list.get(i);
        }
        return s;
    }

    public Map<SlotType, SlotRanges> getSlotRangesMap() {
        return slotRangesMap;
    }

    public int[] getAccessibleSlots() {
        setupAccessibleSlots();
        return accessibleSlots;
    }

    public int[] getAccessibleInputSlots() {
        setupAccessibleSlots();
        return accessibleInputSlots;
    }

    public int[] getAccessibleOutputSlots() {
        setupAccessibleSlots();
        return accessibleOutputSlots;
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

    public boolean isGhostOutputSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_GHOSTOUT;
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

    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(SlotType.SLOT_PLAYERINV, CONTAINER_PLAYER, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(SlotType.SLOT_PLAYERHOTBAR, CONTAINER_PLAYER, 0, leftCol, topRow, 9, 18);

    }

}

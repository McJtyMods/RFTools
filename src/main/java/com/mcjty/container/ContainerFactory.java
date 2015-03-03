package com.mcjty.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerFactory {
    private Map<Integer,SlotDefinition> indexToType = new HashMap<Integer, SlotDefinition>();
    private Map<SlotDefinition,SlotRanges> slotRangesMap = new HashMap<SlotDefinition,SlotRanges>();
    private List<SlotFactory> slots = new ArrayList<SlotFactory>();

    public static final String CONTAINER_PLAYER = "player";

    private boolean slotsSetup = false;
    protected int[] accessibleSlots;
    protected int[] accessibleInputSlots;
    protected int[] accessibleOutputSlots;

    public ContainerFactory() {
        setup();
    }

    protected void setup() {

    }

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

    public Map<SlotDefinition, SlotRanges> getSlotRangesMap() {
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
    public SlotType getSlotType(int index) {
        SlotDefinition slotDefinition = indexToType.get(index);
        if (slotDefinition == null) {
            return SlotType.SLOT_UNKNOWN;
        }
        return slotDefinition.getType();
    }

    public boolean isContainerSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_CONTAINER;
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

    public boolean isCraftResultSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_CRAFTRESULT;
    }

    public boolean isPlayerInventorySlot(int index) {
        return getSlotType(index) == SlotType.SLOT_PLAYERINV;
    }

    public boolean isSpecificItemSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_SPECIFICITEM;
    }

    public boolean isPlayerHotbarSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_PLAYERHOTBAR;
    }

    public void addSlot(SlotDefinition slotDefinition, String inventoryName, int index, int x, int y) {
        SlotFactory slotFactory = new SlotFactory(slotDefinition, inventoryName, index, x, y);
        int slotIndex = slots.size();
        slots.add(slotFactory);

        SlotRanges slotRanges = slotRangesMap.get(slotDefinition);
        if (slotRanges == null) {
            slotRanges = new SlotRanges(slotDefinition);
            slotRangesMap.put(slotDefinition, slotRanges);
        }
        slotRanges.addSingle(slotIndex);
        indexToType.put(slotIndex, slotDefinition);
    }

    public int addSlotRange(SlotDefinition slotDefinition, String inventoryName, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(slotDefinition, inventoryName, index, x, y);
            x += dx;
            index++;
        }
        return index;
    }

    public int addSlotBox(SlotDefinition slotDefinition, String inventoryName, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(slotDefinition, inventoryName, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(new SlotDefinition(SlotType.SLOT_PLAYERINV), CONTAINER_PLAYER, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(new SlotDefinition(SlotType.SLOT_PLAYERHOTBAR), CONTAINER_PLAYER, 0, leftCol, topRow, 9, 18);

    }

}

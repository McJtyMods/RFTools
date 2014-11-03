package com.mcjty.rftools.blocks.shield;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.SlotType;

public class ShieldContainerFactory  extends ContainerFactory {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_BUFFER = 0;
    public static final int BUFFER_SIZE = 1;
    public static final int SLOT_PLAYERINV = SLOT_BUFFER + BUFFER_SIZE;
    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    private static ShieldContainerFactory instance = null;

    public static synchronized ShieldContainerFactory getInstance() {
        if (instance == null) {
            instance = new ShieldContainerFactory();
        }
        return instance;
    }

    public ShieldContainerFactory() {
        layoutBuffer();
        layoutPlayerInventorySlots(85, 142);
    }

    private void layoutBuffer() {
        // Input slots
        int leftCol = 31;
        int topRow = 142;
        addSlotBox(SlotType.SLOT_INPUT, CONTAINER_INVENTORY, SLOT_BUFFER, leftCol, topRow, 1, 18, 1, 18);
    }

}

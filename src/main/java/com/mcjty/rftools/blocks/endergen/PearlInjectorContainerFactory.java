package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.SlotType;

public class PearlInjectorContainerFactory extends ContainerFactory {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_BUFFER = 0;
    public static final int BUFFER_SIZE = (9*2);
    public static final int SLOT_PLAYERINV = SLOT_BUFFER + BUFFER_SIZE;
    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    private static PearlInjectorContainerFactory instance = null;

    public static synchronized PearlInjectorContainerFactory getInstance() {
        if (instance == null) {
            instance = new PearlInjectorContainerFactory();
        }
        return instance;
    }

    public PearlInjectorContainerFactory() {
        layoutBuffer();
        layoutPlayerInventorySlots(10, 70);
    }

    private void layoutBuffer() {
        // Input slots
        int leftCol = 10;
        int topRow = 25;
        addSlotBox(SlotType.SLOT_ENDERPEARL, CONTAINER_INVENTORY, SLOT_BUFFER, leftCol, topRow, 9, 18, 2, 18);
    }

}

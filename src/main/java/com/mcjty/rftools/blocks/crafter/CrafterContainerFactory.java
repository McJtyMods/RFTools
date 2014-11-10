package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.SlotType;

public class CrafterContainerFactory extends ContainerFactory {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = (13*2);
    public static final int SLOT_BUFFEROUT = SLOT_BUFFER + BUFFER_SIZE;
    public static final int BUFFEROUT_SIZE = 4;
//    public static final int SLOT_PLAYERINV = SLOT_BUFFEROUT + BUFFEROUT_SIZE;
//    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    private static CrafterContainerFactory instance = null;

    public static synchronized CrafterContainerFactory getInstance() {
        if (instance == null) {
            instance = new CrafterContainerFactory();
        }
        return instance;
    }

    public CrafterContainerFactory() {
        layoutCraftingGrid();
        layoutBuffer();
        layoutPlayerInventorySlots(85, 142);
    }

    private void layoutBuffer() {
        // Input slots
        int leftCol = 12;
        int topRow = 97;
        addSlotBox(SlotType.SLOT_INPUT, CONTAINER_INVENTORY, SLOT_BUFFER, leftCol, topRow, 13, 18, 2, 18);

        // Output slots
        leftCol = 31;
        topRow = 142;
        addSlotBox(SlotType.SLOT_OUTPUT, CONTAINER_INVENTORY, SLOT_BUFFEROUT, leftCol, topRow, 2, 18, 2, 18);
    }

    private void layoutCraftingGrid() {
        int leftCol = 193;
        int topRow = 7;
        addSlotBox(SlotType.SLOT_GHOST, CONTAINER_INVENTORY, SLOT_CRAFTINPUT, leftCol, topRow, 3, 18, 3, 18);
        addSlot(SlotType.SLOT_GHOSTOUT, CONTAINER_INVENTORY, SLOT_CRAFTOUTPUT, leftCol, 65);
    }


}

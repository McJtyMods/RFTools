package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.SlotType;

public class CrafterContainerFactory extends ContainerFactory {
    public static final String CONTAINER_INVENTORY = "container";
    public static final String CONTAINER_PLAYER = "player";

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = (12*2);
    public static final int SLOT_BUFFEROUT = SLOT_BUFFER + BUFFER_SIZE;
    public static final int BUFFEROUT_SIZE = 2;
    public static final int SLOT_PLAYERINV = SLOT_BUFFEROUT + BUFFEROUT_SIZE;
    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    private static CrafterContainerFactory instance = null;

    public static CrafterContainerFactory getInstance() {
        if (instance == null) {
            instance = new CrafterContainerFactory();
        }
        return instance;
    }

    public CrafterContainerFactory() {
        layoutCraftingGrid();
        layoutBuffer();
        layoutPlayerInventory();
    }

    private void layoutBuffer() {
        int leftCol = 12;
        int topRow = 89;

        // Input slots
        addSlotBox(SlotType.SLOT_INPUT, CONTAINER_INVENTORY, SLOT_BUFFER, leftCol, topRow, 12, 18, 2, 18);
        // Output slots
        for (int py = 0 ; py < 2 ; py++) {
            addSlot(SlotType.SLOT_OUTPUT, CONTAINER_INVENTORY, SLOT_BUFFEROUT + py, leftCol + 12 * 18, topRow + py * 18);
        }
    }

    private void layoutCraftingGrid() {
        int leftCol = 13;
        int topRow = 134;
        addSlotBox(SlotType.SLOT_GHOST, CONTAINER_INVENTORY, SLOT_CRAFTINPUT, leftCol, topRow, 3, 18, 3, 18);
        addSlot(SlotType.SLOT_GHOST, CONTAINER_INVENTORY, SLOT_CRAFTOUTPUT, leftCol, 192);
    }

    private void layoutPlayerInventory() {
        // Player inventory
        int leftCol = 85;
        int topRow = 134;
        addSlotBox(SlotType.SLOT_PLAYERINV, CONTAINER_PLAYER, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow = 192;
        addSlotRange(SlotType.SLOT_PLAYERHOTBAR, CONTAINER_PLAYER, 0, leftCol, topRow, 9, 18);
    }


}

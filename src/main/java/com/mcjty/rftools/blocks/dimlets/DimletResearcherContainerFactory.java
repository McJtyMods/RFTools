package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.SlotType;

public class DimletResearcherContainerFactory extends ContainerFactory {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_DIMLETINPUT = 0;
    public static final int SLOT_DIMLETOUTPUT = 1;

    private static DimletResearcherContainerFactory instance = null;

    public static synchronized DimletResearcherContainerFactory getInstance() {
        if (instance == null) {
            instance = new DimletResearcherContainerFactory();
        }
        return instance;
    }

    public DimletResearcherContainerFactory() {
        layoutCraftingGrid();
        layoutBuffer();
        layoutPlayerInventorySlots(85, 142);
    }

    private void layoutBuffer() {
        // Input slots
        int leftCol = 12;
        int topRow = 97;
        addSlotBox(SlotType.SLOT_INPUT, CONTAINER_INVENTORY, SLOT_DIMLETINPUT, leftCol, topRow, 1, 18, 1, 18);

        // Output slots
        leftCol = 31;
        topRow = 142;
        addSlotBox(SlotType.SLOT_OUTPUT, CONTAINER_INVENTORY, SLOT_DIMLETOUTPUT, leftCol, topRow, 1, 18, 1, 18);
    }

    private void layoutCraftingGrid() {
        int leftCol = 193;
        int topRow = 7;
        addSlotBox(SlotType.SLOT_GHOST, CONTAINER_INVENTORY, SLOT_DIMLETINPUT, leftCol, topRow, 3, 18, 3, 18);
        addSlot(SlotType.SLOT_GHOSTOUT, CONTAINER_INVENTORY, SLOT_DIMLETOUTPUT, leftCol, 65);
    }


}

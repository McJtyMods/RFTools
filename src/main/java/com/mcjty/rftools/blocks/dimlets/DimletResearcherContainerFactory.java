package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.item.ItemStack;

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
        layoutBuffer();
        layoutPlayerInventorySlots(10, 70);
    }

    private void layoutBuffer() {
        // Input slots
        int leftCol = 64;
        int topRow = 24;
        addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.unknownDimlet)), CONTAINER_INVENTORY, SLOT_DIMLETINPUT, leftCol, topRow, 1, 18, 1, 18);

        // Output slots
        leftCol = 118;
        topRow = 24;
        addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_DIMLETOUTPUT, leftCol, topRow, 1, 18, 1, 18);
    }
}

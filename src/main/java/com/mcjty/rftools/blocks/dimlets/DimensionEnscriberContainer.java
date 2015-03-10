package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import com.mcjty.rftools.Achievements;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DimensionEnscriberContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_DIMLETS = 0;
    public static final int SIZE_DIMLETS = 13*7;
    public static final int SLOT_TAB = SLOT_DIMLETS + SIZE_DIMLETS;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.knownDimlet)), CONTAINER_INVENTORY, SLOT_DIMLETS, 13, 6, 13, 18, 7, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_TAB, 13, 142, 1, 18, 1, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };

    public DimensionEnscriberContainer(EntityPlayer player, DimensionEnscriberTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        if (index == SLOT_TAB) {
            Achievements.trigger(player, Achievements.firstDimension);
        }
        return super.slotClick(index, button, mode, player);
    }
}

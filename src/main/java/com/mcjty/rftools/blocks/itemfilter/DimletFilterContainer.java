package com.mcjty.rftools.blocks.itemfilter;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DimletFilterContainer extends GenericContainer {

    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_BUFFER = 0;
    public static final int BUFFER_SIZE = 9;
    public static final int SLOT_PLAYERINV = BUFFER_SIZE;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.knownDimlet)), CONTAINER_INVENTORY, SLOT_BUFFER, 24, 105, 9, 18, 1, 18);
            layoutPlayerInventorySlots(24, 130);
        }
    };

    public DimletFilterContainer(EntityPlayer player, DimletFilterTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}

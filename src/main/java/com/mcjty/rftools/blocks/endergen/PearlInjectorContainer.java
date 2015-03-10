package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class PearlInjectorContainer extends GenericContainer {

    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_BUFFER = 0;
    public static final int BUFFER_SIZE = (9*2);
    public static final int SLOT_PLAYERINV = SLOT_BUFFER + BUFFER_SIZE;
//    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(Items.ender_pearl)), CONTAINER_INVENTORY, SLOT_BUFFER, 10, 25, 9, 18, 2, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public PearlInjectorContainer(EntityPlayer player, PearlInjectorTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}

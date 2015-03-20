package com.mcjty.rftools.blocks.spawner;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class SpawnerContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_SYRINGE = 0;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.syringeItem)), CONTAINER_INVENTORY, SLOT_SYRINGE, 28, 8, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public SpawnerContainer(EntityPlayer player, SpawnerTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}

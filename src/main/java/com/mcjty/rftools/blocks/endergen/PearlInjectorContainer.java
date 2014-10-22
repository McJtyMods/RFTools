package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;

public class PearlInjectorContainer extends GenericContainer {

    public PearlInjectorContainer(EntityPlayer player, PearlInjectorTileEntity containerInventory) {
        super(PearlInjectorContainerFactory.getInstance(), player);
        addInventory(PearlInjectorContainerFactory.CONTAINER_INVENTORY, containerInventory);
        addInventory(PearlInjectorContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}

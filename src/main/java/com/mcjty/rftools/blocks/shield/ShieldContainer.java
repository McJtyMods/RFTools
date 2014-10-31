package com.mcjty.rftools.blocks.shield;

import com.mcjty.container.GenericEnergyHandlerContainer;
import net.minecraft.entity.player.EntityPlayer;

public class ShieldContainer extends GenericEnergyHandlerContainer {

    public ShieldContainer(EntityPlayer player, ShieldTileEntity containerInventory) {
        super(ShieldContainerFactory.getInstance(), player, containerInventory);
        addInventory(ShieldContainerFactory.CONTAINER_INVENTORY, containerInventory);
        addInventory(ShieldContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}

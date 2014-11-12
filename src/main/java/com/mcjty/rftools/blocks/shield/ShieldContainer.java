package com.mcjty.rftools.blocks.shield;

import com.mcjty.container.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;

public class ShieldContainer extends GenericContainer {

    public ShieldContainer(EntityPlayer player, ShieldTileEntity containerInventory) {
        super(ShieldContainerFactory.getInstance(), player);
        addInventory(ShieldContainerFactory.CONTAINER_INVENTORY, containerInventory);
        addInventory(ShieldContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}

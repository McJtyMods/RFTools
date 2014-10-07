package com.mcjty.container;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Use this in case you want a container with no slots (for example, for energy storage only).
 */
public class EmptyContainer<T extends GenericEnergyHandlerTileEntity> extends GenericEnergyHandlerContainer {

    public EmptyContainer(EntityPlayer player, T tileEntity) {
        super(EmptyContainerFactory.getInstance(), player, tileEntity);
    }
}

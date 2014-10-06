package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.GenericEnergyHandlerContainer;
import net.minecraft.entity.player.EntityPlayer;

public class MatterTransmitterContainer extends GenericEnergyHandlerContainer {

    public MatterTransmitterContainer(EntityPlayer player, MatterTransmitterTileEntity matterTransmitterTileEntity) {
        super(MatterTransmitterContainerFactory.getInstance(), player, matterTransmitterTileEntity);
    }
}

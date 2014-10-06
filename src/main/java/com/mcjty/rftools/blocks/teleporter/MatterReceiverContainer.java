package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.GenericEnergyHandlerContainer;
import net.minecraft.entity.player.EntityPlayer;

public class MatterReceiverContainer extends GenericEnergyHandlerContainer {

    public MatterReceiverContainer(EntityPlayer player, MatterReceiverTileEntity matterReceiverTileEntity) {
        super(MatterTransmitterContainerFactory.getInstance(), player, matterReceiverTileEntity);
    }
}

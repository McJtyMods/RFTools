package com.mcjty.container;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class GenericContainerBlock extends GenericBlock {

    protected GenericContainerBlock(Material material, Class<? extends TileEntity> tileEntityClass) {
        super(material, tileEntityClass);
        this.isBlockContainer = true;
    }

    @Override
    public boolean onBlockEventReceived(World world, int x, int y, int z, int eventId, int eventData) {
        super.onBlockEventReceived(world, x, y, z, eventId, eventData);
        TileEntity tileentity = world.getTileEntity(x, y, z);
        return tileentity != null ? tileentity.receiveClientEvent(eventId, eventData) : false;
    }
}

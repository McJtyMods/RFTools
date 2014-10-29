package com.mcjty.container;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class GenericContainerBlock extends GenericBlock {

    protected GenericContainerBlock(Material material, Class<? extends TileEntity> tileEntityClass) {
        super(material, tileEntityClass);
        this.isBlockContainer = true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        world.removeTileEntity(x, y, z);
    }

    @Override
    public boolean onBlockEventReceived(World world, int x, int y, int z, int eventId, int eventData) {
        super.onBlockEventReceived(world, x, y, z, eventId, eventData);
        TileEntity tileentity = world.getTileEntity(x, y, z);
        return tileentity != null ? tileentity.receiveClientEvent(eventId, eventData) : false;
    }


    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!tileEntityClass.isInstance(te)) {
            return true;
        }
        if (world.isRemote) {
            return true;
        }
        player.openGui(RFTools.instance, getGuiID(), world, x, y, z);
        return true;
    }
}

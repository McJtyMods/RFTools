package com.mcjty.rftools.items.netmonitor;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkMonitorItem extends Item {
    private ConcurrentHashMap<Coordinate,BlockInfo> connectedBlocks = new ConcurrentHashMap<Coordinate, BlockInfo>();

    public NetworkMonitorItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    private void findConnectedBlocks(ConcurrentHashMap<Coordinate,BlockInfo> connectedBlocks, World world, int x, int y, int z, boolean first) {
        if (y < 0 || y >= world.getActualHeight()) {
            return;
        }
        Coordinate c = new Coordinate(x, y, z);
        if (connectedBlocks.containsKey(c)) {
            return;
        }
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            if (tileEntity instanceof IEnergyHandler) {
                Block block = world.getBlock(x, y, z);
                connectedBlocks.put(c, new BlockInfo(tileEntity, block, world.getBlockMetadata(x, y, z), c, first));
                findConnectedBlocks(connectedBlocks, world, x + 1, y, z, false);
                findConnectedBlocks(connectedBlocks, world, x - 1, y, z, false);
                findConnectedBlocks(connectedBlocks, world, x, y - 1, z, false);
                findConnectedBlocks(connectedBlocks, world, x, y + 1, z, false);
                findConnectedBlocks(connectedBlocks, world, x, y, z - 1, false);
                findConnectedBlocks(connectedBlocks, world, x, y, z + 1, false);
            }
        }
    }

    synchronized public ConcurrentHashMap<Coordinate, BlockInfo> getConnectedBlocks() {
        return connectedBlocks;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_LIST_BLOCKS, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return true;
        } else {
            connectedBlocks.clear();
            findConnectedBlocks(connectedBlocks, world, x, y, z, true);
        }
        return true;
    }
}
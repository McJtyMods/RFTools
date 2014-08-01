package com.mcjty.rftools.items;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashMap;
import java.util.Map;

public class NetworkMonitorItem extends Item {

    private class Coordinate {
        int x, y, z;

        private Coordinate(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Coordinate that = (Coordinate) o;

            if (x != that.x) return false;
            if (y != that.y) return false;
            if (z != that.z) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }

    private class BlockInfo {
        TileEntity tileEntity;
        Block block;

        private BlockInfo(TileEntity tileEntity, Block block) {
            this.tileEntity = tileEntity;
            this.block = block;
        }
    }

    public NetworkMonitorItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    private void findConnectedBlocks(HashMap<Coordinate,BlockInfo> connectedBlocks, World world, int x, int y, int z) {
        if (y < 0 || y >= world.getActualHeight()) {
            return;
        }
        Coordinate c = new Coordinate(x, y, z);
        if (connectedBlocks.containsKey(c)) {
            return;
        }
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            Block block  = world.getBlock(x, y, z);
            connectedBlocks.put(c, new BlockInfo(tileEntity, block));
            findConnectedBlocks(connectedBlocks, world, x+1, y, z);
            findConnectedBlocks(connectedBlocks, world, x - 1, y, z);
            findConnectedBlocks(connectedBlocks, world, x, y-1, z);
            findConnectedBlocks(connectedBlocks, world, x, y + 1, z);
            findConnectedBlocks(connectedBlocks, world, x, y, z - 1);
            findConnectedBlocks(connectedBlocks, world, x, y, z+1);
        }
    }


    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_LIST_BLOCKS, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
        }

        if (!world.isRemote) {
            System.out.println("==========================================");
            HashMap<Coordinate,BlockInfo> connectedBlocks = new HashMap<Coordinate, BlockInfo>();
            findConnectedBlocks(connectedBlocks, world, x, y, z);
            for (Map.Entry<Coordinate,BlockInfo> me : connectedBlocks.entrySet()) {
                showBlockInfo(me.getKey(), me.getValue());
            }
        }
        return true;
    }

    private void showBlockInfo(Coordinate c, BlockInfo blockInfo) {
        System.out.print("Block: " + blockInfo.block.getUnlocalizedName() + " at " + c.x + "," + c.y + "," + c.z);
        IEnergyHandler handler = (IEnergyHandler) blockInfo.tileEntity;
        if (handler != null) {
            System.out.println("  Energy:" + handler.getEnergyStored(ForgeDirection.DOWN) + " (" + handler.getMaxEnergyStored(ForgeDirection.DOWN) + ")");
        } else {
            System.out.println();
        }
//        showTileEntityInfo(blockInfo.tileEntity);
    }

    private void showTileEntityInfo(TileEntity tileEntity) {
        if (tileEntity != null) {
            if (tileEntity instanceof IEnergyHandler) {
                System.out.println("    Tile entity is an energy handler");
                IEnergyHandler handler = (IEnergyHandler) tileEntity;
                checkConnections(handler);
                checkEnergyStored(handler);
            }
            else if (tileEntity instanceof IEnergyConnection) {
                System.out.println("    Tile entity is an energy connection");
                IEnergyConnection connection = (IEnergyConnection) tileEntity;
                checkConnections(connection);
            }
            if (tileEntity instanceof IEnergyStorage) {
                System.out.println("    Tile entity is an energy storage");
            }

        }
    }

    private void checkConnections(IEnergyConnection connection) {
        if (connection.canConnectEnergy(ForgeDirection.DOWN)) {
            System.out.println("        Can connect from side DOWN");
        }
        if (connection.canConnectEnergy(ForgeDirection.UP)) {
            System.out.println("        Can connect from side UP");
        }
        if (connection.canConnectEnergy(ForgeDirection.EAST)) {
            System.out.println("        Can connect from side EAST");
        }
        if (connection.canConnectEnergy(ForgeDirection.WEST)) {
            System.out.println("        Can connect from side WEST");
        }
        if (connection.canConnectEnergy(ForgeDirection.NORTH)) {
            System.out.println("        Can connect from side NORTH");
        }
        if (connection.canConnectEnergy(ForgeDirection.SOUTH)) {
            System.out.println("        Can connect from side SOUTH");
        }
    }

    private void checkEnergyStored(IEnergyHandler handler) {
        System.out.println("        Energy stored DOWN: " + handler.getEnergyStored(ForgeDirection.DOWN) + " (" + handler.getMaxEnergyStored(ForgeDirection.DOWN));
        System.out.println("        Energy stored UP: " + handler.getEnergyStored(ForgeDirection.UP) + " (" + handler.getMaxEnergyStored(ForgeDirection.UP));
        System.out.println("        Energy stored EAST: " + handler.getEnergyStored(ForgeDirection.EAST) + " (" + handler.getMaxEnergyStored(ForgeDirection.EAST));
        System.out.println("        Energy stored WEST: " + handler.getEnergyStored(ForgeDirection.WEST) + " (" + handler.getMaxEnergyStored(ForgeDirection.WEST));
        System.out.println("        Energy stored NORTH: " + handler.getEnergyStored(ForgeDirection.NORTH) + " (" + handler.getMaxEnergyStored(ForgeDirection.NORTH));
        System.out.println("        Energy stored SOUTH: " + handler.getEnergyStored(ForgeDirection.SOUTH) + " (" + handler.getMaxEnergyStored(ForgeDirection.SOUTH));
    }
}
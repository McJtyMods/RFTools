package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.BlockInfo;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

public class StorageMonitorTileEntity extends GenericEnergyHandlerTileEntity {
    public static final int MAXENERGY = 100000;
    public static final int RECEIVEPERTICK = 100;


    public StorageMonitorTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    private void scan(int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = x1 ; x <= x2 ; x++) {
            for (int y = y1 ; y <= y2 ; y++) {
                for (int z = z1 ; z <= z2 ; z++) {
                    TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
                    if (tileEntity instanceof IInventory) {
                        IInventory inventory = (IInventory) tileEntity;
                        if (inventory.getSizeInventory() > 0) {
                            Block block = worldObj.getBlock(x, y, z);
                            System.out.print("Block: " + BlockInfo.getReadableName(block, worldObj.getBlockMetadata(x, y, z)));
                            System.out.println(", invsize:" + inventory.getSizeInventory());
                        }
                    }
                }
            }
        }
    }

    public void startScan(int radius) {
        int x1 = xCoord - radius;
        int y1 = yCoord - radius;
        int z1 = zCoord - radius;
        int x2 = xCoord + radius;
        int y2 = yCoord + radius;
        int z2 = zCoord + radius;
        if (y1 < 0) {
            y1 = 0;
        }
        if (y2 >= worldObj.getActualHeight()) {
            y2 = worldObj.getActualHeight()-1;
        }
        scan(x1, y1, z1, x2, y2, z2);
    }

    public void stopScan() {

    }
}

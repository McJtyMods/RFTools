package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.entity.SyncedValue;
import com.mcjty.rftools.BlockInfo;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class StorageMonitorTileEntity extends GenericEnergyHandlerTileEntity {
    public static final int MAXENERGY = 100000;
    public static final int RECEIVEPERTICK = 100;
    public static int rfPerOperation = 100;

    // Serverside
    private SyncedValue<Boolean> scanning = new SyncedValue<Boolean>(false);
    private SyncedValue<Integer> x1 = new SyncedValue<Integer>(0);
    private SyncedValue<Integer> y1 = new SyncedValue<Integer>(0);
    private SyncedValue<Integer> z1 = new SyncedValue<Integer>(0);
    private SyncedValue<Integer> x2 = new SyncedValue<Integer>(0);
    private SyncedValue<Integer> y2 = new SyncedValue<Integer>(0);
    private SyncedValue<Integer> z2 = new SyncedValue<Integer>(0);
    private SyncedValue<Integer> curx = new SyncedValue<Integer>(0);
    private SyncedValue<Integer> cury = new SyncedValue<Integer>(0);
    private SyncedValue<Integer> curz = new SyncedValue<Integer>(0);

    public StorageMonitorTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
        registerSyncedValue(scanning);
        registerSyncedValue(x1);
        registerSyncedValue(y1);
        registerSyncedValue(z1);
        registerSyncedValue(x2);
        registerSyncedValue(y2);
        registerSyncedValue(z2);
        registerSyncedValue(curx);
        registerSyncedValue(cury);
        registerSyncedValue(curz);
    }

    public void startScan(int radius) {
        if (!worldObj.isRemote) {
            // Only on server
            x1.setValue(xCoord - radius);
            y1.setValue(yCoord - radius);
            z1.setValue(zCoord - radius);
            x2.setValue(xCoord + radius);
            y2.setValue(yCoord + radius);
            z2.setValue(zCoord + radius);
            if (y1.getValue() < 0) {
                y1.setValue(0);
            }
            if (y2.getValue() >= worldObj.getActualHeight()) {
                y2.setValue(worldObj.getActualHeight()-1);
            }
            scanning.setValue(true);
            curx.setValue(x1.getValue());
            cury.setValue(y1.getValue());
            curz.setValue(z1.getValue());
        }
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();
        if (scanning.getValue()) {
            TileEntity tileEntity = worldObj.getTileEntity(curx.getValue(), cury.getValue(), curz.getValue());
            if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                if (inventory.getSizeInventory() > 0) {
                    Block block = worldObj.getBlock(curx.getValue(), cury.getValue(), curz.getValue());
                    System.out.print("Block: " + BlockInfo.getReadableName(block, worldObj.getBlockMetadata(curx.getValue(), cury.getValue(), curz.getValue())));
                    System.out.println(", invsize:" + inventory.getSizeInventory());
                }
            }
            curx.setValue(curx.getValue()+1);
            if (curx.getValue() > x2.getValue()) {
                curx.setValue(x1.getValue());
                cury.setValue(cury.getValue()+1);
                if (cury.getValue() > y2.getValue()) {
                    cury.setValue(y1.getValue());
                    curz.setValue(curz.getValue()+1);
                    if (curz.getValue() > z2.getValue()) {
                        scanning.setValue(false);
                        // DONE!
                    }
                }
            }
        }
    }

    // Client side
    public void progressScan(int bx, int by, int bz, int sizeInventory) {

    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        scanning.setValue(tagCompound.getBoolean("scanning"));
        x1.setValue(tagCompound.getInteger("x1"));
        y1.setValue(tagCompound.getInteger("y1"));
        z1.setValue(tagCompound.getInteger("z1"));
        x2.setValue(tagCompound.getInteger("x2"));
        y2.setValue(tagCompound.getInteger("y2"));
        z2.setValue(tagCompound.getInteger("z2"));
        curx.setValue(tagCompound.getInteger("curx"));
        cury.setValue(tagCompound.getInteger("cury"));
        curz.setValue(tagCompound.getInteger("curz"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("scanning", scanning.getValue());
        tagCompound.setInteger("x1", x1.getValue());
        tagCompound.setInteger("y1", y1.getValue());
        tagCompound.setInteger("z1", z1.getValue());
        tagCompound.setInteger("x2", x2.getValue());
        tagCompound.setInteger("y2", y2.getValue());
        tagCompound.setInteger("z2", z2.getValue());
        tagCompound.setInteger("curx", curx.getValue());
        tagCompound.setInteger("cury", cury.getValue());
        tagCompound.setInteger("curz", curz.getValue());
    }
}

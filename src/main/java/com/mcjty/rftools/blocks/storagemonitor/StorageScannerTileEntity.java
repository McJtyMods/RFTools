package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.entity.SyncedCoordinate;
import com.mcjty.entity.SyncedValue;
import com.mcjty.entity.SyncedValueList;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class StorageScannerTileEntity extends GenericEnergyHandlerTileEntity {
    public static final int MAXENERGY = 100000;
    public static final int RECEIVEPERTICK = 100;
    public static int rfPerOperation = 100;
    public static int scansPerOperation = 5;

    private SyncedValue<Integer> radius = new SyncedValue<Integer>(1);
    private SyncedValue<Boolean> scanning = new SyncedValue<Boolean>(false);
    private SyncedCoordinate c1 = new SyncedCoordinate(Coordinate.INVALID);
    private SyncedCoordinate c2 = new SyncedCoordinate(Coordinate.INVALID);
    private SyncedCoordinate cur = new SyncedCoordinate(Coordinate.INVALID);
    private SyncedValueList<InvBlockInfo> inventories = new SyncedValueList<InvBlockInfo>() {
        @Override
        public InvBlockInfo readElementFromNBT(NBTTagCompound tagCompound) {
            return InvBlockInfo.readFromNBT(tagCompound);
        }

        @Override
        public NBTTagCompound writeElementToNBT(InvBlockInfo element) {
            return element.writeToNBT();
        }
    };

    public StorageScannerTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
        registerSyncedObject(scanning);
        registerSyncedObject(c1);
        registerSyncedObject(c2);
        registerSyncedObject(cur);
        registerSyncedObject(inventories);
    }

    public void startScan(boolean start) {
        if (!worldObj.isRemote) {
            if (!start) {
                scanning.setValue(false);
                notifyBlockUpdate();
                return;
            }

            int r = radius.getValue();
            System.out.println("radius = " + r);
            // Only on server
            int y1 = yCoord-r;
            if (y1 < 0) {
                y1 = 0;
            }
            c1.setCoordinate(new Coordinate(xCoord-r, y1, zCoord-r));
            int y2 = yCoord+r;
            if (y2 >= worldObj.getActualHeight()) {
                y2 = worldObj.getActualHeight()-1;
            }
            c2.setCoordinate(new Coordinate(xCoord+r, y2, zCoord+r));

            scanning.setValue(true);
            cur.setCoordinate(c1.getCoordinate());

            inventories.clear();
            notifyBlockUpdate();
        }
    }

    public int getRadius() {
        return radius.getValue();
    }

    public void setRadius(int v) {
        radius.setValue(v);
        notifyBlockUpdate();
    }

    public boolean isScanning() {
        return scanning.getValue();
    }

    public int getProgress() {
        int z1 = c1.getCoordinate().getZ();
        int z2 = c2.getCoordinate().getZ();
        int z = cur.getCoordinate().getZ();
        return (z-z1) * 100 / (z2-z1);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();
        if (scanning.getValue()) {
            if (getEnergyStored(ForgeDirection.DOWN) < rfPerOperation) {
                return;
            }
            extractEnergy(ForgeDirection.DOWN, rfPerOperation, false);

            for (int i = 0 ; i < scansPerOperation ; i++) {
                Coordinate c = cur.getCoordinate();
                checkInventoryStatus(c.getX(), c.getY(), c.getZ());
                if (!advanceCurrent()) {
                    return;
                }
            }
        }
    }

    private void checkInventoryStatus(int cx, int cy, int cz) {
        TileEntity tileEntity = worldObj.getTileEntity(cx, cy, cz);
        if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            if (inventory.getSizeInventory() > 0) {
                Block block = worldObj.getBlock(cx, cy, cz);
                System.out.print("Block: " + BlockInfo.getReadableName(block, worldObj.getBlockMetadata(cx, cy, cz)));
                System.out.println(", invsize:" + inventory.getSizeInventory());
                inventories.add(new InvBlockInfo(new Coordinate(cx, cy, cz), inventory.getSizeInventory()));
                notifyBlockUpdate();                }
        }
    }

    // Advance the 'cur' index to the next block. Return false when done.
    // When done 'scanning' will be set to false as well.
    private boolean advanceCurrent() {
        Coordinate c = cur.getCoordinate();
        int cx = c.getX();
        int cy = c.getY();
        int cz = c.getZ();
        cx++;
        Coordinate lo = c1.getCoordinate();
        Coordinate up = c2.getCoordinate();
        if (cx > up.getX()) {
            cx = lo.getX();
            cy++;
            if (cy > up.getY()) {
                cy = lo.getY();
                cz++;
                if (cz > up.getZ()) {
                    scanning.setValue(false);
                    notifyBlockUpdate();
                    return false;
                }
            }
        }
        cur.setCoordinate(new Coordinate(cx, cy, cz));
        notifyBlockUpdate();
        return true;
    }

    public SyncedValueList<InvBlockInfo> getInventories() {
        return inventories;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        radius.setValue(tagCompound.getInteger("radius"));
        scanning.setValue(tagCompound.getBoolean("scanning"));
        c1.readFromNBT(tagCompound, "c1");
        c2.readFromNBT(tagCompound, "c2");
        cur.readFromNBT(tagCompound, "cur");
        inventories.readFromNBT(tagCompound, "inv");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("radius", radius.getValue());
        tagCompound.setBoolean("scanning", scanning.getValue());
        c1.writeToNBT(tagCompound, "c1");
        c2.writeToNBT(tagCompound, "c2");
        cur.writeToNBT(tagCompound, "cur");
        inventories.writeToNBT(tagCompound, "inv");
    }
}

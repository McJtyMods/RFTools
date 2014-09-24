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

public class StorageMonitorTileEntity extends GenericEnergyHandlerTileEntity {
    public static final int MAXENERGY = 100000;
    public static final int RECEIVEPERTICK = 100;
    public static int rfPerOperation = 100;

    // Serverside
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

    public StorageMonitorTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
        registerSyncedObject(scanning);
        registerSyncedObject(c1);
        registerSyncedObject(c2);
        registerSyncedObject(cur);
        registerSyncedObject(inventories);
    }

    public void startScan(int radius) {
        if (!worldObj.isRemote) {
            System.out.println("radius = " + radius);
            // Only on server
            int y1 = yCoord-radius;
            if (y1 < 0) {
                y1 = 0;
            }
            c1.setCoordinate(new Coordinate(xCoord-radius, y1, zCoord-radius));
            int y2 = yCoord+radius;
            if (y2 >= worldObj.getActualHeight()) {
                y2 = worldObj.getActualHeight()-1;
            }
            c2.setCoordinate(new Coordinate(xCoord+radius, y2, zCoord+radius));

            scanning.setValue(true);
            cur.setCoordinate(c1.getCoordinate());
        }
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();
        if (scanning.getValue()) {
            Coordinate c = cur.getCoordinate();
            int cx = c.getX();
            int cy = c.getY();
            int cz = c.getZ();
            System.out.print("cx = " + cx);
            System.out.print(", cy = " + cy);
            System.out.println(", cz = " + cz);


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
                        // DONE!
                    }
                }
            }
            cur.setCoordinate(new Coordinate(cx, cy, cz));
        }
    }

    public SyncedValueList<InvBlockInfo> getInventories() {
        return inventories;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        scanning.setValue(tagCompound.getBoolean("scanning"));
        c1.readFromNBT(tagCompound, "c1");
        c2.readFromNBT(tagCompound, "c2");
        cur.readFromNBT(tagCompound, "cur");
        inventories.readFromNBT(tagCompound, "inv");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("scanning", scanning.getValue());
        c1.writeToNBT(tagCompound, "c1");
        c2.writeToNBT(tagCompound, "c2");
        cur.writeToNBT(tagCompound, "cur");
        inventories.writeToNBT(tagCompound, "inv");
    }
}

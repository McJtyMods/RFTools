package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.entity.SyncedCoordinate;
import com.mcjty.entity.SyncedValue;
import com.mcjty.entity.SyncedValueList;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class StorageScannerTileEntity extends GenericEnergyHandlerTileEntity {

    public static final String CMD_SETRADIUS = "setRadius";
    public static final String CMD_STARTSCAN = "startScan";
    public static final String CMD_STARTSEARCH = "startSearch";
    public static final String CLIENTCMD_SEARCHREADY = "searchReady";

    // For client side: the items of the inventory we're currently looking at.
    private List<ItemStack> showingItems = new ArrayList<ItemStack> ();

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
        super(StorageScannerConfiguration.MAXENERGY, StorageScannerConfiguration.RECEIVEPERTICK);
        registerSyncedObject(scanning);
        registerSyncedObject(c1);
        registerSyncedObject(c2);
        registerSyncedObject(cur);
        registerSyncedObject(inventories);
    }

    public List<Coordinate> startSearch(String search) {
        search = search.toLowerCase();
        List<Coordinate> output = new ArrayList<Coordinate>();
        for (InvBlockInfo invBlockInfo : inventories) {
            Coordinate c = invBlockInfo.getCoordinate();
            TileEntity tileEntity = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
            if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
                    ItemStack itemStack = inventory.getStackInSlot(i);
                    if (itemStack != null) {
                        String readableName = itemStack.getDisplayName();
                        if (readableName.toLowerCase().contains(search)) {
                            output.add(c);
                            break;
                        }
                    }
                }
            }
        }
        return output;
    }

    public void startScan(boolean start) {
        if (!worldObj.isRemote) {
            if (!start) {
                scanning.setValue(false);
                notifyBlockUpdate();
                return;
            }

            int r = radius.getValue();
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
            int rf = StorageScannerConfiguration.rfPerOperation;
            rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);

            if (getEnergyStored(ForgeDirection.DOWN) < rf) {
                return;
            }
            extractEnergy(ForgeDirection.DOWN, rf, false);

            int scans = StorageScannerConfiguration.scansPerOperation;

            scans = scans * (int) (getInfusedFactor() + 1.01f);

            for (int i = 0 ; i < scans; i++) {
                Coordinate c = cur.getCoordinate();
                checkInventoryStatus(c.getX(), c.getY(), c.getZ());
                if (!advanceCurrent()) {
                    return;
                }
            }
        }
    }

    public void storeItemsForClient(List<ItemStack> items) {
        showingItems = new ArrayList<ItemStack>(items);
    }

    public List<ItemStack> getShowingItems() {
        return showingItems;
    }

    public void clearShowingItems() {
        showingItems.clear();
    }

    public List<ItemStack> getInventoryForBlock(int cx, int cy, int cz) {
        showingItems = new ArrayList<ItemStack>();

        if (getEnergyStored(ForgeDirection.DOWN) < StorageScannerConfiguration.rfPerOperation) {
            return showingItems;
        }
        extractEnergy(ForgeDirection.DOWN, StorageScannerConfiguration.rfPerOperation, false);

        TileEntity tileEntity = worldObj.getTileEntity(cx, cy, cz);
        if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (itemStack != null) {
                    showingItems.add(itemStack);
                }
            }
        }
        return showingItems;
    }

    private void checkInventoryStatus(int cx, int cy, int cz) {
        TileEntity tileEntity = worldObj.getTileEntity(cx, cy, cz);
        if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            if (inventory.getSizeInventory() > 0) {
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
        scanning.setValue(tagCompound.getBoolean("scanning"));
        c1.readFromNBT(tagCompound, "c1");
        c2.readFromNBT(tagCompound, "c2");
        cur.readFromNBT(tagCompound, "cur");
        inventories.readFromNBT(tagCompound, "inv");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        radius.setValue(tagCompound.getInteger("radius"));
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

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("radius", radius.getValue());
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETRADIUS.equals(command)) {
            setRadius(args.get("r").getInteger());
            return true;
        } else if (CMD_STARTSCAN.equals(command)) {
            startScan(args.get("start").getBoolean());
            return true;
        }
        return false;
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_STARTSEARCH.equals(command)) {
            return startSearch(args.get("search").getString());
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_SEARCHREADY.equals(command)) {
            GuiStorageScanner.fromServer_coordinates = new HashSet<Coordinate>(list);
            return true;
        }
        return false;
    }
}

package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class StorageScannerTileEntity extends GenericEnergyReceiverTileEntity {

    public static final String CMD_SETRADIUS = "setRadius";
    public static final String CMD_STARTSEARCH = "startSearch";
    public static final String CLIENTCMD_SEARCHREADY = "searchReady";

    // For client side: the items of the inventory we're currently looking at.
    private List<ItemStack> showingItems = new ArrayList<ItemStack> ();

    private Integer radius = 1;

    public StorageScannerTileEntity() {
        super(StorageScannerConfiguration.MAXENERGY, StorageScannerConfiguration.RECEIVEPERTICK);
    }

    public List<BlockPos> startSearch(String search) {
        List<BlockPos> inventories = findInventories();
        search = search.toLowerCase();
        List<BlockPos> output = new ArrayList<>();
        for (BlockPos c : inventories) {
            TileEntity tileEntity = worldObj.getTileEntity(c);
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int v) {
        radius = v;
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

    public List<BlockPos> findInventories() {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = getPos().getX() - radius ; x <= getPos().getX() + radius ; x++) {
            for (int z = getPos().getZ() - radius ; z <= getPos().getZ() + radius ; z++) {
                for (int y = getPos().getY() - radius ; y <= getPos().getY() + radius ; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    TileEntity te = worldObj.getTileEntity(p);
                    if (te instanceof IInventory) {
                        // @todo IItemHandler
                        positions.add(p);
                    }
                }
            }
        }
        return positions;
    }

    public List<ItemStack> getInventoryForBlock(BlockPos cpos) {
        showingItems = new ArrayList<>();

        if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerOperation) {
            return showingItems;
        }
        consumeEnergy(StorageScannerConfiguration.rfPerOperation);

        TileEntity tileEntity = worldObj.getTileEntity(cpos);
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

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        radius = tagCompound.getInteger("radius");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("radius", radius);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETRADIUS.equals(command)) {
            setRadius(args.get("r").getInteger());
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
            GuiStorageScanner.fromServer_coordinates = new HashSet<>(list);
            return true;
        }
        return false;
    }
}

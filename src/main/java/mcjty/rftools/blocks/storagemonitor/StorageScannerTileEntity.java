package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class StorageScannerTileEntity extends GenericEnergyReceiverTileEntity {

    public static final String CMD_SETRADIUS = "setRadius";

    private List<BlockPos> inventories = new ArrayList<>();
    private Integer radius = 1;

    public StorageScannerTileEntity() {
        super(StorageScannerConfiguration.MAXENERGY, StorageScannerConfiguration.RECEIVEPERTICK);
    }

    public Set<BlockPos> performSearch(String search) {
        List<BlockPos> inventories = getInventories();
        search = search.toLowerCase();
        Set<BlockPos> output = new HashSet<>();
        for (BlockPos c : inventories) {
            TileEntity tileEntity = worldObj.getTileEntity(c);
            // @todo IItemHandler
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
        markDirty();
    }

    public List<BlockPos> getInventories() {
        return inventories;
    }

    public List<BlockPos> findInventories() {
        inventories.clear();
        for (int x = getPos().getX() - radius ; x <= getPos().getX() + radius ; x++) {
            for (int z = getPos().getZ() - radius ; z <= getPos().getZ() + radius ; z++) {
                for (int y = getPos().getY() - radius ; y <= getPos().getY() + radius ; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    TileEntity te = worldObj.getTileEntity(p);
                    if (te instanceof IInventory) {
                        // @todo IItemHandler
                        inventories.add(p);
                    }
                }
            }
        }
        return inventories;
    }

    public List<ItemStack> getInventoryForBlock(BlockPos cpos) {
        List<ItemStack> showingItems = new ArrayList<>();
        // @todo IItemHandler

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
        NBTTagList list = tagCompound.getTagList("inventories", Constants.NBT.TAG_COMPOUND);
        inventories.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tag = (NBTTagCompound) list.get(i);
            BlockPos c = BlockPosTools.readFromNBT(tag, "c");
            inventories.add(c);
        }

    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        radius = tagCompound.getInteger("radius");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        NBTTagList list = new NBTTagList();
        for (BlockPos c : inventories) {
            NBTTagCompound tag = BlockPosTools.writeToNBT(c);
            list.appendTag(tag);
        }
        tagCompound.setTag("inventories", list);
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
}

package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CustomSidedInvWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class StorageScannerTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final String CMD_SETRADIUS = "setRadius";
    public static final String CMD_REQUESTITEM = "requestItem";
    public static final String CMD_UP = "up";
    public static final String CMD_DOWN = "down";
    public static final String CMD_TOGGLEROUTABLE = "toggleRoutable";

    private List<BlockPos> inventories = new ArrayList<>();
    private Set<BlockPos> routable = new HashSet<>();
    private int radius = 1;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, StorageScannerContainer.factory, 2);

    public StorageScannerTileEntity() {
        super(StorageScannerConfiguration.MAXENERGY, StorageScannerConfiguration.RECEIVEPERTICK);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            if (inventoryHelper.containsItem(StorageScannerContainer.SLOT_IN)) {
                if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerInsert) {
                    return;
                }

                ItemStack stack = inventoryHelper.getStackInSlot(StorageScannerContainer.SLOT_IN);
                for (BlockPos blockPos : inventories) {
                    if (!blockPos.equals(getPos()) && routable.contains(blockPos)) {
                        TileEntity te = worldObj.getTileEntity(blockPos);
                        if (te != null) {
                            stack = InventoryHelper.insertItem(worldObj, blockPos, null, stack);
                            if (stack == null) {
                                break;
                            }
                        }
                    }
                }
                inventoryHelper.setInventorySlotContents(64, StorageScannerContainer.SLOT_IN, stack);

                consumeEnergy(StorageScannerConfiguration.rfPerInsert);
            }
        }
    }

    public int countStack(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        // @todo optimize for modular storage
        int cnt = 0;
        List<BlockPos> inventories = getInventories();
        for (BlockPos c : inventories) {
            TileEntity tileEntity = worldObj.getTileEntity(c);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                for (int i = 0 ; i < capability.getSlots() ; i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    if (ItemStack.areItemStacksEqual(stack, itemStack)) {
                        cnt += itemStack.stackSize;
                    }
                }
            } else if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
                    ItemStack itemStack = inventory.getStackInSlot(i);
                    if (ItemStack.areItemStacksEqual(stack, itemStack)) {
                        cnt += itemStack.stackSize;
                    }
                }
            }
        }
        return cnt;
    }

    public Set<BlockPos> performSearch(String search) {
        List<BlockPos> inventories = getInventories();
        search = search.toLowerCase();
        Set<BlockPos> output = new HashSet<>();
        for (BlockPos c : inventories) {
            TileEntity tileEntity = worldObj.getTileEntity(c);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                for (int i = 0 ; i < capability.getSlots() ; i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    if (itemStack != null) {
                        String readableName = itemStack.getDisplayName();
                        if (readableName.toLowerCase().contains(search)) {
                            output.add(c);
                            break;
                        }
                    }
                }
            } else if (tileEntity instanceof IInventory) {
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
        markDirtyClient();
    }

    public boolean isRoutable(BlockPos p) {
        return routable.contains(p);
    }

    public void toggleRoutable(BlockPos p) {
        if (routable.contains(p)) {
            routable.remove(p);
        } else {
            routable.add(p);
        }
        markDirtyClient();
    }

    public List<BlockPos> getInventories() {
        return inventories;
    }

    private void moveUp(int index) {
        if (index <= 0) {
            return;
        }
        if (index >= inventories.size()) {
            return;
        }
        BlockPos p1 = inventories.get(index-1);
        BlockPos p2 = inventories.get(index);
        inventories.set(index-1, p2);
        inventories.set(index, p1);
        markDirty();
    }

    private void moveDown(int index) {
        if (index < 0) {
            return;
        }
        if (index >= inventories.size()-1) {
            return;
        }
        BlockPos p1 = inventories.get(index);
        BlockPos p2 = inventories.get(index+1);
        inventories.set(index, p2);
        inventories.set(index+1, p1);
        markDirty();
    }

    public List<BlockPos> findInventories() {

        // First remove all inventories that are either out of range or no longer an inventory:
        List<BlockPos> old = inventories;
        Set<BlockPos> oldAdded = new HashSet<>();
        inventories = new ArrayList<>();
        for (BlockPos p : old) {
            if (p.getX() >= getPos().getX() - radius && p.getX() <= getPos().getX() + radius) {
                if (p.getY() >= getPos().getY() - radius && p.getY() <= getPos().getY() + radius) {
                    if (p.getZ() >= getPos().getZ() - radius && p.getZ() <= getPos().getZ() + radius) {
                        TileEntity te = worldObj.getTileEntity(p);
                        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                            inventories.add(p);
                            oldAdded.add(p);
                        } else if (te instanceof IInventory) {
                            inventories.add(p);
                            oldAdded.add(p);
                        }
                    }
                }
            }
        }

        // Now append all inventories that are new.
        for (int x = getPos().getX() - radius ; x <= getPos().getX() + radius ; x++) {
            for (int z = getPos().getZ() - radius ; z <= getPos().getZ() + radius ; z++) {
                for (int y = getPos().getY() - radius ; y <= getPos().getY() + radius ; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!oldAdded.contains(p)) {
                        TileEntity te = worldObj.getTileEntity(p);
                        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                            inventories.add(p);
                        } else if (te instanceof IInventory) {
                            inventories.add(p);
                        }
                    }
                }
            }
        }
        return inventories;
    }


    private void requestItem(BlockPos pos, int slot) {
        if (inventoryHelper.containsItem(StorageScannerContainer.SLOT_OUT)) {
            return;
        }

        TileEntity tileEntity = worldObj.getTileEntity(pos);
        if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            ItemStack item = capability.getStackInSlot(slot);
            if (item != null) {
                if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerRequest) {
                    return;
                }
                consumeEnergy(StorageScannerConfiguration.rfPerRequest);

                ItemStack stack = capability.extractItem(slot, item.stackSize, false);
                setInventorySlotContents(StorageScannerContainer.SLOT_OUT, stack);
            }
        } else if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            ItemStack item = inventory.getStackInSlot(slot);
            if (item != null) {
                if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerRequest) {
                    return;
                }
                consumeEnergy(StorageScannerConfiguration.rfPerRequest);

                ItemStack stack = inventory.decrStackSize(slot, item.stackSize);
                setInventorySlotContents(StorageScannerContainer.SLOT_OUT, stack);
            }
        }
    }

    public List<Pair<ItemStack,Integer>> getInventoryForBlock(BlockPos cpos) {
        List<Pair<ItemStack,Integer>> showingItems = new ArrayList<>();

        TileEntity tileEntity = worldObj.getTileEntity(cpos);
        if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            for (int i = 0 ; i < capability.getSlots() ; i++) {
                ItemStack itemStack = capability.getStackInSlot(i);
                if (itemStack != null) {
                    showingItems.add(Pair.of(itemStack, i));
                }
            }
        } else if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (itemStack != null) {
                    showingItems.add(Pair.of(itemStack, i));
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
        list = tagCompound.getTagList("routable", Constants.NBT.TAG_COMPOUND);
        routable.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tag = (NBTTagCompound) list.get(i);
            BlockPos c = BlockPosTools.readFromNBT(tag, "c");
            routable.add(c);
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
        list = new NBTTagList();
        for (BlockPos c : routable) {
            NBTTagCompound tag = BlockPosTools.writeToNBT(c);
            list.appendTag(tag);
        }
        tagCompound.setTag("routable", list);
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
        } else if (CMD_REQUESTITEM.equals(command)) {
            BlockPos inv = args.get("inv").getCoordinate();
            int slot = args.get("slot").getInteger();
            requestItem(inv, slot);
            return true;
        } else if (CMD_UP.equals(command)) {
            moveUp(args.get("index").getInteger());
            return true;
        } else if (CMD_DOWN.equals(command)) {
            moveDown(args.get("index").getInteger());
            return true;
        } else if (CMD_TOGGLEROUTABLE.equals(command)) {
            toggleRoutable(args.get("pos").getCoordinate());
            return true;
        }
        return false;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return StorageScannerContainer.factory.isOutputSlot(index);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return StorageScannerContainer.factory.isInputSlot(index);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return StorageScannerContainer.factory.getAccessibleSlots();
    }

    IItemHandler invHandler = new CustomSidedInvWrapper(this);

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) invHandler;
        }
        return super.getCapability(capability, facing);
    }

}

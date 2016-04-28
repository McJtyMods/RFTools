package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CustomSidedInvWrapper;
import mcjty.lib.varia.SoundTools;
import mcjty.rftools.api.general.IInventoryTracker;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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
    private Map<Pair<BlockPos, Item>, Pair<Integer, Integer>> cachedCounts = new HashMap<>();
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
                stack = injectStackInternal(stack);
                inventoryHelper.setInventorySlotContents(64, StorageScannerContainer.SLOT_IN, stack);

                consumeEnergy(StorageScannerConfiguration.rfPerInsert);
            }
        }
    }


    public ItemStack injectStack(ItemStack stack, EntityPlayer player) {
        if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerInsert) {
            player.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "Not enough power to insert items!"));
            return stack;
        }
        if (!checkForRoutableInventories()) {
            player.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "There are no routable inventories!"));
            return stack;
        }
        stack = injectStackInternal(stack);
        if (stack == null) {
            consumeEnergy(StorageScannerConfiguration.rfPerInsert);
            SoundTools.playSound(worldObj, SoundEvents.ENTITY_ITEM_PICKUP, getPos().getX(), getPos().getY(), getPos().getZ(), 1.0f, 3.0f);
        }
        return stack;
    }

    private boolean checkForRoutableInventories() {
        for (BlockPos blockPos : inventories) {
            if (!blockPos.equals(getPos()) && routable.contains(blockPos)) {
                TileEntity te = worldObj.getTileEntity(blockPos);
                if (te != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack injectStackInternal(ItemStack stack) {
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
        return stack;
    }

    /**
     * Give a stack matching the input stack to the player containing either a single
     * item or else a full stack
     * @param stack
     * @param single
     * @param player
     */
    public void giveToPlayer(ItemStack stack, boolean single, EntityPlayer player) {
        if (stack == null) {
            return;
        }
        if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerRequest) {
            player.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "Not enough power to request items!"));
            return;
        }
        List<BlockPos> inventories = getInventories();
        final int[] cnt = {single ? 1 : stack.getMaxStackSize()};
        boolean given = false;
        for (BlockPos c : inventories) {
            TileEntity tileEntity = worldObj.getTileEntity(c);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                for (int i = 0 ; i < capability.getSlots() ; i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    if (stack.isItemEqual(itemStack)) {
                        ItemStack received = capability.extractItem(i, cnt[0], false);
                        if (giveItemToPlayer(player, cnt, received)) {
                            given = true;
                        }
                    }
                }
            } else if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
                    ItemStack itemStack = inventory.getStackInSlot(i);
                    if (stack.isItemEqual(itemStack)) {
                        ItemStack received = inventory.decrStackSize(i, cnt[0]);
                        if (giveItemToPlayer(player, cnt, received)) {
                            given = true;
                        }
                    }
                }
            }
        }
        if (given) {
            consumeEnergy(StorageScannerConfiguration.rfPerRequest);
            SoundTools.playSound(worldObj, SoundEvents.ENTITY_ITEM_PICKUP, getPos().getX(), getPos().getY(), getPos().getZ(), 1.0f, 1.0f);
        }
    }

    private boolean giveItemToPlayer(EntityPlayer player, int[] cnt, ItemStack received) {
        if (received != null && cnt[0] > 0) {
            cnt[0] -= received.stackSize;
            giveToPlayer(received, player);
            return true;
        }
        return false;
    }

    private boolean giveToPlayer(ItemStack stack, EntityPlayer player) {
        if (stack == null) {
            return false;
        }
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.entityDropItem(stack, 1.05f);
        }
        return true;
    }

    public int countStack(ItemStack stack, boolean starred) {
        if (stack == null) {
            return 0;
        }
        int cnt = 0;
        List<BlockPos> inventories = getInventories();
        for (BlockPos c : inventories) {
            if ((!starred) || routable.contains(c)) {
                TileEntity tileEntity = worldObj.getTileEntity(c);
                Integer cachedCount = null;
                if (tileEntity instanceof IInventoryTracker) {
                    IInventoryTracker tracker = (IInventoryTracker) tileEntity;
                    Pair<Integer, Integer> pair = cachedCounts.get(Pair.of(c, stack.getItem()));
                    if (pair != null) {
                        Integer oldVersion = pair.getLeft();
                        if (oldVersion == tracker.getVersion()) {
                            cachedCount = pair.getRight();
                        }
                    }
                }
                if (cachedCount != null) {
                    cnt += cachedCount;
                } else {
                    final int[] cc = {0};
                    RFToolsTools.getItems(tileEntity, s -> s.isItemEqual(stack)).forEach(s -> {
                        cc[0] += s.stackSize;
                    });
                    cnt += cc[0];
                    if (tileEntity instanceof IInventoryTracker) {
                        IInventoryTracker tracker = (IInventoryTracker) tileEntity;
                        cachedCounts.put(Pair.of(c, stack.getItem()), Pair.of(tracker.getVersion(), cc[0]));
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
            final String finalSearch = search;
            RFToolsTools.getItems(tileEntity, s -> s.getDisplayName().toLowerCase().contains(finalSearch)).forEach(s -> output.add(c));
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
        // Clear the caches
        cachedCounts.clear();

        // First remove all inventories that are either out of range or no longer an inventory:
        List<BlockPos> old = inventories;
        Set<BlockPos> oldAdded = new HashSet<>();
        inventories = new ArrayList<>();
        for (BlockPos p : old) {
            if (p.getX() >= getPos().getX() - radius && p.getX() <= getPos().getX() + radius) {
                if (p.getY() >= getPos().getY() - radius && p.getY() <= getPos().getY() + radius) {
                    if (p.getZ() >= getPos().getZ() - radius && p.getZ() <= getPos().getZ() + radius) {
                        TileEntity te = worldObj.getTileEntity(p);
                        if (RFToolsTools.isInventory(te)) {
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
                        if (RFToolsTools.isInventory(te)) {
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
        if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerRequest) {
            return;
        }

        TileEntity tileEntity = worldObj.getTileEntity(pos);
        RFToolsTools.handleSlot(tileEntity, slot, s -> {
            consumeEnergy(StorageScannerConfiguration.rfPerRequest);
            setInventorySlotContents(StorageScannerContainer.SLOT_OUT, s);
        });
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

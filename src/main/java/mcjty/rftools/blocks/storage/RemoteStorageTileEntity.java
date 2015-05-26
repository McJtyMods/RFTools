package mcjty.rftools.blocks.storage;

import mcjty.container.InventoryHelper;
import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.items.storage.StorageModuleItem;
import mcjty.rftools.network.Argument;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class RemoteStorageTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory {

    public static final String CMD_SETGLOBAL = "setGlobal";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, RemoteStorageContainer.factory, 8);

    private ItemStack[][] slots = new ItemStack[][] {
            new ItemStack[ModularStorageContainer.MAXSIZE_STORAGE],
            new ItemStack[ModularStorageContainer.MAXSIZE_STORAGE],
            new ItemStack[ModularStorageContainer.MAXSIZE_STORAGE],
            new ItemStack[ModularStorageContainer.MAXSIZE_STORAGE]
    };
    private int[] maxsize = { 0, 0, 0, 0 };
    private int[] numStacks = { 0, 0, 0, 0 };
    private boolean[] global = { false, false, false, false };

    public RemoteStorageTileEntity() {
        super(ModularStorageConfiguration.REMOTE_MAXENERGY, ModularStorageConfiguration.REMOTE_RECEIVEPERTICK);
    }

    private int timer = 0;

    public boolean isPowerLow() {
        return getEnergyStored(ForgeDirection.DOWN) < ModularStorageConfiguration.remoteShareLocal;
    }

    @Override
    protected void checkStateServer() {
        timer--;
        if (timer > 0) {
            return;
        }
        timer = 5;

        int hasPower = isPowerLow() ? 0 : 8;
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int newmeta = (meta & 0x7) | hasPower;
        if (newmeta != meta) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newmeta, 2);
        }

        RemoteStorageIdRegistry registry = RemoteStorageIdRegistry.getRegistry(worldObj);
        for (int i = 0 ; i < 4 ; i++) {
            if (inventoryHelper.containsItem(i)) {
                ItemStack stack = inventoryHelper.getStackInSlot(i);
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound != null && tagCompound.hasKey("id")) {
                    int rf;
                    if (isGlobal(i)) {
                        rf = ModularStorageConfiguration.remoteShareGlobal;
                    } else {
                        rf = ModularStorageConfiguration.remoteShareLocal;
                    }
                    rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);

                    if (getEnergyStored(ForgeDirection.DOWN) < rf) {
                        return;
                    }
                    consumeEnergy(rf);
                    markDirty();

                    int id = tagCompound.getInteger("id");
                    registry.publishStorage(id, new GlobalCoordinate(new Coordinate(xCoord, yCoord, zCoord), worldObj.provider.dimensionId));
                }
            }
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return RemoteStorageContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return RemoteStorageContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return RemoteStorageContainer.factory.isOutputSlot(index);
    }

    public boolean hasStorage(int index) {
        return inventoryHelper.containsItem(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getCount();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    // Compact slots.
    public void compact(int id) {
        int si = findRemoteIndex(id);
        if (si == -1) {
            return;
        }
        ItemStack[] s = findStacksForId(id);
        InventoryHelper.compactStacks(s, 0, maxsize[si]);
        updateStackCount(si);
        markDirty();
    }

    // Find another storage on the same block.
    public int cycle(int id) {
        int si = findRemoteIndex(id);
        if (si == -1) {
            for (int i = 0 ; i < 4 ; i++) {
                ItemStack stack = getStackInSlot(i);
                if (stack != null && stack.getTagCompound() != null && stack.getTagCompound().hasKey("id")) {
                    return stack.getTagCompound().getInteger("id");
                }
            }
            return -1;
        }
        for (int i = si+1 ; i < si + 4 ; i++) {
            int ii = i % 4;
            ItemStack stack = getStackInSlot(ii);
            if (stack != null && stack.getTagCompound() != null && stack.getTagCompound().hasKey("id")) {
                return stack.getTagCompound().getInteger("id");
            }
        }
        return id;
    }

    private void link(int index) {
        if (index >= RemoteStorageContainer.SLOT_LINKER) {
            index -= RemoteStorageContainer.SLOT_LINKER;
        }
        if (!inventoryHelper.containsItem(index)) {
            return;
        }
        if (!inventoryHelper.containsItem(index+4)) {
            return;
        }
        ItemStack source = inventoryHelper.getStackInSlot(index);
        ItemStack dest = inventoryHelper.getStackInSlot(index+4);
        if (dest.getItemDamage() != StorageModuleItem.STORAGE_REMOTE) {
            return;
        }

        NBTTagCompound tagCompound = source.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            source.setTagCompound(tagCompound);
        }
        int id;
        if (tagCompound.hasKey("id")) {
            id = tagCompound.getInteger("id");
        } else {
            RemoteStorageIdRegistry registry = RemoteStorageIdRegistry.getRegistry(worldObj);
            id = registry.getNewId();
            registry.save(worldObj);
            tagCompound.setInteger("id", id);
        }

        tagCompound = dest.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            dest.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("id", id);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < RemoteStorageContainer.SLOT_LINKER) {
            copyFromModule(stack, index);
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        if (!worldObj.isRemote) {
            link(index);
        }
    }

    @Override
    public String getInventoryName() {
        return "Remote Storage Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public int findRemoteIndex(int id) {
        for (int i = 0 ; i < 4 ; i++) {
            if (inventoryHelper.containsItem(i)) {
                ItemStack stack = inventoryHelper.getStackInSlot(i);
                if (stack.getItemDamage() != StorageModuleItem.STORAGE_REMOTE) {
                    NBTTagCompound tagCompound = stack.getTagCompound();
                    if (tagCompound != null && tagCompound.hasKey("id")) {
                        if (id == tagCompound.getInteger("id")) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public boolean isGlobal(int index) {
        return global[index];
    }

    public void setGlobal(int index, boolean global) {
        this.global[index] = global;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public ItemStack[] getRemoteStacks(int si) {
        return slots[si];
    }

    public void updateCount(int si, int cnt) {
        numStacks[si] = cnt;
        StorageModuleItem.updateStackSize(getStackInSlot(si), numStacks[si]);
    }

    public int getCount(int si) {
        return numStacks[si];
    }

    public int getMaxStacks(int si) {
        return maxsize[si];
    }

    public ItemStack decrStackSizeRemote(int si, int index, int amount) {
        if (index >= slots[si].length) {
            return null;
        }
        ItemStack[] stacks = slots[si];
        boolean hasOld = stacks[index] != null;
        ItemStack its = null;
        if (stacks[index] != null) {
            if (stacks[index].stackSize <= amount) {
                ItemStack old = stacks[index];
                stacks[index] = null;
                its = old;
            } else {
                its = stacks[index].splitStack(amount);
                if (stacks[index].stackSize == 0) {
                    stacks[index] = null;
                }
            }
        }

        boolean hasNew = stacks[index] != null;
        if (hasOld && !hasNew) {
            numStacks[si]--;
        } else if (hasNew && !hasOld) {
            numStacks[si]++;
        }
        StorageModuleItem.updateStackSize(getStackInSlot(si), numStacks[si]);

        markDirty();
        return its;
    }

    public boolean updateRemoteSlot(int si, int limit, int index, ItemStack stack) {
        if (index >= slots[si].length) {
            return false;
        }
        boolean hasOld = slots[si][index] != null;
        slots[si][index] = stack;
        if (stack != null && stack.stackSize > limit) {
            stack.stackSize = limit;
        }
        boolean hasNew = stack != null;
        if (hasOld && !hasNew) {
            numStacks[si]--;
        } else if (hasNew && !hasOld) {
            numStacks[si]++;
        }
        StorageModuleItem.updateStackSize(getStackInSlot(si), numStacks[si]);

        markDirty();
        return true;
    }

    public ItemStack findStorageWithId(int id) {
        for (int i = 0 ; i < 4 ; i++) {
            if (inventoryHelper.containsItem(i)) {
                ItemStack stack = inventoryHelper.getStackInSlot(i);
                if (stack.getItemDamage() != StorageModuleItem.STORAGE_REMOTE) {
                    NBTTagCompound tagCompound = stack.getTagCompound();
                    if (tagCompound != null && tagCompound.hasKey("id")) {
                        if (id == tagCompound.getInteger("id")) {
                            return stack;
                        }
                    }
                }
            }
        }
        return null;
    }

    public ItemStack[] findStacksForId(int id) {
        for (int i = 0 ; i < 4 ; i++) {
            if (inventoryHelper.containsItem(i)) {
                ItemStack stack = inventoryHelper.getStackInSlot(i);
                if (stack.getItemDamage() != StorageModuleItem.STORAGE_REMOTE) {
                    NBTTagCompound tagCompound = stack.getTagCompound();
                    if (tagCompound != null && tagCompound.hasKey("id")) {
                        if (id == tagCompound.getInteger("id")) {
                            return slots[i];
                        }
                    }
                }
            }
        }
        return null;
    }


    public void copyToModule(int si) {
        ItemStack stack = inventoryHelper.getStackInSlot(si);
        if (stack == null) {
            // Should be impossible.
            return;
        }
        if (stack.getItemDamage() == StorageModuleItem.STORAGE_REMOTE) {
            return;
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        int cnt = writeSlotsToNBT(tagCompound, "Items", si);
        tagCompound.setInteger("count", cnt);

        for (int i = 0 ; i < ModularStorageContainer.MAXSIZE_STORAGE ; i++) {
            slots[si][i] = null;
        }
    }

    public void copyFromModule(ItemStack stack, int si) {
        for (int i = 0 ; i < ModularStorageContainer.MAXSIZE_STORAGE ; i++) {
            slots[si][i] = null;
        }
        numStacks[si] = 0;

        if (stack == null) {
            setMaxSize(si, 0);
            return;
        }
        if (stack.getItemDamage() == StorageModuleItem.STORAGE_REMOTE) {
            setMaxSize(si, 0);
            return;
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            readSlotsFromNBT(tagCompound, "Items", si);
        }

        setMaxSize(si, StorageModuleItem.MAXSIZE[stack.getItemDamage()]);
        updateStackCount(si);
    }

    private void setMaxSize(int index, int ms) {
        maxsize[index] = ms;
    }

    private void updateStackCount(int si) {
        numStacks[si] = 0;
        ItemStack[] stacks = slots[si];
        for (ItemStack stack : stacks) {
            if (stack != null) {
                numStacks[si]++;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        for (int i = 0 ; i < 4 ; i++) {
            readSlotsFromNBT(tagCompound, "Slots" + i, i);
            maxsize[i] = tagCompound.getInteger("maxSize" + i);
            global[i] = tagCompound.getBoolean("global" + i);
            updateStackCount(i);
        }
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.setStackInSlot(i, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
    }

    private void readSlotsFromNBT(NBTTagCompound tagCompound, String tagname, int index) {
        NBTTagList bufferTagList = tagCompound.getTagList(tagname, Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            slots[index][i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }


    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        for (int i = 0 ; i < 4 ; i++) {
            writeSlotsToNBT(tagCompound, "Slots" + i, i);
            tagCompound.setInteger("maxSize" + i, maxsize[i]);
            tagCompound.setBoolean("global" + i, global[i]);
        }
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    private int writeSlotsToNBT(NBTTagCompound tagCompound, String tagname, int index) {
        NBTTagList bufferTagList = new NBTTagList();
        int cnt = 0;
        for (int i = 0 ; i < slots[index].length ; i++) {
            ItemStack stack = slots[index][i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
                if (stack.stackSize > 0) {
                    cnt++;
                }
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag(tagname, bufferTagList);
        return cnt;
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return rc;
        }
        if (CMD_SETGLOBAL.equals(command)) {
            int index = args.get("index").getInteger();
            boolean global = args.get("global").getBoolean();
            setGlobal(index, global);
            return true;
        }
        return false;
    }
}

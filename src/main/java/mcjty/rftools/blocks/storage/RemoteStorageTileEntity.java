package mcjty.rftools.blocks.storage;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class RemoteStorageTileEntity extends GenericEnergyReceiverTileEntity implements ITickable, DefaultSidedInventory {

    public static final String CMD_SETGLOBAL = "relay.setGlobal";
    public static final Key<Integer> PARAM_INDEX = new Key<>("index", Type.INTEGER);
    public static final Key<Boolean> PARAM_GLOBAL = new Key<>("global", Type.BOOLEAN);

    private InventoryHelper inventoryHelper = new InventoryHelper(this, RemoteStorageContainer.factory, 8);

    private ItemStackList[] slots = new ItemStackList[] {
            ItemStackList.create(0),
            ItemStackList.create(0),
            ItemStackList.create(0),
            ItemStackList.create(0)
    };
    private int[] maxsize = { 0, 0, 0, 0 };
    private int[] numStacks = { 0, 0, 0, 0 };
    private boolean[] global = { false, false, false, false };
    private int version = 0;

    public RemoteStorageTileEntity() {
        super(ModularStorageConfiguration.REMOTE_MAXENERGY, ModularStorageConfiguration.REMOTE_RECEIVEPERTICK);
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    private int timer = 0;

    public boolean isPowerLow() {
        return getEnergyStored() < ModularStorageConfiguration.remoteShareLocal;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        timer--;
        if (timer > 0) {
            return;
        }
        timer = 5;

        int hasPower = isPowerLow() ? 0 : 8;
//        int meta = getWorld().getBlockMetadata(xCoord, yCoord, zCoord);
//        int newmeta = (meta & 0x7) | hasPower;
//        if (newmeta != meta) {
//            getWorld().setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newmeta, 2);
//        }

        RemoteStorageIdRegistry registry = RemoteStorageIdRegistry.getRegistry(getWorld());
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

                    if (getEnergyStored() < rf) {
                        return;
                    }
                    consumeEnergy(rf);
                    markDirty();

                    int id = tagCompound.getInteger("id");
                    registry.publishStorage(id, new GlobalCoordinate(getPos(), getWorld().provider.getDimension()));
                }
            }
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return RemoteStorageContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return RemoteStorageContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return RemoteStorageContainer.factory.isOutputSlot(index);
    }

    public int getVersion() {
        return version;
    }

    public void updateVersion() {
        version++;
        markDirty();
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
        IBlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        return inventoryHelper.decrStackSize(index, amount);
    }

    // Compact slots.
    public void compact(int id) {
        int si = findRemoteIndex(id);
        if (si == -1) {
            return;
        }
        ItemStackList s = findStacksForId(id);
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
                if (!stack.isEmpty() && stack.getTagCompound() != null && stack.getTagCompound().hasKey("id")) {
                    return stack.getTagCompound().getInteger("id");
                }
            }
            return -1;
        }
        for (int i = si+1 ; i < si + 4 ; i++) {
            int ii = i % 4;
            ItemStack stack = getStackInSlot(ii);
            if (!stack.isEmpty() && stack.getTagCompound() != null && stack.getTagCompound().hasKey("id")) {
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
            RemoteStorageIdRegistry registry = RemoteStorageIdRegistry.getRegistry(getWorld());
            id = registry.getNewId();
            registry.save(getWorld());
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
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);

        if (index < RemoteStorageContainer.SLOT_LINKER) {
            copyFromModule(stack, index);
        }

        IBlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        if (!getWorld().isRemote) {
            link(index);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == ModularStorageSetup.storageModuleItem;
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
        markDirtyClient();
    }

    public ItemStackList getRemoteStacks(int si) {
        return slots[si];
    }

    public ItemStack getRemoteSlot(int si, int index) {
        if (index >= slots[si].size()) {
            return ItemStack.EMPTY;
        }
        return slots[si].get(index);
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
        if (index >= slots[si].size()) {
            return ItemStack.EMPTY;
        }
        ItemStackList stacks = slots[si];
        boolean hasOld = !stacks.get(index).isEmpty();
        ItemStack its = ItemStack.EMPTY;
        if (!stacks.get(index).isEmpty()) {
            if (stacks.get(index).getCount() <= amount) {
                ItemStack old = stacks.get(index);
                stacks.set(index, ItemStack.EMPTY);
                its = old;
            } else {
                its = stacks.get(index).splitStack(amount);
                if (stacks.get(index).isEmpty()) {
                    stacks.set(index, ItemStack.EMPTY);
                }
            }
        }

        boolean hasNew = !stacks.get(index).isEmpty();
        if (hasOld && !hasNew) {
            numStacks[si]--;
        } else if (hasNew && !hasOld) {
            numStacks[si]++;
        }
        StorageModuleItem.updateStackSize(getStackInSlot(si), numStacks[si]);

        markDirty();
        return its;
    }

    public ItemStack removeStackFromSlotRemote(int si, int index) {
        if (index >= slots[si].size()) {
            return ItemStack.EMPTY;
        }
        ItemStackList stacks = slots[si];
        if (stacks.get(index).isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack old = stacks.get(index);
        stacks.set(index, ItemStack.EMPTY);

        numStacks[si]--;
        StorageModuleItem.updateStackSize(getStackInSlot(si), numStacks[si]);

        markDirty();
        return old;
    }

    public boolean updateRemoteSlot(int si, int limit, int index, ItemStack stack) {
        if (index >= slots[si].size()) {
            return false;
        }
        boolean hasOld = !slots[si].get(index).isEmpty();
        slots[si].set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > limit) {
            if (limit <= 0) {
                stack.setCount(0);
            } else {
                stack.setCount(limit);
            }
        }
        boolean hasNew = !stack.isEmpty();
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
        return ItemStack.EMPTY;
    }

    @Nonnull
    public ItemStackList findStacksForId(int id) {
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
        return ItemStackList.EMPTY;
    }


    public void copyToModule(int si) {
        ItemStack stack = inventoryHelper.getStackInSlot(si);
        if (stack.isEmpty()) {
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
    }

    public void copyFromModule(ItemStack stack, int si) {
        if (stack.isEmpty()) {
            setMaxSize(si, 0);
            return;
        }
        if (stack.getItemDamage() == StorageModuleItem.STORAGE_REMOTE) {
            setMaxSize(si, 0);
            return;
        }

        setMaxSize(si, StorageModuleItem.MAXSIZE[stack.getItemDamage()]);

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            readSlotsFromNBT(tagCompound, "Items", si);
        }

        updateStackCount(si);
    }

    private void setMaxSize(int si, int ms) {
        maxsize[si] = ms;
        slots[si] = ItemStackList.create(ms);
        numStacks[si] = 0;
    }

    private void updateStackCount(int si) {
        numStacks[si] = 0;
        ItemStackList stacks = slots[si];
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
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
        readBufferFromNBT(tagCompound, inventoryHelper);
        version = tagCompound.getInteger("version");
        for (int i = 0 ; i < 4 ; i++) {
            int max = tagCompound.getInteger("maxSize" + i);
            setMaxSize(i, max);
            readSlotsFromNBT(tagCompound, "Slots" + i, i);
            global[i] = tagCompound.getBoolean("global" + i);
            updateStackCount(i);
        }
    }

    private void readSlotsFromNBT(NBTTagCompound tagCompound, String tagname, int index) {
        NBTTagList bufferTagList = tagCompound.getTagList(tagname, Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < Math.min(bufferTagList.tagCount(), slots[index].size()) ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            slots[index].set(i, new ItemStack(nbtTagCompound));
        }
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("version", version);
        for (int i = 0 ; i < 4 ; i++) {
            writeSlotsToNBT(tagCompound, "Slots" + i, i);
            tagCompound.setInteger("maxSize" + i, maxsize[i]);
            tagCompound.setBoolean("global" + i, global[i]);
        }
    }

    private int writeSlotsToNBT(NBTTagCompound tagCompound, String tagname, int index) {
        NBTTagList bufferTagList = new NBTTagList();
        int cnt = 0;
        for (int i = 0 ; i < slots[index].size() ; i++) {
            ItemStack stack = slots[index].get(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (!stack.isEmpty()) {
                stack.writeToNBT(nbtTagCompound);
                // @todo check?
                if (stack.getCount() > 0) {
                    cnt++;
                }
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag(tagname, bufferTagList);
        return cnt;
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return rc;
        }
        if (CMD_SETGLOBAL.equals(command)) {
            int index = params.get(PARAM_INDEX);
            boolean global = params.get(PARAM_GLOBAL);
            setGlobal(index, global);
            return true;
        }
        return false;
    }
}

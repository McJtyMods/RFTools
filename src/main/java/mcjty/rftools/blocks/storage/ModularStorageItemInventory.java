package mcjty.rftools.blocks.storage;

import mcjty.container.InventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ModularStorageItemInventory implements IInventory {
    private ItemStack stacks[] = new ItemStack[ModularStorageItemContainer.MAXSIZE_STORAGE];
    private final int id;
    private final World world;
    private final RemoteStorageTileEntity remoteStorageTileEntity;
    private final int si;

    public ModularStorageItemInventory(World world, int id) {
        this.world = world;
        this.id = id;
        remoteStorageTileEntity = RemoteStorageIdRegistry.getRemoteStorage(world, id);
        if (remoteStorageTileEntity != null) {
            si = remoteStorageTileEntity.findRemoteIndex(id);
        } else {
            si = -1;
        }
    }

    @Override
    public int getSizeInventory() {
        if (remoteStorageTileEntity != null) {
            return remoteStorageTileEntity.getMaxStacks(si);
        } else {
            return ModularStorageItemContainer.MAXSIZE_STORAGE;
        }
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (remoteStorageTileEntity != null) {
            return remoteStorageTileEntity.getRemoteStacks(si)[index];
        } else {
            return stacks[index];
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        ItemStack[] s = stacks;
        if (remoteStorageTileEntity != null) {
            s = remoteStorageTileEntity.getRemoteStacks(si);
        }
        if (s[index] != null) {
            markDirty();
            if (s[index].stackSize <= amount) {
                ItemStack old = s[index];
                s[index] = null;
                return old;
            }
            ItemStack its = s[index].splitStack(amount);
            if (s[index].stackSize == 0) {
                s[index] = null;
            }
            return its;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack[] s = stacks;
        if (remoteStorageTileEntity != null) {
            s = remoteStorageTileEntity.getRemoteStacks(si);
        }

        markDirty();
        s[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return "remote storage";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (remoteStorageTileEntity != null) {
            remoteStorageTileEntity.markDirty();
        }

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
        if (remoteStorageTileEntity != null) {
            if (index >= remoteStorageTileEntity.getMaxStacks(si)) {
                return false;
            }
        }
        return true;
    }
}

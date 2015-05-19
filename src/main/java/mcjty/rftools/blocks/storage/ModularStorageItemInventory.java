package mcjty.rftools.blocks.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ModularStorageItemInventory implements IInventory {
    private ItemStack stacks[] = new ItemStack[ModularStorageItemContainer.MAXSIZE_STORAGE];
    private final int id;
    private final RemoteStorageTileEntity remoteStorageTileEntity;
    private final  EntityPlayer entityPlayer;

    public ModularStorageItemInventory(EntityPlayer player, int id) {
        this.entityPlayer = player;
        this.id = id;
        remoteStorageTileEntity = RemoteStorageIdRegistry.getRemoteStorage(player.worldObj, id);
    }

    private ItemStack[] getStacks() {
        if (remoteStorageTileEntity != null) {
            int si = remoteStorageTileEntity.findRemoteIndex(id);
            if (si == -1) {
                return new ItemStack[0];
            }
            return remoteStorageTileEntity.getRemoteStacks(si);
        } else {
            int maxSize = entityPlayer.getHeldItem().getTagCompound().getInteger("maxSize");
            if (maxSize != stacks.length) {
                stacks = new ItemStack[maxSize];
            }
            return stacks;
        }
    }

    @Override
    public int getSizeInventory() {
        if (remoteStorageTileEntity != null) {
            int si = remoteStorageTileEntity.findRemoteIndex(id);
            if (si == -1) {
                return 0;
            }
            return remoteStorageTileEntity.getMaxStacks(si);
        } else {
            int maxSize = entityPlayer.getHeldItem().getTagCompound().getInteger("maxSize");
            return maxSize;
        }
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (remoteStorageTileEntity != null) {
            int si = remoteStorageTileEntity.findRemoteIndex(id);
            if (si == -1) {
                return null;
            }
            return remoteStorageTileEntity.getRemoteStacks(si)[index];
        } else {
            return stacks[index];
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (remoteStorageTileEntity != null) {
            int si = remoteStorageTileEntity.findRemoteIndex(id);
            if (si == -1) {
                return null;
            }
            return remoteStorageTileEntity.decrStackSizeRemote(si, index, amount);
        } else {
            if (index >= stacks.length) {
                return null;
            }
            if (stacks[index] != null) {
                markDirty();
                if (stacks[index].stackSize <= amount) {
                    ItemStack old = stacks[index];
                    stacks[index] = null;
                    return old;
                }
                ItemStack its = stacks[index].splitStack(amount);
                if (stacks[index].stackSize == 0) {
                    stacks[index] = null;
                }
                return its;
            }
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (remoteStorageTileEntity != null) {
            int si = remoteStorageTileEntity.findRemoteIndex(id);
            if (si == -1) {
                return;
            }
            remoteStorageTileEntity.updateRemoteSlot(si, getInventoryStackLimit(), index, stack);
        } else {
            if (index >= stacks.length) {
                return;
            }
            stacks[index] = stack;
            if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                stack.stackSize = getInventoryStackLimit();
            }
            markDirty();
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
        ItemStack[] s = getStacks();
        if (index >= s.length) {
            return false;
        }
        if (remoteStorageTileEntity != null) {
            int si = remoteStorageTileEntity.findRemoteIndex(id);
            if (si == -1) {
                return false;
            }
            if (index >= remoteStorageTileEntity.getMaxStacks(si)) {
                return false;
            }
        }
        return true;
    }
}

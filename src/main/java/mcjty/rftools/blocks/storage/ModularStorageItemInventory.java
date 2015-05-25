package mcjty.rftools.blocks.storage;

import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class ModularStorageItemInventory implements IInventory {
    private ItemStack stacks[];
    private final EntityPlayer entityPlayer;

    public ModularStorageItemInventory(EntityPlayer player) {
        this.entityPlayer = player;
        int maxSize = getMaxSize();
        stacks = new ItemStack[maxSize];
        NBTTagCompound tagCompound = entityPlayer.getHeldItem().getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            entityPlayer.getHeldItem().setTagCompound(tagCompound);
        }
        tagCompound.setInteger("maxSize", maxSize);
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < Math.min(bufferTagList.tagCount(), maxSize) ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    private int getMaxSize() {
        return StorageModuleItem.MAXSIZE[entityPlayer.getHeldItem().getItemDamage()];
    }

    @Override
    public int getSizeInventory() {
        return getMaxSize();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= getMaxSize()) {
            return null;
        } else {
            return stacks[index];
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index >= stacks.length) {
            return null;
        }
        if (stacks[index] != null) {
            if (stacks[index].stackSize <= amount) {
                ItemStack old = stacks[index];
                stacks[index] = null;
                markDirty();
                return old;
            }
            ItemStack its = stacks[index].splitStack(amount);
            if (stacks[index].stackSize == 0) {
                stacks[index] = null;
            }
            markDirty();
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
        if (index >= stacks.length) {
            return;
        }
        stacks[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "module storage";
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
        NBTTagList bufferTagList = new NBTTagList();
        int numStacks = 0;
        for (int i = 0 ; i < getMaxSize() ; i++) {
            ItemStack stack = stacks[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
                if (stack.stackSize > 0) {
                    numStacks++;
                }
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        NBTTagCompound tagCompound = entityPlayer.getHeldItem().getTagCompound();
        tagCompound.setTag("Items", bufferTagList);
        tagCompound.setInteger("count", numStacks);
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
        return index < getMaxSize();
    }
}

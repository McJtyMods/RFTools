package mcjty.rftools.items.storage;

import mcjty.lib.compat.CompatInventory;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class StorageFilterInventory implements CompatInventory {
    private ItemStack stacks[] = new ItemStack[StorageFilterContainer.FILTER_SLOTS];
    private final EntityPlayer entityPlayer;

    public StorageFilterInventory(EntityPlayer player) {
        this.entityPlayer = player;
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            entityPlayer.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(tagCompound);
        }
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks[i] = ItemStackTools.loadFromNBT(nbtTagCompound);
        }
    }

    @Override
    public int getSizeInventory() {
        return StorageFilterContainer.FILTER_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index >= stacks.length) {
            return ItemStackTools.getEmptyStack();
        }
        if (ItemStackTools.isValid(stacks[index])) {
            if (ItemStackTools.getStackSize(stacks[index]) <= amount) {
                ItemStack old = stacks[index];
                stacks[index] = ItemStackTools.getEmptyStack();
                markDirty();
                return old;
            }
            ItemStack its = stacks[index].splitStack(amount);
            if (ItemStackTools.isEmpty(stacks[index])) {
                stacks[index] = ItemStackTools.getEmptyStack();
            }
            markDirty();
            return its;
        }
        return ItemStackTools.getEmptyStack();
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= stacks.length) {
            return;
        }

        if (StorageFilterContainer.factory.isGhostSlot(index)) {
            if (ItemStackTools.isValid(stack)) {
                stacks[index] = stack.copy();
                if (index < 9) {
                    ItemStackTools.setStackSize(stacks[index], 1);
                }
            } else {
                stacks[index] = ItemStackTools.getEmptyStack();
            }
        } else {
            stacks[index] = stack;
            if (ItemStackTools.isValid(stack) && ItemStackTools.getStackSize(stack) > getInventoryStackLimit()) {
                ItemStackTools.setStackSize(stack, getInventoryStackLimit());
            }
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        convertItemsToNBT(tagCompound, stacks);
    }

    public static void convertItemsToNBT(NBTTagCompound tagCompound, ItemStack[] stacks) {
        NBTTagList bufferTagList = new NBTTagList();
        for (ItemStack stack : stacks) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (ItemStackTools.isValid(stack)) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStackTools.getEmptyStack());
        return stack;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return "storage filter";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }
}

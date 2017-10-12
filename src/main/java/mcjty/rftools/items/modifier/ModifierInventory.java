package mcjty.rftools.items.modifier;

import mcjty.lib.compat.CompatInventory;
import mcjty.lib.tools.ItemStackList;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;

public class ModifierInventory implements CompatInventory {

    private final EntityPlayer entityPlayer;

    public ModifierInventory(EntityPlayer player) {
        this.entityPlayer = player;
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            entityPlayer.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(tagCompound);
        }
    }

    @Override
    public int getSizeInventory() {
        return ModifierContainer.COUNT_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        ItemStackList stacks = ModifierItem.getItemStacks(tagCompound);
        return stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        ItemStackList stacks = ModifierItem.getItemStacks(tagCompound);
        if (index >= stacks.size()) {
            return ItemStackTools.getEmptyStack();
        }
        if (ItemStackTools.isValid(stacks.get(index))) {
            if (ItemStackTools.getStackSize(stacks.get(index)) <= amount) {
                ItemStack old = stacks.get(index);
                stacks.set(index, ItemStackTools.getEmptyStack());
                markDirty();
                return old;
            }
            ItemStack its = stacks.get(index).splitStack(amount);
            if (ItemStackTools.isEmpty(stacks.get(index))) {
                stacks.set(index, ItemStackTools.getEmptyStack());
            }
            markDirty();
            return its;
        }
        return ItemStackTools.getEmptyStack();
    }

    @Override
    public void markDirty() {

    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        ItemStackList stacks = ModifierItem.getItemStacks(tagCompound);
        if (index >= stacks.size()) {
            return;
        }

        stacks.set(index, stack);
        if (ItemStackTools.isValid(stack) && ItemStackTools.getStackSize(stack) > getInventoryStackLimit()) {
            ItemStackTools.setStackSize(stack, getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    public static void convertItemsToNBT(NBTTagCompound tagCompound, ItemStackList stacks) {
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
        return "modifier";
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

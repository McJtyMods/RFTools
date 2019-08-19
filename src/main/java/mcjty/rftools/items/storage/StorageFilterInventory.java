package mcjty.rftools.items.storage;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class StorageFilterInventory implements IInventory {
    private ItemStackList stacks = ItemStackList.create(StorageFilterContainer.FILTER_SLOTS);
    private final PlayerEntity PlayerEntity;

    public StorageFilterInventory(PlayerEntity player) {
        this.PlayerEntity = player;
        CompoundNBT tagCompound = PlayerEntity.getHeldItem(Hand.MAIN_HAND).getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
            PlayerEntity.getHeldItem(Hand.MAIN_HAND).setTagCompound(tagCompound);
        }
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            CompoundNBT CompoundNBT = bufferTagList.getCompoundTagAt(i);
            stacks.set(i, new ItemStack(CompoundNBT));
        }
    }

    @Override
    public int getSizeInventory() {
        return StorageFilterContainer.FILTER_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index >= stacks.size()) {
            return ItemStack.EMPTY;
        }
        if (!stacks.get(index).isEmpty()) {
            if (stacks.get(index).getCount() <= amount) {
                ItemStack old = stacks.get(index);
                stacks.set(index, ItemStack.EMPTY);
                markDirty();
                return old;
            }
            ItemStack its = stacks.get(index).splitStack(amount);
            if (stacks.get(index).isEmpty()) {
                stacks.set(index, ItemStack.EMPTY);
            }
            markDirty();
            return its;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= stacks.size()) {
            return;
        }

        if (StorageFilterContainer.factory.isGhostSlot(index)) {
            if (!stack.isEmpty()) {
                ItemStack stack1 = stack.copy();
                if (index < 9) {
                    stack1.setCount(1);
                }
                stacks.set(index, stack1);
            } else {
                stacks.set(index, ItemStack.EMPTY);
            }
        } else {
            stacks.set(index, stack);
            if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
                int amount = getInventoryStackLimit();
                if (amount <= 0) {
                    stack.setCount(0);
                } else {
                    stack.setCount(amount);
                }
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
        ItemStack heldItem = PlayerEntity.getHeldItem(Hand.MAIN_HAND);
        if (!(heldItem).isEmpty()) {
            CompoundNBT tagCompound = heldItem.getTag();
            convertItemsToNBT(tagCompound, stacks);
        }
    }

    public static void convertItemsToNBT(CompoundNBT tagCompound, ItemStackList stacks) {
        NBTTagList bufferTagList = new NBTTagList();
        for (ItemStack stack : stacks) {
            CompoundNBT CompoundNBT = new CompoundNBT();
            if (!stack.isEmpty()) {
                stack.writeToNBT(CompoundNBT);
            }
            bufferTagList.appendTag(CompoundNBT);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    public boolean isUsable(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void openInventory(PlayerEntity player) {

    }

    @Override
    public void closeInventory(PlayerEntity player) {

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

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return isUsable(player);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}

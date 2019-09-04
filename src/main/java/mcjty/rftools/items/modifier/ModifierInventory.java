package mcjty.rftools.items.modifier;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;

// @todo 1.14 convert to IItemHandler
public class ModifierInventory implements IInventory {

    private final PlayerEntity playerEntity;

    public ModifierInventory(PlayerEntity player) {
        this.playerEntity = player;
        playerEntity.getHeldItem(Hand.MAIN_HAND).getOrCreateTag();
    }

    @Override
    public int getSizeInventory() {
        return ModifierContainer.COUNT_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        CompoundNBT tagCompound = playerEntity.getHeldItem(Hand.MAIN_HAND).getTag();
        ItemStackList stacks = ModifierItem.getItemStacks(tagCompound);
        return stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        CompoundNBT tagCompound = playerEntity.getHeldItem(Hand.MAIN_HAND).getTag();
        ItemStackList stacks = ModifierItem.getItemStacks(tagCompound);
        if (index >= stacks.size()) {
            return ItemStack.EMPTY;
        }
        if (!stacks.get(index).isEmpty()) {
            if (stacks.get(index).getCount() <= amount) {
                ItemStack old = stacks.get(index);
                stacks.set(index, ItemStack.EMPTY);
                convertItemsToNBT(tagCompound, stacks);
                markDirty();
                return old;
            }
            ItemStack its = stacks.get(index).split(amount);
            if (stacks.get(index).isEmpty()) {
                stacks.set(index, ItemStack.EMPTY);
                convertItemsToNBT(tagCompound, stacks);
            }
            markDirty();
            return its;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        CompoundNBT tagCompound = playerEntity.getHeldItem(Hand.MAIN_HAND).getTag();
        ItemStackList stacks = ModifierItem.getItemStacks(tagCompound);
        if (index >= stacks.size()) {
            return;
        }

        stacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        convertItemsToNBT(tagCompound, stacks);
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    public static void convertItemsToNBT(CompoundNBT tagCompound, ItemStackList stacks) {
        ListNBT bufferTagList = new ListNBT();
        for (ItemStack stack : stacks) {
            CompoundNBT CompoundNBT = new CompoundNBT();
            if (!stack.isEmpty()) {
                stack.write(CompoundNBT);
            }
            bufferTagList.add(CompoundNBT);
        }
        tagCompound.put("Items", bufferTagList);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
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
    public void clear() {

    }
}

package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.compat.CompatItemHandlerModifiable;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemFilterInvWrapper implements CompatItemHandlerModifiable {
    private final ISidedInventory inv;
    private final EnumFacing side;

    public ItemFilterInvWrapper(ISidedInventory inv) {
        this(inv, null);
    }

    public ItemFilterInvWrapper(ISidedInventory inv, EnumFacing side) {
        this.inv = inv;
        this.side = side;
    }

    public static int getSlot(ISidedInventory inv, int slot, EnumFacing side) {
        int[] slots = inv.getSlotsForFace(side);
        if (slot < slots.length) {
            return slots[slot];
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemFilterInvWrapper that = (ItemFilterInvWrapper) o;

        if (!inv.equals(that.inv)) {
            return false;
        }
        if (side != that.side) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = inv.hashCode();
        result = 31 * result + (side != null ? side.hashCode() : 0);
        return result;
    }

    @Override
    public int getSlots() {
        return inv.getSlotsForFace(side).length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int i = getSlot(inv, slot, side);
        return i == -1 ? ItemStackTools.getEmptyStack() : inv.getStackInSlot(i);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

        if (ItemStackTools.isEmpty(stack)) {
            return ItemStackTools.getEmptyStack();
        }

        int slot1 = getSlot(inv, slot, side);

        if (slot1 == -1) {
            return stack;
        }

        if (!inv.isItemValidForSlot(slot1, stack) || !inv.canInsertItem(slot1, stack, side)) {
            return stack;
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot1);

        int m;
        if (ItemStackTools.isValid(stackInSlot)) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
                return stack;
            }

            m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit()) - ItemStackTools.getStackSize(stackInSlot);

            if (ItemStackTools.getStackSize(stack) <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    ItemStackTools.incStackSize(copy, ItemStackTools.getStackSize(stackInSlot));
                    inv.setInventorySlotContents(slot1, copy);
                }

                return ItemStackTools.getEmptyStack();
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.splitStack(m);
                    ItemStackTools.incStackSize(copy, ItemStackTools.getStackSize(stackInSlot));
                    inv.setInventorySlotContents(slot1, copy);
                    return stack;
                } else {
                    ItemStackTools.incStackSize(stack, -m);
                    return stack;
                }
            }
        } else {
            m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
            if (m < ItemStackTools.getStackSize(stack)) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    inv.setInventorySlotContents(slot1, stack.splitStack(m));
                    return stack;
                } else {
                    ItemStackTools.incStackSize(stack, -m);
                    return stack;
                }
            } else {
                if (!simulate) {
                    inv.setInventorySlotContents(slot1, stack);
                }
                return ItemStackTools.getEmptyStack();
            }
        }

    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        inv.setInventorySlotContents(slot, stack);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStackTools.getEmptyStack();
        }

        int slot1 = getSlot(inv, slot, side);

        if (slot1 == -1) {
            return ItemStackTools.getEmptyStack();
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot1);

        if (ItemStackTools.isEmpty(stackInSlot)) {
            return ItemStackTools.getEmptyStack();
        }

        if (!inv.canExtractItem(slot1, stackInSlot, side)) {
            return ItemStackTools.getEmptyStack();
        }

        if (simulate) {
            if (ItemStackTools.getStackSize(stackInSlot) < amount) {
                return stackInSlot.copy();
            } else {
                ItemStack copy = stackInSlot.copy();
                ItemStackTools.setStackSize(copy, amount);
                return copy;
            }
        } else {
            int m = Math.min(ItemStackTools.getStackSize(stackInSlot), amount);
            return inv.decrStackSize(slot1, m);
        }
    }

    @Override
    public int getSlotMaxLimit() {
        return 64;
    }
}
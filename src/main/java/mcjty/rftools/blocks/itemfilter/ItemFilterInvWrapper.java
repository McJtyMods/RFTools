package mcjty.rftools.blocks.itemfilter;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemFilterInvWrapper implements IItemHandlerModifiable {
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
        return i == -1 ? ItemStack.EMPTY : inv.getStackInSlot(i);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
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
        if (!stackInSlot.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
                return stack;
            }

            m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit()) - stackInSlot.getCount();

            if (stack.getCount() <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    inv.setInventorySlotContents(slot1, copy);
                }

                return ItemStack.EMPTY;
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.splitStack(m);
                    copy.grow(stackInSlot.getCount());
                    inv.setInventorySlotContents(slot1, copy);
                    return stack;
                } else {
                    int amount = -m;
                    stack.grow(amount);
                    return stack;
                }
            }
        } else {
            m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
            if (m < stack.getCount()) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    inv.setInventorySlotContents(slot1, stack.splitStack(m));
                    return stack;
                } else {
                    int amount = -m;
                    stack.grow(amount);
                    return stack;
                }
            } else {
                if (!simulate) {
                    inv.setInventorySlotContents(slot1, stack);
                }
                return ItemStack.EMPTY;
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
            return ItemStack.EMPTY;
        }

        int slot1 = getSlot(inv, slot, side);

        if (slot1 == -1) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot1);

        if (stackInSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!inv.canExtractItem(slot1, stackInSlot, side)) {
            return ItemStack.EMPTY;
        }

        if (simulate) {
            if (stackInSlot.getCount() < amount) {
                return stackInSlot.copy();
            } else {
                ItemStack copy = stackInSlot.copy();
                if (amount <= 0) {
                    copy.setCount(0);
                } else {
                    copy.setCount(amount);
                }
                return copy;
            }
        } else {
            int m = Math.min(stackInSlot.getCount(), amount);
            return inv.decrStackSize(slot1, m);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
}
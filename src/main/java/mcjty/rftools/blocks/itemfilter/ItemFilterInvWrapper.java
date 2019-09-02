package mcjty.rftools.blocks.itemfilter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

import static mcjty.rftools.blocks.itemfilter.ItemFilterTileEntity.SLOT_BUFFER;

public class ItemFilterInvWrapper implements IItemHandlerModifiable {
    private final ItemFilterTileEntity te;
    private final IItemHandlerModifiable inv;
    private final Direction side;

    public ItemFilterInvWrapper(ItemFilterTileEntity te) {
        this(te, null);
    }

    public ItemFilterInvWrapper(ItemFilterTileEntity te, Direction side) {
        this.te = te;
        this.inv = te.getItemHandler();
        this.side = side;
    }

    public static int getSlot(ItemFilterTileEntity te, int slot, Direction side) {
        int[] slots = te.getSlotsForFace(side);
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
        return te.getSlotsForFace(side).length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int i = getSlot(te, slot, side);
        return i == -1 ? ItemStack.EMPTY : inv.getStackInSlot(i);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int slot1 = getSlot(te, slot, side);

        if (slot1 == -1) {
            return stack;
        }

        if (!inv.isItemValid(slot1, stack) || !te.canInsertItem(slot1, stack, side)) {
            return stack;
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot1);

        int m;
        if (!stackInSlot.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
                return stack;
            }

            m = Math.min(stack.getMaxStackSize(), inv.getSlots()) - stackInSlot.getCount();

            if (stack.getCount() <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    inv.setStackInSlot(slot1, copy);
                }

                return ItemStack.EMPTY;
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.split(m);
                    copy.grow(stackInSlot.getCount());
                    inv.setStackInSlot(slot1, copy);
                    return stack;
                } else {
                    int amount = -m;
                    stack.grow(amount);
                    return stack;
                }
            }
        } else {
            m = Math.min(stack.getMaxStackSize(), inv.getSlotLimit(0));
            if (m < stack.getCount()) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    inv.setStackInSlot(slot1, stack.split(m));
                    return stack;
                } else {
                    int amount = -m;
                    stack.grow(amount);
                    return stack;
                }
            } else {
                if (!simulate) {
                    inv.setStackInSlot(slot1, stack);
                }
                return ItemStack.EMPTY;
            }
        }

    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        inv.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }

        int slot1 = getSlot(te, slot, side);

        if (slot1 == -1) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot1);

        if (stackInSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!te.canExtractItem(slot1, stackInSlot, side)) {
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
            return inv.extractItem(slot1, m, false);
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot < SLOT_BUFFER) {
            return false;
        }
        ItemStack ghostStack = getStackInSlot(slot - SLOT_BUFFER);
        return ghostStack.isEmpty() || ghostStack.isItemEqual(stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
}
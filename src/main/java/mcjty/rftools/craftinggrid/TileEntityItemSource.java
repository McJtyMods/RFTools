package mcjty.rftools.craftinggrid;

import mcjty.lib.container.InventoryHelper;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityItemSource implements IItemSource {

    private List<Pair<Object, Integer>> inventories = new ArrayList<>();

    public TileEntityItemSource add(TileEntity te, int offset) {
        if (RFToolsTools.hasItemCapabilitySafe(te)) {
            IItemHandler capability = RFToolsTools.getItemCapabilitySafe(te);
            inventories.add(Pair.of(capability, offset));
        } else if (te instanceof IInventory) {
            inventories.add(Pair.of(te, offset));
        }
        return this;
    }

    public TileEntityItemSource addInventory(IInventory te, int offset) {
        inventories.add(Pair.of(te, offset));
        return this;
    }

    private static ItemStack getStackInSlot(Object inv, int slot) {
        if (inv instanceof IItemHandler) {
            return ((IItemHandler) inv).getStackInSlot(slot);
        } else if (inv instanceof IInventory) {
            return ((IInventory) inv).getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    private static boolean insertStackInSlot(Object inv, int slot, ItemStack stack) {
        if (inv instanceof IItemHandler) {
            IItemHandler handler = (IItemHandler) inv;
            if (!handler.insertItem(slot, stack, true).isEmpty()) {
                return false;
            }
            return handler.insertItem(slot, stack, false).isEmpty();
        } else if (inv instanceof IInventory) {
            IInventory inventory = (IInventory) inv;
            ItemStack oldStack = inventory.getStackInSlot(slot);
            if (!oldStack.isEmpty()) {
                if ((stack.getCount() + oldStack.getCount()) > stack.getMaxStackSize()) {
                    return false;
                }
                stack.grow(oldStack.getCount());
            }
            inventory.setInventorySlotContents(slot, stack);
            return true;
        }
        return false;
    }

    private static int insertStackInAnySlot(Object inv, ItemStack stack) {
        if (inv instanceof IItemHandler) {
            IItemHandler handler = (IItemHandler) inv;
            ItemStack leftOver = ItemHandlerHelper.insertItem(handler, stack, false);
            return leftOver.getCount();
        } else if (inv instanceof IInventory) {
            IInventory inventory = (IInventory) inv;
            return InventoryHelper.mergeItemStack(inventory, true, stack, 0, inventory.getSizeInventory(), null);
        }
        return stack.getCount();
    }

    private static int getSizeInventory(Object inv) {
        if (inv instanceof IItemHandler) {
            return ((IItemHandler) inv).getSlots();
        } else if (inv instanceof IInventory) {
            return ((IInventory) inv).getSizeInventory();
        }
        return 0;
    }

    @Override
    public Iterable<Pair<IItemKey, ItemStack>> getItems() {
        return () -> new Iterator<Pair<IItemKey, ItemStack>>() {
            private int inventoryIndex = 0;
            private int slotIndex = 0;

            private boolean firstValidItem() {
                while (true) {
                    if (inventoryIndex >= inventories.size()) {
                        return false;
                    }
                    Object inventory = inventories.get(inventoryIndex).getLeft();
                    if (slotIndex < getSizeInventory(inventory)) {
                        return true;
                    } else {
                        slotIndex = 0;
                        inventoryIndex++;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return firstValidItem();
            }

            @Override
            public Pair<IItemKey, ItemStack> next() {
                Object te = inventories.get(inventoryIndex).getLeft();

                ItemKey key = new ItemKey(te, slotIndex);
                Pair<IItemKey, ItemStack> result = Pair.of(key, getStackInSlot(te, slotIndex));
                slotIndex++;
                return result;
            }
        };
    }

    @Override
    public ItemStack decrStackSize(IItemKey key, int amount) {
        ItemKey realKey = (ItemKey) key;
        Object te = realKey.getInventory();
        if (te instanceof IItemHandler) {
            IItemHandler handler = (IItemHandler) te;
            return handler.extractItem(realKey.getSlot(), amount, false);
        } else if (te instanceof IInventory) {
            IInventory inventory = (IInventory) te;
            ItemStack stack = inventory.getStackInSlot(realKey.getSlot());
            ItemStack result = stack.splitStack(amount);
            if (stack.isEmpty()) {
                inventory.setInventorySlotContents(realKey.getSlot(), ItemStack.EMPTY);
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean insertStack(IItemKey key, ItemStack stack) {
        ItemKey realKey = (ItemKey) key;
        return insertStackInSlot(realKey.getInventory(), realKey.getSlot(), stack);
    }

    @Override
    public int insertStackAnySlot(IItemKey key, ItemStack stack) {
        ItemKey realKey = (ItemKey) key;
        return insertStackInAnySlot(realKey.getInventory(), stack);
    }

    private static class ItemKey implements IItemKey {
        private Object inventory;
        private int slot;

        public ItemKey(Object inventory, int slot) {
            this.inventory = inventory;
            this.slot = slot;
        }

        public Object getInventory() {
            return inventory;
        }

        public int getSlot() {
            return slot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ItemKey itemKey = (ItemKey) o;

            if (slot != itemKey.slot) {
                return false;
            }
            return inventory.equals(itemKey.inventory);

        }

        @Override
        public int hashCode() {
            int result = inventory.hashCode();
            result = 31 * result + slot;
            return result;
        }
    }
}

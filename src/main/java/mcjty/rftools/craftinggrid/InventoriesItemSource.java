package mcjty.rftools.craftinggrid;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InventoriesItemSource implements IItemSource {

    private List<Pair<IInventory, Integer>> inventories = new ArrayList<>();

    public InventoriesItemSource add(IInventory inventory, int offset) {
        inventories.add(Pair.of(inventory, offset));
        return this;
    }

    @Override
    public Iterable<Pair<IItemKey, ItemStack>> getItems() {
        return () -> new Iterator<Pair<IItemKey, ItemStack>>() {
            private int inventoryIndex = 0;
            private int slotIndex = 0;

            @Override
            public boolean hasNext() {
                return inventoryIndex < inventories.size() && slotIndex < inventories.get(inventoryIndex).getLeft().getSizeInventory();
            }

            @Override
            public Pair<IItemKey, ItemStack> next() {
                IInventory inventory = inventories.get(inventoryIndex).getLeft();
                ItemKey key = new ItemKey(inventory, slotIndex);
                Pair<IItemKey, ItemStack> result = Pair.of(key, inventory.getStackInSlot(slotIndex));
                slotIndex++;
                if (slotIndex >= inventory.getSizeInventory()) {
                    inventoryIndex++;
                    if (inventoryIndex < inventories.size()) {
                        slotIndex = inventories.get(inventoryIndex).getRight();
                    }
                }
                return result;
            }
        };
    }

    @Override
    public ItemStack decrStackSize(IItemKey key, int amount) {
        ItemKey realKey = (ItemKey) key;
        ItemStack stack = realKey.getInventory().getStackInSlot(realKey.getSlot());
        ItemStack result = stack.splitStack(amount);
        if (stack.stackSize == 0) {
            realKey.getInventory().setInventorySlotContents(realKey.getSlot(), null);
        }
        return result;
    }

    @Override
    public void insertStack(IItemKey key, ItemStack stack) {
        ItemKey realKey = (ItemKey) key;
        IInventory inventory = realKey.getInventory();
        ItemStack origStack = inventory.removeStackFromSlot(realKey.getSlot());
        if (origStack != null) {
            stack.stackSize += origStack.stackSize;
        }
        inventory.setInventorySlotContents(realKey.getSlot(), stack);
    }

    private static class ItemKey implements IItemKey {
        private IInventory inventory;
        private int slot;

        public ItemKey(IInventory inventory, int slot) {
            this.inventory = inventory;
            this.slot = slot;
        }

        public IInventory getInventory() {
            return inventory;
        }

        public int getSlot() {
            return slot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ItemKey itemKey = (ItemKey) o;

            if (slot != itemKey.slot) return false;
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

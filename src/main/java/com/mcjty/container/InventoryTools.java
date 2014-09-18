package com.mcjty.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryTools {
    /**
     * Merges provided ItemStack with the first avaliable one in this inventory.
     */
    public static boolean mergeItemStack(IInventory inventory, ItemStack result, int start, int stop) {
        boolean success = false;
        int k = start;

        ItemStack itemstack1;
        int itemsToPlace = result.stackSize;

        if (result.isStackable()) {
            while (itemsToPlace > 0 && (k < stop)) {
                itemstack1 = inventory.getStackInSlot(k);

                if (itemstack1 != null && itemstack1.getItem() == result.getItem() && (!result.getHasSubtypes() || result.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(result, itemstack1)) {
                    int l = itemstack1.stackSize + itemsToPlace;

                    if (l <= result.getMaxStackSize()) {
                        itemsToPlace = 0;
                        itemstack1.stackSize = l;
                        inventory.markDirty();
                        success = true;
                    } else if (itemstack1.stackSize < result.getMaxStackSize()) {
                        itemsToPlace -= result.getMaxStackSize() - itemstack1.stackSize;
                        itemstack1.stackSize = result.getMaxStackSize();
                        inventory.markDirty();
                        success = true;
                    }
                }

                ++k;
            }
        }

        if (itemsToPlace > 0) {
            k = start;

            while (k < stop) {
                itemstack1 = inventory.getStackInSlot(k);

                if (itemstack1 == null) {
                    inventory.setInventorySlotContents(k, result.copy());
                    inventory.markDirty();
                    itemsToPlace = 0;
                    success = true;
                    break;
                }

                ++k;
            }
        }

        return success;
    }

}

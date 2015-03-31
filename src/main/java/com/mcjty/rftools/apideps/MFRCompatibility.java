package com.mcjty.rftools.apideps;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

public class MFRCompatibility {
    public static boolean isExtendedStorage(IInventory inventory) {
        return inventory instanceof IDeepStorageUnit;
    }

    public static ItemStack getContents(IInventory inventory) {
        return ((IDeepStorageUnit) inventory).getStoredItemType();
    }
}

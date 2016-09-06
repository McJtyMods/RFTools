package mcjty.rftools.api.storage;

import net.minecraft.item.ItemStack;

/**
 * This interface is implemented by the RFTools Storage Scanner tile entity
 */
public interface IStorageScanner {

    /**
     * Request an itemstack from the storage scanner
     * Returns null if not possible. The returned itemstack will have at most
     * 'amount' items in it. It will try to extract as much as possible.
     * The given amount is automatically capped to the stack limit of the item
     * This can also return null if there is insufficient energy for the request
     */
    ItemStack requestItem(ItemStack match, int amount, boolean routable, boolean oredict);

    /**
     * Count the number of items that match in this storage scanner. This counts all the
     * items available in the system so the returned number can exceed the maximum
     * stacksize of the item
     * @return
     */
    int countItems(ItemStack match, boolean routable, boolean oredict);

    /**
     * Push the given items into the system (routable inventories only).
     * Returns the number of items that could not be inserted.
     * @param stack
     */
    int insertItem(ItemStack stack);
}

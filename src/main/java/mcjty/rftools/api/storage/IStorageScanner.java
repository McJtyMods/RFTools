package mcjty.rftools.api.storage;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * This interface is implemented by the RFTools Storage Scanner tile entity
 */
public interface IStorageScanner {

    /**
     * Request an itemstack from the storage scanner
     * Returns null/EMPTY if not possible. The returned itemstack will have at most
     * 'amount' items in it. It will try to extract as much as possible.
     * The given amount is automatically capped to the stack limit of the item
     * This can also return null (or EMPTY on 1.11) if there is insufficient energy
     * for the request
     */
    ItemStack requestItem(ItemStack match, int amount, boolean routable, boolean oredict);

    /**
     * Request an itemstack from the storage scanner
     * Returns null/EMPTY if not possible.
     * The returned item will have at most 'amount' items in it (capped to max stacksize)
     * This can also return null (or EMPTY on 1.11) if there is insufficient energy
     * for the request
     */
    ItemStack requestItem(Predicate<ItemStack> matcher, boolean simulate, int amount, boolean routable);

    /**
     * Count the number of items that match in this storage scanner. This counts all the
     * items available in the system so the returned number can exceed the maximum
     * stacksize of the item
     * @return
     */
    int countItems(ItemStack match, boolean routable, boolean oredict);

    /**
     * Count the number of items that match in this storage scanner. This counts all the
     * items available in the system so the returned number can exceed the maximum
     * stacksize of the item
     * @return
     */
    int countItems(ItemStack match, boolean routable, boolean oredict, @Nullable Integer maxneeded);

    /**
     * Count items matching a certain predicate.
     * If 'maxneeded' is given then the count will stop as soon as we have reached that number
     */
    int countItems(Predicate<ItemStack> matcher, boolean routable, @Nullable Integer maxneeded);

    /**
     * Push the given items into the system (routable inventories only).
     * Returns the stack that could not be inserted.
     * @param stack
     */
    ItemStack insertItem(ItemStack stack, boolean simulate);

    /**
     * Push the given items into the system (routable inventories only).
     * Returns the number of items that could not be inserted.
     * @param stack
     */
    int insertItem(ItemStack stack);
}

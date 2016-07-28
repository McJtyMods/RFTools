package mcjty.rftools.craftinggrid;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public interface IItemSource {

    Iterable<Pair<IItemKey, ItemStack>> getItems();

    // Return the actually removed items
    ItemStack decrStackSize(IItemKey key, int amount);

    // Insert the itemstack in the specific slot. This returns true on success
    boolean insertStack(IItemKey key, ItemStack stack);

    // Insert the itemstack in any slot. Return number of items that could not be inserted
    int insertStackAnySlot(IItemKey key, ItemStack stack);
}

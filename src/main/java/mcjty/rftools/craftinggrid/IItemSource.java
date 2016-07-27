package mcjty.rftools.craftinggrid;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public interface IItemSource {

    Iterable<Pair<IItemKey, ItemStack>> getItems();

    // Return the actually removed items
    ItemStack decrStackSize(IItemKey key, int amount);

    // Insert the itemstack in the specific slot. This does not check if the items are identical!
    void insertStack(IItemKey key, ItemStack stack);
}

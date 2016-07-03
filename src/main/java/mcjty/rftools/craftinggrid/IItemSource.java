package mcjty.rftools.craftinggrid;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public interface IItemSource {

    Iterable<Pair<IItemKey, ItemStack>> getItems();

    void decrStackSize(IItemKey key, int amount);

    void putStack(IItemKey key, ItemStack stack);
}

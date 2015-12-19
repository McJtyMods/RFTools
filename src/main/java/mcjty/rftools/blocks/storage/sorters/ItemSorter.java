package mcjty.rftools.blocks.storage.sorters;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public interface ItemSorter {
    String getName();

    String getTooltip();

    // For the icon
    int getU();
    int getV();

    Comparator<Pair<ItemStack,Integer>> getComparator();

    // Return true if these items belong to the same group.
    boolean isSameGroup(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2);

    // Return a name of the group for this item (this is basically the primary key on which we sort)
    String getGroupName(Pair<ItemStack, Integer> object);
}

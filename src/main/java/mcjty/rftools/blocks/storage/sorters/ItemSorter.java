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
}

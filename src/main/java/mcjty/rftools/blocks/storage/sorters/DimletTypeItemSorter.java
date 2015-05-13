package mcjty.rftools.blocks.storage.sorters;

import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletType;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public class DimletTypeItemSorter implements ItemSorter {
    @Override
    public String getName() {
        return "type";
    }

    @Override
    public String getTooltip() {
        return "Sort on dimlet type";
    }

    @Override
    public int getU() {
        return 14*16;
    }

    @Override
    public int getV() {
        return 16;
    }

    @Override
    public Comparator<Pair<ItemStack, Integer>> getComparator() {
        return new Comparator<Pair<ItemStack, Integer>>() {
            @Override
            public int compare(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
                return compareTypes(o1, o2);
            }
        };
    }

    public static int compareTypes(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        DimletType type1 = null;
        if (o1.getKey().getItem() == DimletSetup.knownDimlet) {
            DimletKey key = KnownDimletConfiguration.getDimletKey(o1.getKey(), null);
            type1 = key.getType();
        }
        DimletType type2 = null;
        if (o2.getKey().getItem() == DimletSetup.knownDimlet) {
            DimletKey key = KnownDimletConfiguration.getDimletKey(o2.getKey(), null);
            type2 = key.getType();
        }
        if (type1 == null) {
            if (type2 == null) {
                return NameItemSorter.compareNames(o1, o2);
            } else {
                return -1;
            }
        }
        if (type2 == null) {
            return 1;
        }
        if (type1 == type2) {
            return NameItemSorter.compareNames(o1, o2);
        }
        return type1.compareTo(type2);
    }
}

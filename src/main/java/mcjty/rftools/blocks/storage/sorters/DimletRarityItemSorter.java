package mcjty.rftools.blocks.storage.sorters;

import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.items.dimlets.DimletEntry;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletType;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public class DimletRarityItemSorter implements ItemSorter {
    @Override
    public String getName() {
        return "rarity";
    }

    @Override
    public String getTooltip() {
        return "Sort on dimlet rarity";
    }

    @Override
    public int getU() {
        return 15*16;
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
                int rarity1 = -1;
                if (o1.getKey().getItem() == DimletSetup.knownDimlet) {
                    DimletKey key = KnownDimletConfiguration.getDimletKey(o1.getKey(), null);
                    DimletEntry entry = KnownDimletConfiguration.getEntry(key);
                    if (entry != null) {
                        rarity1 = entry.getRarity();
                    }
                }
                int rarity2 = -1;
                if (o2.getKey().getItem() == DimletSetup.knownDimlet) {
                    DimletKey key = KnownDimletConfiguration.getDimletKey(o2.getKey(), null);
                    DimletEntry entry = KnownDimletConfiguration.getEntry(key);
                    if (entry != null) {
                        rarity2 = entry.getRarity();
                    }
                }

                if (rarity1 == rarity2) {
                    return DimletTypeItemSorter.compareTypes(o1, o2);
                }

                return new Integer(rarity1).compareTo(rarity2);
            }
        };
    }
}

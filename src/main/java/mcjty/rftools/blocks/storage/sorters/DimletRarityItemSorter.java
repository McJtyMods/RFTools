package mcjty.rftools.blocks.storage.sorters;

import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.items.dimlets.DimletEntry;
import mcjty.rftools.items.dimlets.DimletKey;
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
                int rarity1 = getRarity(o1);
                int rarity2 = getRarity(o2);

                if (rarity1 == rarity2) {
                    return DimletTypeItemSorter.compareTypes(o1, o2);
                }

                return new Integer(rarity1).compareTo(rarity2);
            }
        };
    }

    private int getRarity(Pair<ItemStack, Integer> object) {
        int rarity = -1;
        if (object.getKey().getItem() == DimletSetup.knownDimlet) {
            DimletKey key = KnownDimletConfiguration.getDimletKey(object.getKey(), null);
            DimletEntry entry = KnownDimletConfiguration.getEntry(key);
            if (entry != null) {
                rarity = entry.getRarity();
            }
        }
        return rarity;
    }

    @Override
    public boolean isSameGroup(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        int rarity1 = getRarity(o1);
        int rarity2 = getRarity(o2);
        return rarity1 == rarity2;
    }

    @Override
    public String getGroupName(Pair<ItemStack, Integer> object) {
        int rarity = getRarity(object);
        return "Rarity " + (rarity == -1 ? "unknown" : rarity);
    }
}

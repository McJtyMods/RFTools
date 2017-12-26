package mcjty.rftools.blocks.storage.sorters;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public class OreTypeItemSorter implements ItemSorter {
    @Override
    public String getName() {
        return "type";
    }

    @Override
    public String getTooltip() {
        return "Sort on ore type";
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
        return OreTypeItemSorter::compareOreType;
    }

    @Override
    public boolean isSameGroup(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String oreName1 = getOreType(o1);
        String oreName2 = getOreType(o2);
        if (oreName1 == null) {
            return oreName2 == null;
        }
        if (oreName2 == null) {
            return false;
        }
        return oreName1.equals(oreName2);
    }

    public static int compareOreType(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String oreName1 = getOreType(o1);
        String oreName2 = getOreType(o2);

        if (oreName1 == null) {
            if (oreName2 == null) {
                return NameItemSorter.compareNames(o1, o2);
            } else {
                return -1;
            }
        }

        if (oreName2 == null) {
            return 1;
        }
        if (oreName1.equals(oreName2)) {
            return NameItemSorter.compareNames(o1, o2);
        }
        return oreName1.compareTo(oreName2);
    }

    private static String getOreType(Pair<ItemStack, Integer> object) {
        int[] iDs = OreDictionary.getOreIDs(object.getKey());
        String name;
        if (iDs.length < 1) {
            name = null;
        } else {
            name = OreDictionary.getOreName(iDs[0]);
        }
        return name;
    }

    @Override
    public String getGroupName(Pair<ItemStack, Integer> object) {
        String name = getOreType(object);
        if (name == null || name.isEmpty()) {
            return "none";
        }

        return name;
    }
}

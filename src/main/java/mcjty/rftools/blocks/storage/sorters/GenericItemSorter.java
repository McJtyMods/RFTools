package mcjty.rftools.blocks.storage.sorters;

import net.minecraft.item.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public class GenericItemSorter implements ItemSorter {
    @Override
    public String getName() {
        return "type";
    }

    @Override
    public String getTooltip() {
        return "Sort on generic type";
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
                return compareOreType(o1, o2);
            }
        };
    }

    @Override
    public boolean isSameGroup(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String name1 = getName(o1);
        String name2 = getName(o2);
        return name1.equals(name2);
    }

    public static int compareOreType(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String name1 = getName(o1);
        String name2 = getName(o2);

        if (name1.equals(name2)) {
            return NameItemSorter.compareNames(o1, o2);
        }
        return name1.compareTo(name2);
    }

    private static String getName(Pair<ItemStack, Integer> object) {
        Item item = object.getKey().getItem();
        if (item instanceof ItemPotion) {
            return "Potions";
        } else if (item instanceof ItemArmor) {
            return "Armor";
        } else if (item instanceof ItemBook) {
            return "Books";
        } else if (item instanceof ItemFood) {
            return "Food";
        } else if (item instanceof ItemRecord) {
            return "Records";
        } else if (item instanceof ItemSkull) {
            return "Skulls";
        } else if (item instanceof ItemTool) {
            return "Tools";
        } else {
            return "Unknown";
        }
    }

    @Override
    public String getGroupName(Pair<ItemStack, Integer> object) {
        return getName(object);
    }
}

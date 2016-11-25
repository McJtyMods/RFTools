package mcjty.rftools.blocks.storage.sorters;

import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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
        return (o1, o2) -> compareCategory(o1, o2);
    }

    @Override
    public boolean isSameGroup(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String name1 = getName(o1);
        String name2 = getName(o2);
        return name1.equals(name2);
    }

    public static int compareCategory(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String name1 = getName(o1);
        String name2 = getName(o2);

        if (name1.equals(name2)) {
            return NameItemSorter.compareNames(o1, o2);
        }
        return name1.compareTo(name2);
    }

    private static String getName(Pair<ItemStack, Integer> object) {
        Item item = object.getKey().getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).getBlock();
            if (block != null && block.getClass() != null) {
                String category = ModularStorageConfiguration.getCategory(block.getClass());
                if (category != null) {
                    return category;
                }
            }
            return "Blocks";
        } else {
            if (item != null && item.getClass() != null) {
                String category = ModularStorageConfiguration.getCategory(item.getClass());
                if (category != null) {
                    return category;
                }
            }
            String displayName = object.getKey().getDisplayName();
            if (displayName.contains("Ingot")) {
                return "Ingots";
            }
            return "Unknown";
        }
    }

    @Override
    public String getGroupName(Pair<ItemStack, Integer> object) {
        return getName(object);
    }
}

package mcjty.rftools.blocks.storage.sorters;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public class ModItemSorter implements ItemSorter {
    @Override
    public String getName() {
        return "mod";
    }

    @Override
    public String getTooltip() {
        return "Sort on mod";
    }

    @Override
    public int getU() {
        return 15*16;
    }

    @Override
    public int getV() {
        return 0;
    }

    @Override
    public Comparator<Pair<ItemStack, Integer>> getComparator() {
        return new Comparator<Pair<ItemStack, Integer>>() {
            @Override
            public int compare(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
                return compareMod(o1, o2);
            }
        };
    }

    @Override
    public boolean isSameGroup(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String name1 = getMod(o1);
        String name2 = getMod(o2);
        return name1.equals(name2);
    }

    public static int compareMod(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String name1 = getMod(o1);
        String name2 = getMod(o2);

        if (name1.equals(name2)) {
            return NameItemSorter.compareNames(o1, o2);
        }
        return name1.compareTo(name2);
    }

    public static String getModidForBlock(Block block) {
        String nameForObject = GameData.getBlockRegistry().getNameForObject(block);
        if (nameForObject == null) {
            return "?";
        }
        String[] lst = StringUtils.split(nameForObject, ":");
        if (lst.length >= 2) {
            return lst[0];
        } else {
            return "?";
        }
    }

    public static String getModidForItem(Item item) {
        String nameForObject = GameData.getItemRegistry().getNameForObject(item);
        if (nameForObject == null) {
            return "?";
        }
        String[] lst = StringUtils.split(nameForObject, ":");
        if (lst.length >= 2) {
            return lst[0];
        } else {
            return "?";
        }
    }


    private static String getMod(Pair<ItemStack, Integer> object) {
        Item item = object.getKey().getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).field_150939_a;
            if (block != null) {
                return getModidForBlock(block);
            }
            return "Unknown";
        } else {
            if (item != null) {
                return getModidForItem(item);
            }
            return "Unknown";
        }
    }

    @Override
    public String getGroupName(Pair<ItemStack, Integer> object) {
        return getMod(object);
    }
}

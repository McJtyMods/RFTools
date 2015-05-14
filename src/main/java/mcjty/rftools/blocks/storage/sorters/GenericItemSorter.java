package mcjty.rftools.blocks.storage.sorters;

import mcjty.container.GenericBlock;
import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.screens.ModuleProvider;
import mcjty.rftools.blocks.storage.modules.TypeModule;
import mcjty.rftools.items.dimlets.KnownDimlet;
import mcjty.rftools.items.dimlets.UnknownDimlet;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.block.*;
import net.minecraft.item.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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

    private static Map<String,String> categoryMapper = new HashMap<String, String>();
    static {
        categoryMapper.put("powercrystals.minefactoryreloaded.block", "Machines");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.base.ItemFactoryBucket", "Buckets");

        categoryMapper.put("cofh.core.item.ItemBucket", "Buckets");
        categoryMapper.put("cofh.thermalexpansion.block.device", "Machines");


        categoryMapper.put("thermalexpansion.block.machine", "Machines");
        categoryMapper.put("thermalexpansion.block.cell", "Machines");

        categoryMapper.put("crazypants.enderio.machine", "Machines");
        categoryMapper.put("crazypants.enderio.item.skull", "Skulls");
        categoryMapper.put("crazypants.enderio.fluid.ItemBucketEio", "Buckets");

        categoryMapper.put("codechicken.microblock.ItemMicroPart", "Microblocks");

        categoryMapper.put("biomesoplenty.common.items.ItemBOPBucket", "Buckets");

        categoryMapper.put("extrabiomes.blocks.BlockCustomFlower", "Flowers");

        //buildcraft.builders
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
        } else if (item instanceof ItemBucket) {
            return "Buckets";
        } else if (item instanceof ItemSkull) {
            return "Skulls";
        } else if (item instanceof ItemTool || item instanceof ItemHoe || item instanceof ItemShears) {
            return "Tools";
        } else if (item instanceof ItemSword || item instanceof ItemBow) {
            return "Weapons";
        } else if (item instanceof KnownDimlet || item instanceof UnknownDimlet) {
            return "Dimlets";
        } else if (item instanceof ModuleProvider || item instanceof EnvModuleProvider || item instanceof StorageModuleItem || item instanceof TypeModule) {
            return "Modules";
        } else if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).field_150939_a;
            if (block instanceof BlockOre) {
                return "Ores";
            } else if (block instanceof BlockCake) {
                return "Food";
            } else if (block instanceof BlockSapling) {
                return "Saplings";
            } else if (block instanceof GenericBlock) {
                return "Machines";
            } else if (block instanceof BlockFlower) {
                return "Flowers";
            } else {
                if (block != null && block.getClass() != null) {
                    String joined = getCategory(block.getClass());
                    if (joined != null) return joined;
                }
                return "Blocks";
            }
        } else {
            if (item != null && item.getClass() != null) {
                String joined = getCategory(item.getClass());
                if (joined != null) return joined;
            }
            String displayName = object.getKey().getDisplayName();
            if (displayName.contains("Ingot")) {
                return "Ingots";
            }
            return "Unknown";
        }
    }

    private static String getCategory(Class cls) {
        String[] strings = StringUtils.split(cls.getCanonicalName(), ".");
        if (strings == null) {
            return null;
        }
        if (strings.length >= 3) {
            String[] part = new String[]{strings[0], strings[1], strings[2]};
            String joined = StringUtils.join(part, ".");
            if (categoryMapper.containsKey(joined)) {
                return categoryMapper.get(joined);
            }
            if (strings.length >= 4) {
                String[] part2 = new String[]{strings[0], strings[1], strings[2], strings[3]};
                String joined2 = StringUtils.join(part2, ".");
                if (categoryMapper.containsKey(joined2)) {
                    return categoryMapper.get(joined2);
                }
                if (strings.length >= 5) {
                    String[] part3 = new String[]{strings[0], strings[1], strings[2], strings[3], strings[4]};
                    String joined3 = StringUtils.join(part3, ".");
                    if (categoryMapper.containsKey(joined3)) {
                        return categoryMapper.get(joined3);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getGroupName(Pair<ItemStack, Integer> object) {
        return getName(object);
    }
}

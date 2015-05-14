package mcjty.rftools.blocks.storage;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ModularStorageConfiguration {
    public static final String CATEGORY_STORAGE = "storage";
    public static final String CATEGORY_STORAGE_CONFIG = "storageconfig";

    public static int itemListBackground = 0xff8090a0;
    public static int groupBackground = 0xffeedd33;
    public static int groupForeground = 0xff000000;
    public static Map<String,String> categoryMapper = new HashMap<String, String>();

    public static void init(Configuration cfg) {
        itemListBackground = cfg.get(CATEGORY_STORAGE, "itemListBackground", itemListBackground,
                "Color for the item background").getInt();
        groupBackground = cfg.get(CATEGORY_STORAGE, "groupBackground", groupBackground,
                "Background color for group lines").getInt();
        groupForeground = cfg.get(CATEGORY_STORAGE, "groupForeground", groupForeground,
                "Foreground color for group lines").getInt();

        ConfigCategory category = cfg.getCategory(CATEGORY_STORAGE_CONFIG);
        if (category.isEmpty()) {
            initCategories();
            for (Map.Entry<String, String> entry : categoryMapper.entrySet()) {
                category.put(entry.getKey(), new Property(entry.getKey(), entry.getValue(), Property.Type.STRING));
            }
        } else {
            for (Map.Entry<String, Property> entry : category.entrySet()) {
                categoryMapper.put(entry.getKey(), entry.getValue().getString());
            }

        }
    }

    public static String getCategory(Class cls) {
        if (cls == null) {
            return null;
        }
        String name = cls.getCanonicalName();
        if (name == null) {
            return null;
        }
        if (categoryMapper.containsKey(name)) {
            return categoryMapper.get(name);
        }

        String[] strings = StringUtils.split(name, ".");
        if (strings == null) {
            return null;
        }

        for (int i = strings.length-1 ; i >= 1 ; i--) {
            String joined = StringUtils.join(strings, '.', 0, i);
            if (categoryMapper.containsKey(joined)) {
                return categoryMapper.get(joined);
            }
        }

        name = getCategory(cls.getSuperclass());
        if (name != null) {
            return name;
        }

        for (Class intface : cls.getInterfaces()) {
            String cat = getCategory(intface);
            if (cat != null) {
                return null;
            }
        }

        return null;
    }

    static void initCategories() {
        categoryMapper.put("net.minecraft.item.ItemPotion", "Potions");
        categoryMapper.put("net.minecraft.item.ItemArmor", "Armor");
        categoryMapper.put("net.minecraft.item.ItemBook", "Books");
        categoryMapper.put("net.minecraft.item.ItemFood", "Food");
        categoryMapper.put("net.minecraft.item.ItemRecord", "Records");
        categoryMapper.put("net.minecraft.item.ItemBucket", "Buckets");
        categoryMapper.put("net.minecraft.item.ItemSkull", "Skulls");
        categoryMapper.put("net.minecraft.item.ItemTool", "Tools");
        categoryMapper.put("net.minecraft.item.ItemHoe", "Tools");
        categoryMapper.put("net.minecraft.item.ItemShears", "Tools");
        categoryMapper.put("net.minecraft.item.ItemSword", "Weapons");
        categoryMapper.put("net.minecraft.item.ItemBow", "Weapons");

        categoryMapper.put("net.minecraft.block.BlockOre", "Ores");
        categoryMapper.put("net.minecraft.block.BlockCake", "Food");
        categoryMapper.put("net.minecraft.block.BlockSapling", "Saplings");
        categoryMapper.put("net.minecraft.block.BlockFlower", "Flowers");

        categoryMapper.put("mcjty.container.GenericBlock", "Machines");

        categoryMapper.put("mcjty.rftools.items.dimlets.KnownDimlet", "Dimlets");
        categoryMapper.put("mcjty.rftools.items.dimlets.UnknownDimlet", "Dimlets");

        categoryMapper.put("mcjty.rftools.blocks.screens.ModuleProvider", "Modules");
        categoryMapper.put("mcjty.rftools.blocks.environmental.EnvModuleProvider", "Modules");
        categoryMapper.put("mcjty.rftools.items.storage.StorageModuleItem", "Modules");
        categoryMapper.put("mcjty.rftools.blocks.storage.modules.TypeModule", "Modules");


        categoryMapper.put("powercrystals.minefactoryreloaded.block", "Machines");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.base.ItemFactoryBucket", "Buckets");

        categoryMapper.put("cofh.core.item.ItemBucket", "Buckets");
        categoryMapper.put("cofh.thermalexpansion.block.device", "Machines");

        categoryMapper.put("buildcraft.builders", "Machines");

        categoryMapper.put("thermalexpansion.block.machine", "Machines");
        categoryMapper.put("thermalexpansion.block.cell", "Machines");

        categoryMapper.put("crazypants.enderio.machine", "Machines");
        categoryMapper.put("crazypants.enderio.item.skull", "Skulls");
        categoryMapper.put("crazypants.enderio.fluid.ItemBucketEio", "Buckets");

        categoryMapper.put("codechicken.microblock.ItemMicroPart", "Microblocks");

        categoryMapper.put("biomesoplenty.common.items.ItemBOPBucket", "Buckets");

        categoryMapper.put("extrabiomes.blocks.BlockCustomFlower", "Flowers");
    }
}

package mcjty.rftools.blocks.storage;

import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ModularStorageConfiguration {
    public static final String CATEGORY_STORAGE = "storage";
    public static final String CATEGORY_STORAGE_CONFIG = "storageconfig";

    public static int itemListBackground = 0xff8090a0;
    public static int groupBackground = 0xffeedd33;
    public static int groupForeground = 0xff000000;

    public static int REMOTE_MAXENERGY = 100000;
    public static int REMOTE_RECEIVEPERTICK = 300;

    public static int remoteShareLocal = 10;         // RF/tick to share this inventory locally (same dimension).
    public static int remoteShareGlobal = 50;        // RF/tick to share this inventory to other dimensions.

    public static Map<String,String> categoryMapper = new HashMap<String, String>();

    public static void init(Configuration cfg) {
        itemListBackground = cfg.get(CATEGORY_STORAGE, "itemListBackground", itemListBackground,
                "Color for the item background").getInt();
        groupBackground = cfg.get(CATEGORY_STORAGE, "groupBackground", groupBackground,
                "Background color for group lines").getInt();
        groupForeground = cfg.get(CATEGORY_STORAGE, "groupForeground", groupForeground,
                "Foreground color for group lines").getInt();

        REMOTE_MAXENERGY = cfg.get(CATEGORY_STORAGE, "remoteStorageMaxRF", REMOTE_MAXENERGY,
                "Maximum RF storage that the remote storage block can hold").getInt();
        REMOTE_RECEIVEPERTICK = cfg.get(CATEGORY_STORAGE, "remoteStorageRFPerTick", REMOTE_RECEIVEPERTICK,
                "RF per tick that the remote storage block can receive").getInt();
        remoteShareLocal = cfg.get(CATEGORY_STORAGE, "remoteShareLocal", remoteShareLocal,
                "RF/tick to share an inventory to the same dimension").getInt();
        remoteShareGlobal = cfg.get(CATEGORY_STORAGE, "remoteShareGlobal", remoteShareGlobal,
                "RF/tick to share an inventory to all dimensions").getInt();

        initCategories();
        ConfigCategory category = cfg.getCategory(CATEGORY_STORAGE_CONFIG);

        // Make a copy of the keys we already have.
        Set<String> keys = new HashSet<String>(categoryMapper.keySet());
        // Scan the config to see if there were updates.
        for (String key : keys) {
            categoryMapper.put(key, cfg.get(CATEGORY_STORAGE_CONFIG, key, categoryMapper.get(key)).getString());
        }
        // Now find all new keys in the config and add those.
        for (Map.Entry<String, Property> entry : category.entrySet()) {
            String key = entry.getKey();
            if (!categoryMapper.containsKey(key)) {
                categoryMapper.put(key, entry.getValue().getString());
            }
        }
    }



    public static void dumpClasses(boolean docode) {
        RFTools.log("#### Dumping item and block classification");
        for (Object o : Block.blockRegistry) {
            Block block = (Block) o;
            if (docode) {
                formateAsCode(block.getClass(), getCategory(block.getClass()));
            } else {
                formatClassification("B", BlockInfo.getReadableName(block, 0), block.getClass(), getCategory(block.getClass()));
            }
        }

        for (Object o : Item.itemRegistry) {
            Item item = (Item) o;
            if (docode) {
                formateAsCode(item.getClass(), getCategory(item.getClass()));
            } else {
                formatClassification("I", BlockInfo.getReadableName(item, 0), item.getClass(), getCategory(item.getClass()));
            }
        }
    }

    private static void formatClassification(String type, String name, Class clz, String group) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("%1$-1.1s Name:%2$-30.30s Class:%3$-50.50s Group:%4$-20.20s", type, name, clz.getCanonicalName(), group);
        RFTools.log(sb.toString());
    }

    private static void formateAsCode(Class clz, String group) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("categoryMapper.put(\"%1$s\", \"%2$s\");", clz.getCanonicalName(), group);
        RFTools.log(sb.toString());
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

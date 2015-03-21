package com.mcjty.rftools.blocks.spawner;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.*;

public class SpawnerConfiguration {
    public static final String CATEGORY_SPAWNER = "spawner";
    public static final String CATEGORY_MOBSPAWNAMOUNTS = "mobspawnamounts";
    public static final String CATEGORY_MOBSPAWNRF = "mobspawnrf";
    public static final String CATEGORY_LIVINGMATTER = "livingmatter";

    public static final Map<String,Integer> mobSpawnRf = new HashMap<String, Integer>();
    public static final Map<String,List<MobSpawnAmount>> mobSpawnAmounts = new HashMap<String, List<MobSpawnAmount>>();
    public static final Map<Object,Float> livingMatter = new HashMap<Object, Float>();

    public static final int MATERIALTYPE_KEY = 0;
    public static final int MATERIALTYPE_BULK = 1;
    public static final int MATERIALTYPE_LIVING = 2;

    public static int SPAWNER_MAXENERGY = 200000;
    public static int SPAWNER_RECEIVEPERTICK = 2000;

    public static int BEAMER_MAXENERGY = 200000;
    public static int BEAMER_RECEIVEPERTICK = 1000;
    public static int beamRfPerObject = 10;
    public static int beamBlocksPerSend = 10;
    public static int maxBeamDistance = 8;
    public static int maxMatterStorage = 64 * 100;

    public static void init(Configuration cfg) {
        SPAWNER_MAXENERGY = cfg.get(CATEGORY_SPAWNER, "spawnerMaxRF", SPAWNER_MAXENERGY,
                "Maximum RF storage that the spawner can hold").getInt();
        SPAWNER_RECEIVEPERTICK = cfg.get(CATEGORY_SPAWNER, "spawnerRFPerTick", SPAWNER_RECEIVEPERTICK,
                "RF per tick that the spawner can receive").getInt();

        BEAMER_MAXENERGY = cfg.get(CATEGORY_SPAWNER, "beamerMaxRF", BEAMER_MAXENERGY,
                "Maximum RF storage that the matter beamer can hold").getInt();
        BEAMER_RECEIVEPERTICK = cfg.get(CATEGORY_SPAWNER, "beamerRFPerTick", BEAMER_RECEIVEPERTICK,
                "RF per tick that the matter beamer can receive").getInt();
        beamRfPerObject = cfg.get(CATEGORY_SPAWNER, "beamerRfPerSend", beamRfPerObject,
                "RF per tick that the matter beamer will use for sending over a single object").getInt();
        beamBlocksPerSend = cfg.get(CATEGORY_SPAWNER, "beamerBlocksPerSend", beamBlocksPerSend,
                "The amount of blocks that the matter beamer will use send in one operation (every 10 ticks)").getInt();

        maxMatterStorage = cfg.get(CATEGORY_SPAWNER, "spawnerMaxMatterStorage", maxMatterStorage,
                "The maximum amount of energized matter that this spawner can store (per type)").getInt();
        maxBeamDistance = cfg.get(CATEGORY_SPAWNER, "maxBeamDistance", maxBeamDistance,
                "The maximum distance that a laser can travel between the beamer and the spawner").getInt();

        readLivingConfig(cfg);
    }

    public static void readLivingConfig(Configuration cfg) {
        ConfigCategory category = cfg.getCategory(CATEGORY_LIVINGMATTER);
        if (category.isEmpty()) {
            setupInitialLivingConfig(cfg);
        } else {
            for (Map.Entry<String, Property> entry : category.entrySet()) {
                String[] value = entry.getValue().getStringList();
                try {
                    String type = value[0];
                    String name = value[1];
                    Float factor = Float.parseFloat(value[2]);
                    if ("B".equals(type)) {
                        Object block = Block.blockRegistry.getObject(name);
                        livingMatter.put(block, factor);
                    } else {
                        Object item = Item.itemRegistry.getObject(name);
                        livingMatter.put(item, factor);
                    }
                } catch (Exception e) {
                    RFTools.logError("Badly formatted 'livingmatter' configuration option!");
                    return;
                }
            }
        }
    }

    private static void setupInitialLivingConfig(Configuration cfg) {
        int counter = 0;
        counter = addLiving(cfg, Blocks.leaves, counter, 1.0f);
        counter = addLiving(cfg, Blocks.leaves2, counter, 1.0f);
        counter = addLiving(cfg, Blocks.sapling, counter, 1.0f);
        counter = addLiving(cfg, Blocks.hay_block, counter, 1.0f);
        counter = addLiving(cfg, Blocks.melon_block, counter, 1.0f);
        counter = addLiving(cfg, Blocks.cactus, counter, 0.8f);
        counter = addLiving(cfg, Items.apple, counter, 1.0f);
        counter = addLiving(cfg, Items.wheat, counter, 1.5f);
        counter = addLiving(cfg, Items.wheat_seeds, counter, 0.8f);
        counter = addLiving(cfg, Items.potato, counter, 1.5f);
        counter = addLiving(cfg, Items.carrot, counter, 1.5f);
        counter = addLiving(cfg, Items.pumpkin_seeds, counter, 0.8f);
        counter = addLiving(cfg, Items.melon_seeds, counter, 0.8f);
        counter = addLiving(cfg, Items.beef, counter, 2.0f);
        counter = addLiving(cfg, Items.porkchop, counter, 2.0f);
        counter = addLiving(cfg, Items.chicken, counter, 2.0f);
    }

    private static int addLiving(Configuration cfg, Block block, int counter, float factor) {
        String name = Block.blockRegistry.getNameForObject(block);
        cfg.get(CATEGORY_LIVINGMATTER, "living." + counter, new String[] { "B", name, Float.toString(factor) });
        livingMatter.put(block, factor);
        return counter+1;
    }

    private static int addLiving(Configuration cfg, Item item, int counter, float factor) {
        String name = Item.itemRegistry.getNameForObject(item);
        cfg.get(CATEGORY_LIVINGMATTER, "living." + counter, new String[] { "I", name, Float.toString(factor) });
        livingMatter.put(item, factor);
        return counter+1;
    }

    public static void readMobSpawnAmountConfig(Configuration cfg) {
        addMobSpawnRF(cfg, "Bat", 100);
        addMobSpawnAmount(cfg, "Bat", MATERIALTYPE_KEY, Items.feather, 0, .1f);
        addMobSpawnAmount(cfg, "Bat", MATERIALTYPE_BULK, Blocks.dirt, 0, 20);
        addMobSpawnAmount(cfg, "Bat", MATERIALTYPE_LIVING, null, 0, 2);
        addMobSpawnRF(cfg, "Blaze", 1000);
        addMobSpawnAmount(cfg, "Blaze", MATERIALTYPE_KEY, Items.blaze_rod, 0, 0.1f);
        addMobSpawnAmount(cfg, "Blaze", MATERIALTYPE_BULK, Blocks.netherrack, 0, 60);
        addMobSpawnAmount(cfg, "Blaze", MATERIALTYPE_LIVING, null, 0, 9);
        addMobSpawnRF(cfg, "Cave Spider", 500);
        addMobSpawnAmount(cfg, "Cave Spider", MATERIALTYPE_KEY, Items.string, 0, 0.1f);
        addMobSpawnAmount(cfg, "Cave Spider", MATERIALTYPE_BULK, Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Cave Spider", MATERIALTYPE_LIVING, null, 0, 5);
        addMobSpawnRF(cfg, "Chicken", 500);
        addMobSpawnAmount(cfg, "Chicken", MATERIALTYPE_KEY, Items.feather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Chicken", MATERIALTYPE_BULK, Blocks.dirt, 0, 30);
        addMobSpawnAmount(cfg, "Chicken", MATERIALTYPE_LIVING, null, 0, 5);
        addMobSpawnRF(cfg, "Cow", 800);
        addMobSpawnAmount(cfg, "Cow", MATERIALTYPE_KEY, Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Cow", MATERIALTYPE_BULK, Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Cow", MATERIALTYPE_LIVING, null, 0, 8);
        addMobSpawnRF(cfg, "Creeper", 800);
        addMobSpawnAmount(cfg, "Creeper", MATERIALTYPE_KEY, Items.gunpowder, 0, 0.1f);
        addMobSpawnAmount(cfg, "Creeper", MATERIALTYPE_BULK, Blocks.dirt, 0, 60);
        addMobSpawnAmount(cfg, "Creeper", MATERIALTYPE_LIVING, null, 0, 5);
        addMobSpawnRF(cfg, "Dragon", 100000);
        addMobSpawnAmount(cfg, "Dragon", MATERIALTYPE_KEY, Items.experience_bottle, 0, 0.1f);
        addMobSpawnAmount(cfg, "Dragon", MATERIALTYPE_BULK, Blocks.end_stone, 0, 200);
        addMobSpawnAmount(cfg, "Dragon", MATERIALTYPE_LIVING, null, 0, 200);
        addMobSpawnRF(cfg, "Enderman", 2000);
        addMobSpawnAmount(cfg, "Enderman", MATERIALTYPE_KEY, Items.ender_pearl, 0, 0.1f);
        addMobSpawnAmount(cfg, "Enderman", MATERIALTYPE_BULK, Blocks.end_stone, 0, 20);
        addMobSpawnAmount(cfg, "Enderman", MATERIALTYPE_LIVING, null, 0, 11);
        addMobSpawnRF(cfg, "Ghast", 2000);
        addMobSpawnAmount(cfg, "Ghast", MATERIALTYPE_KEY, Items.ghast_tear, 0, 0.1f);
        addMobSpawnAmount(cfg, "Ghast", MATERIALTYPE_BULK, Blocks.netherrack, 0, 50);
        addMobSpawnAmount(cfg, "Ghast", MATERIALTYPE_LIVING, null, 0, 12);
        addMobSpawnRF(cfg, "Horse", 1000);
        addMobSpawnAmount(cfg, "Horse", MATERIALTYPE_KEY, Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Horse", MATERIALTYPE_BULK, Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Horse", MATERIALTYPE_LIVING, null, 0, 8);
        addMobSpawnRF(cfg, "Iron Golem", 2000);
        addMobSpawnAmount(cfg, "Iron Golem", MATERIALTYPE_KEY, Items.iron_ingot, 0, 0.1f);
        addMobSpawnAmount(cfg, "Iron Golem", MATERIALTYPE_BULK, Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Iron Golem", MATERIALTYPE_LIVING, 38, 0, 0.1f);     // Poppy
        addMobSpawnRF(cfg, "Magma Cube", 600);
        addMobSpawnAmount(cfg, "Magma Cube", MATERIALTYPE_KEY, Items.magma_cream, 0, 0.1f);
        addMobSpawnAmount(cfg, "Magma Cube", MATERIALTYPE_BULK, Blocks.netherrack, 0, 50);
        addMobSpawnAmount(cfg, "Magma Cube", MATERIALTYPE_LIVING, null, 0, 5);
        addMobSpawnRF(cfg, "Mooshroom", 800);
        addMobSpawnAmount(cfg, "Mooshroom", MATERIALTYPE_KEY, Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Mooshroom", MATERIALTYPE_BULK, Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Mooshroom", MATERIALTYPE_LIVING, null, 0, 8);
        addMobSpawnRF(cfg, "Ocelot", 800);
        addMobSpawnAmount(cfg, "Ocelot", MATERIALTYPE_KEY, Items.fish, 0, 0.1f);
        addMobSpawnAmount(cfg, "Ocelot", MATERIALTYPE_BULK, Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Ocelot", MATERIALTYPE_LIVING, null, 0, 8);
        addMobSpawnRF(cfg, "Pig", 800);
        addMobSpawnAmount(cfg, "Pig", MATERIALTYPE_KEY, Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Pig", MATERIALTYPE_BULK, Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Pig", MATERIALTYPE_LIVING, null, 0, 8);
        addMobSpawnRF(cfg, "Sheep", 800);
        addMobSpawnAmount(cfg, "Sheep", MATERIALTYPE_KEY, Blocks.wool, 0, 0.1f);
        addMobSpawnAmount(cfg, "Sheep", MATERIALTYPE_BULK, Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Sheep", MATERIALTYPE_LIVING, null, 0, 8);
        addMobSpawnRF(cfg, "Skeleton", 800);
        addMobSpawnAmount(cfg, "Skeleton", MATERIALTYPE_KEY, Items.bone, 0, 0.1f);
        addMobSpawnAmount(cfg, "Skeleton", MATERIALTYPE_BULK, Blocks.dirt, 0, 30);
        addMobSpawnAmount(cfg, "Skeleton", MATERIALTYPE_LIVING, null, 0, 5);
        addMobSpawnRF(cfg, "Slime", 600);
        addMobSpawnAmount(cfg, "Slime", MATERIALTYPE_KEY, Items.slime_ball, 0, 0.1f);
        addMobSpawnAmount(cfg, "Slime", MATERIALTYPE_BULK, Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Slime", MATERIALTYPE_LIVING, null, 0, 5);
        addMobSpawnRF(cfg, "Snowman", 600);
        addMobSpawnAmount(cfg, "Snowman", MATERIALTYPE_KEY, Items.snowball, 0, 0.1f);
        addMobSpawnAmount(cfg, "Snowman", MATERIALTYPE_BULK, Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Snowman", MATERIALTYPE_LIVING, null, 0, 4);
        addMobSpawnRF(cfg, "Spider", 500);
        addMobSpawnAmount(cfg, "Spider", MATERIALTYPE_KEY, Items.string, 0, 0.1f);
        addMobSpawnAmount(cfg, "Spider", MATERIALTYPE_BULK, Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Spider", MATERIALTYPE_LIVING, null, 0, 5);
        addMobSpawnRF(cfg, "Squid", 500);
        addMobSpawnAmount(cfg, "Squid", MATERIALTYPE_KEY, 351, 0, 0.1f);     // Ink sac
        addMobSpawnAmount(cfg, "Squid", MATERIALTYPE_BULK, Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Squid", MATERIALTYPE_LIVING, null, 0, 4);
        addMobSpawnRF(cfg, "Villager", 2000);
        addMobSpawnAmount(cfg, "Villager", MATERIALTYPE_KEY, Items.book, 0, 0.1f);
        addMobSpawnAmount(cfg, "Villager", MATERIALTYPE_BULK, Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Villager", MATERIALTYPE_LIVING, null, 0, 7);
        addMobSpawnRF(cfg, "Witch", 1200);
        addMobSpawnAmount(cfg, "Witch", MATERIALTYPE_KEY, Items.glass_bottle, 0, 0.1f);
        addMobSpawnAmount(cfg, "Witch", MATERIALTYPE_BULK, Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Witch", MATERIALTYPE_LIVING, null, 0, 7);
        addMobSpawnRF(cfg, "Wither", 20000);
        addMobSpawnAmount(cfg, "Wither", MATERIALTYPE_KEY, Items.nether_star, 0, 0.1f);
        addMobSpawnAmount(cfg, "Wither", MATERIALTYPE_BULK, Blocks.soul_sand, 0, 40);
        addMobSpawnAmount(cfg, "Wither", MATERIALTYPE_LIVING, null, 0, 6);
        addMobSpawnRF(cfg, "Wolf", 800);
        addMobSpawnAmount(cfg, "Wolf", MATERIALTYPE_KEY, Items.bone, 0, 0.1f);
        addMobSpawnAmount(cfg, "Wolf", MATERIALTYPE_BULK, Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Wolf", MATERIALTYPE_LIVING, null, 0, 8);
        addMobSpawnRF(cfg, "Zombie Pigman", 1200);
        addMobSpawnAmount(cfg, "Zombie Pigman", MATERIALTYPE_KEY, Items.gold_nugget, 0, 0.1f);
        addMobSpawnAmount(cfg, "Zombie Pigman", MATERIALTYPE_BULK, Blocks.netherrack, 0, 30);
        addMobSpawnAmount(cfg, "Zombie Pigman", MATERIALTYPE_LIVING, null, 0, 5);
        addMobSpawnRF(cfg, "Zombie", 800);
        addMobSpawnAmount(cfg, "Zombie", MATERIALTYPE_KEY, Items.rotten_flesh, 0, 0.1f);
        addMobSpawnAmount(cfg, "Zombie", MATERIALTYPE_BULK, Blocks.dirt, 0, 30);
        addMobSpawnAmount(cfg, "Zombie", MATERIALTYPE_LIVING, null, 0, 5);
    }

    public static void addMobSpawnRF(Configuration cfg, String name, int rf) {
        rf = cfg.get(CATEGORY_MOBSPAWNRF, name, rf).getInt();
        mobSpawnRf.put(name, rf);
    }

    public static void addMobSpawnAmount(Configuration cfg, String name, int materialType, Object object, int meta, float amount) {
        String type;
        String itemname;
        if (object instanceof Item) {
            type = "I";
            itemname = Item.itemRegistry.getNameForObject(object);
        } else if (object instanceof Block) {
            type = "B";
            itemname = Block.blockRegistry.getNameForObject(object);
        } else if (object instanceof String) {
            type = "S";
            itemname = (String) object;
        } else if (object instanceof Integer) {
            Integer id = (Integer) object;
            if (id < 0) {
                type = "B";
            } else {
                type = "I";
            }
            itemname = Integer.toString(id);
        } else {
            type = "L";
            itemname = "";
        }
        String[] splitted = cfg.get(CATEGORY_MOBSPAWNAMOUNTS, name + ".spawnamount." + materialType,
                new String[] { type, itemname, Integer.toString(meta), Float.toString(amount) }).getStringList();

        try {
            type = splitted[0];
            itemname = splitted[1];
            meta = Integer.parseInt(splitted[2]);
            amount = Float.parseFloat(splitted[3]);
        } catch (NumberFormatException e) {
            RFTools.logError("Something went wrong parsing the spawnamount setting for '" + name + "'!");
            return;
        }
        ItemStack stack = null;
        if ("I".equals(type)) {
            Item item;
            try {
                Integer id = Integer.parseInt(itemname);
                item = (Item) Item.itemRegistry.getObjectById(id);
            } catch (NumberFormatException e) {
                item = (Item) Item.itemRegistry.getObject(itemname);
            }
            stack = new ItemStack(item, 1, meta);
        } else if ("B".equals(type)) {
            Block block;
            try {
                Integer id = Integer.parseInt(itemname);
                block = (Block) Block.blockRegistry.getObjectById(id);
            } catch (NumberFormatException e) {
                block = (Block) Block.blockRegistry.getObject(itemname);
            }
            stack = new ItemStack(block, 1, meta);
        } else if ("S".equals(type)) {
        }

        List<MobSpawnAmount> list = mobSpawnAmounts.get(name);
        if (list == null) {
            list = new ArrayList<MobSpawnAmount>(3);
            list.add(null);
            list.add(null);
            list.add(null);
            mobSpawnAmounts.put(name, list);
        }
        list.set(materialType, new MobSpawnAmount(stack, amount));
    }

    public static class MobSpawnAmount {
        private final ItemStack object;
        private final float amount;

        public MobSpawnAmount(ItemStack object, float amount) {
            this.object = object;
            this.amount = amount;
        }

        public ItemStack getObject() {
            return object;
        }

        public float getAmount() {
            return amount;
        }

        public Float match(ItemStack stack) {
            if (object == null) {
                // Living?
                Item item = stack.getItem();
                if (item instanceof ItemBlock) {
                    Block block = ((ItemBlock) item).field_150939_a;
                    return livingMatter.get(block);
                } else {
                    return livingMatter.get(item);
                }
            }
            if (stack.isItemEqual(object)) {
                return 1.0f;
            }
            return null;
        }
    }
}

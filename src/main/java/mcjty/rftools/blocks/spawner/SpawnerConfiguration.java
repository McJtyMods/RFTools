package mcjty.rftools.blocks.spawner;

import mcjty.lib.varia.Logging;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerConfiguration {
    public static final String CATEGORY_SPAWNER = "spawner";
    public static final String CATEGORY_MOBSPAWNAMOUNTS = "mobspawnamounts";
    public static final String CATEGORY_MOBSPAWNRF = "mobspawnrf";
    public static final String CATEGORY_LIVINGMATTER = "livingmatter";

    public static final Map<String,Integer> mobSpawnRf = new HashMap<>();
    public static int defaultMobSpawnRf;
    public static final Map<String,List<MobSpawnAmount>> mobSpawnAmounts = new HashMap<>();
    public static final List<MobSpawnAmount> defaultSpawnAmounts = new ArrayList<>();
    public static final Map<Object,Float> livingMatter = new HashMap<>();

    public static final int MATERIALTYPE_KEY = 0;
    public static final int MATERIALTYPE_BULK = 1;
    public static final int MATERIALTYPE_LIVING = 2;

    public static int SPAWNER_MAXENERGY = 200000;
    public static int SPAWNER_RECEIVEPERTICK = 2000;

    public static int BEAMER_MAXENERGY = 200000;
    public static int BEAMER_RECEIVEPERTICK = 1000;
    public static int beamRfPerObject = 2000;
    public static int beamBlocksPerSend = 1;
    public static int maxBeamDistance = 8;
    public static int maxMatterStorage = 64 * 100;

//    public static int maxEntitiesAroundSpawner = 300;

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
                "The amount of blocks that the matter beamer will use send in one operation (every 20 ticks)").getInt();

        maxMatterStorage = cfg.get(CATEGORY_SPAWNER, "spawnerMaxMatterStorage", maxMatterStorage,
                "The maximum amount of energized matter that this spawner can store (per type)").getInt();
        maxBeamDistance = cfg.get(CATEGORY_SPAWNER, "maxBeamDistance", maxBeamDistance,
                "The maximum distance that a laser can travel between the beamer and the spawner").getInt();

//        maxEntitiesAroundSpawner = cfg.get(CATEGORY_SPAWNER, "maxEntitiesAroundSpawner", maxEntitiesAroundSpawner,
//                "If the number of entities around the spawner exceeds this number it will automatically stop spawning").getInt();

        readLivingConfig(cfg);
        readMobSpawnAmountConfig(cfg);
    }

    private static void readLivingConfig(Configuration cfg) {
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
                        Object block = Block.blockRegistry.getObject(new ResourceLocation(name));
                        livingMatter.put(block, factor);
                    } else {
                        Object item = Item.itemRegistry.getObject(new ResourceLocation(name));
                        livingMatter.put(item, factor);
                    }
                } catch (Exception e) {
                    Logging.logError("Badly formatted 'livingmatter' configuration option!");
                    return;
                }
            }
        }
    }

    private static void setupInitialLivingConfig(Configuration cfg) {
        int counter = 0;
        counter = addLiving(cfg, Blocks.leaves, counter, 0.5f);
        counter = addLiving(cfg, Blocks.leaves2, counter, 0.5f);
        counter = addLiving(cfg, Blocks.sapling, counter, 0.5f);
        counter = addLiving(cfg, Blocks.hay_block, counter, 1.0f);
        counter = addLiving(cfg, Blocks.melon_block, counter, 1.0f);
        counter = addLiving(cfg, Blocks.cactus, counter, 0.4f);
        counter = addLiving(cfg, Blocks.red_flower, counter, 0.3f);
        counter = addLiving(cfg, Blocks.yellow_flower, counter, 0.3f);
        counter = addLiving(cfg, Blocks.chorus_flower, counter, 1.1f);
        counter = addLiving(cfg, Blocks.brown_mushroom, counter, 0.4f);
        counter = addLiving(cfg, Blocks.red_mushroom, counter, 0.4f);
        counter = addLiving(cfg, Blocks.pumpkin, counter, 0.9f);
        counter = addLiving(cfg, Items.apple, counter, 1.0f);
        counter = addLiving(cfg, Items.wheat, counter, 1.1f);
        counter = addLiving(cfg, Items.wheat_seeds, counter, 0.4f);
        counter = addLiving(cfg, Items.potato, counter, 1.5f);
        counter = addLiving(cfg, Items.carrot, counter, 1.5f);
        counter = addLiving(cfg, Items.pumpkin_seeds, counter, 0.4f);
        counter = addLiving(cfg, Items.melon_seeds, counter, 0.4f);
        counter = addLiving(cfg, Items.beef, counter, 1.5f);
        counter = addLiving(cfg, Items.porkchop, counter, 1.5f);
        counter = addLiving(cfg, Items.chicken, counter, 1.5f);
        counter = addLiving(cfg, Items.beetroot, counter, 0.8f);
        counter = addLiving(cfg, Items.beetroot_seeds, counter, 0.4f);
        counter = addLiving(cfg, Items.chorus_fruit, counter, 1.5f);
        counter = addLiving(cfg, Items.fish, counter, 1.5f);
    }

    private static int addLiving(Configuration cfg, Block block, int counter, float factor) {
        cfg.get(CATEGORY_LIVINGMATTER, "living." + counter, new String[] { "B", block.getRegistryName().toString(), Float.toString(factor) });
        livingMatter.put(block, factor);
        return counter+1;
    }

    private static int addLiving(Configuration cfg, Item item, int counter, float factor) {
        cfg.get(CATEGORY_LIVINGMATTER, "living." + counter, new String[] { "I", item.getRegistryName().toString(), Float.toString(factor) });
        livingMatter.put(item, factor);
        return counter+1;
    }

    public static void readMobSpawnAmountConfig(Configuration cfg) {
        defaultMobSpawnRf = 10000;
        defaultSpawnAmounts.add(new MobSpawnAmount(new ItemStack(Items.diamond), 1.0f));
        defaultSpawnAmounts.add(new MobSpawnAmount(new ItemStack(Blocks.dirt), 20));
        defaultSpawnAmounts.add(new MobSpawnAmount(null, 120.0f));

        addMobSpawnRF(cfg, EntityBat.class, 100);
        addMobSpawnAmount(cfg, EntityBat.class, MATERIALTYPE_KEY, Items.feather, 0, .1f);
        addMobSpawnAmount(cfg, EntityBat.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntityBat.class, MATERIALTYPE_LIVING, null, 0, 10);
        addMobSpawnRF(cfg, EntityBlaze.class, 1000);
        addMobSpawnAmount(cfg, EntityBlaze.class, MATERIALTYPE_KEY, Items.blaze_rod, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityBlaze.class, MATERIALTYPE_BULK, Blocks.netherrack, 0, .5f);
        addMobSpawnAmount(cfg, EntityBlaze.class, MATERIALTYPE_LIVING, null, 0, 30);
        addMobSpawnRF(cfg, EntityCaveSpider.class, 500);
        addMobSpawnAmount(cfg, EntityCaveSpider.class, MATERIALTYPE_KEY, Items.string, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityCaveSpider.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntityCaveSpider.class, MATERIALTYPE_LIVING, null, 0, 10);
        addMobSpawnRF(cfg, EntityChicken.class, 500);
        addMobSpawnAmount(cfg, EntityChicken.class, MATERIALTYPE_KEY, Items.feather, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityChicken.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntityChicken.class, MATERIALTYPE_LIVING, null, 0, 15);
        addMobSpawnRF(cfg, EntityCow.class, 800);
        addMobSpawnAmount(cfg, EntityCow.class, MATERIALTYPE_KEY, Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityCow.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntityCow.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntityCreeper.class, 800);
        addMobSpawnAmount(cfg, EntityCreeper.class, MATERIALTYPE_KEY, Items.gunpowder, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityCreeper.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .5f);
        addMobSpawnAmount(cfg, EntityCreeper.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntityDragon.class, 100000);
        addMobSpawnAmount(cfg, EntityDragon.class, MATERIALTYPE_KEY, Items.experience_bottle, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityDragon.class, MATERIALTYPE_BULK, Blocks.end_stone, 0, 100);
        addMobSpawnAmount(cfg, EntityDragon.class, MATERIALTYPE_LIVING, null, 0, 200);
        addMobSpawnRF(cfg, EntityEnderman.class, 2000);
        addMobSpawnAmount(cfg, EntityEnderman.class, MATERIALTYPE_KEY, Items.ender_pearl, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityEnderman.class, MATERIALTYPE_BULK, Blocks.end_stone, 0, .5f);
        addMobSpawnAmount(cfg, EntityEnderman.class, MATERIALTYPE_LIVING, null, 0, 40);
        addMobSpawnRF(cfg, EntityGhast.class, 2000);
        addMobSpawnAmount(cfg, EntityGhast.class, MATERIALTYPE_KEY, Items.ghast_tear, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityGhast.class, MATERIALTYPE_BULK, Blocks.netherrack, 0, 1.0f);
        addMobSpawnAmount(cfg, EntityGhast.class, MATERIALTYPE_LIVING, null, 0, 50);
        addMobSpawnRF(cfg, EntityHorse.class, 1000);
        addMobSpawnAmount(cfg, EntityHorse.class, MATERIALTYPE_KEY, Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityHorse.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .5f);
        addMobSpawnAmount(cfg, EntityHorse.class, MATERIALTYPE_LIVING, null, 0, 30);
        addMobSpawnRF(cfg, EntityIronGolem.class, 2000);
        addMobSpawnAmount(cfg, EntityIronGolem.class, MATERIALTYPE_KEY, Items.iron_ingot, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityIronGolem.class, MATERIALTYPE_BULK, Blocks.dirt, 0, 6.0f);
        addMobSpawnAmount(cfg, EntityIronGolem.class, MATERIALTYPE_LIVING, Blocks.red_flower, 0, 0.5f);
        addMobSpawnRF(cfg, EntityMagmaCube.class, 600);
        addMobSpawnAmount(cfg, EntityMagmaCube.class, MATERIALTYPE_KEY, Items.magma_cream, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityMagmaCube.class, MATERIALTYPE_BULK, Blocks.netherrack, 0, .2f);
        addMobSpawnAmount(cfg, EntityMagmaCube.class, MATERIALTYPE_LIVING, null, 0, 10);
        addMobSpawnRF(cfg, EntityMooshroom.class, 800);
        addMobSpawnAmount(cfg, EntityMooshroom.class, MATERIALTYPE_KEY, Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityMooshroom.class, MATERIALTYPE_BULK, Blocks.dirt, 0, 1.0f);
        addMobSpawnAmount(cfg, EntityMooshroom.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntityOcelot.class, 800);
        addMobSpawnAmount(cfg, EntityOcelot.class, MATERIALTYPE_KEY, Items.fish, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityOcelot.class, MATERIALTYPE_BULK, Blocks.dirt, 0, 1.0f);
        addMobSpawnAmount(cfg, EntityOcelot.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntityPig.class, 800);
        addMobSpawnAmount(cfg, EntityPig.class, MATERIALTYPE_KEY, Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityPig.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntityPig.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntitySheep.class, 800);
        addMobSpawnAmount(cfg, EntitySheep.class, MATERIALTYPE_KEY, Blocks.wool, 0, 0.1f);
        addMobSpawnAmount(cfg, EntitySheep.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntitySheep.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntitySkeleton.class, 800);
        addMobSpawnAmount(cfg, EntitySkeleton.class, MATERIALTYPE_KEY, Items.bone, 0, 0.1f);
        addMobSpawnAmount(cfg, EntitySkeleton.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .5f);
        addMobSpawnAmount(cfg, EntitySkeleton.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntitySlime.class, 600);
        addMobSpawnAmount(cfg, EntitySlime.class, MATERIALTYPE_KEY, Items.slime_ball, 0, 0.1f);
        addMobSpawnAmount(cfg, EntitySlime.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .5f);
        addMobSpawnAmount(cfg, EntitySlime.class, MATERIALTYPE_LIVING, null, 0, 15);
        addMobSpawnRF(cfg, EntitySnowman.class, 600);
        addMobSpawnAmount(cfg, EntitySnowman.class, MATERIALTYPE_KEY, Items.snowball, 0, 0.1f);
        addMobSpawnAmount(cfg, EntitySnowman.class, MATERIALTYPE_BULK, Blocks.dirt, 0, 1.0f);
        addMobSpawnAmount(cfg, EntitySnowman.class, MATERIALTYPE_LIVING, null, 0, 15);
        addMobSpawnRF(cfg, EntitySpider.class, 500);
        addMobSpawnAmount(cfg, EntitySpider.class, MATERIALTYPE_KEY, Items.string, 0, 0.1f);
        addMobSpawnAmount(cfg, EntitySpider.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntitySpider.class, MATERIALTYPE_LIVING, null, 0, 15);
        addMobSpawnRF(cfg, EntitySquid.class, 500);
        addMobSpawnAmount(cfg, EntitySquid.class, MATERIALTYPE_KEY, 351, 0, 0.1f);     // Ink sac
        addMobSpawnAmount(cfg, EntitySquid.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .5f);
        addMobSpawnAmount(cfg, EntitySquid.class, MATERIALTYPE_LIVING, null, 0, 10);
        addMobSpawnRF(cfg, EntityVillager.class, 2000);
        addMobSpawnAmount(cfg, EntityVillager.class, MATERIALTYPE_KEY, Items.book, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityVillager.class, MATERIALTYPE_BULK, Blocks.dirt, 0, 5.0f);
        addMobSpawnAmount(cfg, EntityVillager.class, MATERIALTYPE_LIVING, null, 0, 30);
        addMobSpawnRF(cfg, EntityWitch.class, 1200);
        addMobSpawnAmount(cfg, EntityWitch.class, MATERIALTYPE_KEY, Items.glass_bottle, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityWitch.class, MATERIALTYPE_BULK, Blocks.dirt, 0, 1.0f);
        addMobSpawnAmount(cfg, EntityWitch.class, MATERIALTYPE_LIVING, null, 0, 30);
        addMobSpawnRF(cfg, EntityWither.class, 20000);
        addMobSpawnAmount(cfg, EntityWither.class, MATERIALTYPE_KEY, Items.nether_star, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityWither.class, MATERIALTYPE_BULK, Blocks.soul_sand, 0, 0.5f);
        addMobSpawnAmount(cfg, EntityWither.class, MATERIALTYPE_LIVING, null, 0, 100);
        addMobSpawnRF(cfg, EntityWolf.class, 800);
        addMobSpawnAmount(cfg, EntityWolf.class, MATERIALTYPE_KEY, Items.bone, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityWolf.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .5f);
        addMobSpawnAmount(cfg, EntityWolf.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntityPigZombie.class, 1200);
        addMobSpawnAmount(cfg, EntityPigZombie.class, MATERIALTYPE_KEY, Items.gold_nugget, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityPigZombie.class, MATERIALTYPE_BULK, Blocks.netherrack, 0, .5f);
        addMobSpawnAmount(cfg, EntityPigZombie.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntityZombie.class, 800);
        addMobSpawnAmount(cfg, EntityZombie.class, MATERIALTYPE_KEY, Items.rotten_flesh, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityZombie.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntityZombie.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntityGuardian.class, 1000);
        addMobSpawnAmount(cfg, EntityGuardian.class, MATERIALTYPE_KEY, Items.prismarine_shard, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityGuardian.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntityGuardian.class, MATERIALTYPE_LIVING, null, 0, 30);
        addMobSpawnRF(cfg, EntityShulker.class, 600);
        addMobSpawnAmount(cfg, EntityShulker.class, MATERIALTYPE_KEY, Items.ender_pearl, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityShulker.class, MATERIALTYPE_BULK, Blocks.end_stone, 0, .2f);
        addMobSpawnAmount(cfg, EntityShulker.class, MATERIALTYPE_LIVING, null, 0, 20);
        addMobSpawnRF(cfg, EntityEndermite.class, 400);
        addMobSpawnAmount(cfg, EntityEndermite.class, MATERIALTYPE_KEY, Items.ender_pearl, 0, 0.05f);
        addMobSpawnAmount(cfg, EntityEndermite.class, MATERIALTYPE_BULK, Blocks.end_stone, 0, .2f);
        addMobSpawnAmount(cfg, EntityEndermite.class, MATERIALTYPE_LIVING, null, 0, 10);
        addMobSpawnRF(cfg, EntitySilverfish.class, 400);
        addMobSpawnAmount(cfg, EntitySilverfish.class, MATERIALTYPE_KEY, Items.iron_ingot, 0, 0.05f);
        addMobSpawnAmount(cfg, EntitySilverfish.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntitySilverfish.class, MATERIALTYPE_LIVING, null, 0, 10);
        addMobSpawnRF(cfg, EntityRabbit.class, 300);
        addMobSpawnAmount(cfg, EntityRabbit.class, MATERIALTYPE_KEY, Items.rabbit_stew, 0, 0.1f);
        addMobSpawnAmount(cfg, EntityRabbit.class, MATERIALTYPE_BULK, Blocks.dirt, 0, .2f);
        addMobSpawnAmount(cfg, EntityRabbit.class, MATERIALTYPE_LIVING, null, 0, 10);
        addMobSpawnRF(cfg, "WitherSkeleton", 1500);
        addMobSpawnAmount(cfg, "WitherSkeleton", MATERIALTYPE_KEY, Items.bone, 0, 0.1f);
        addMobSpawnAmount(cfg, "WitherSkeleton", MATERIALTYPE_BULK, Blocks.netherrack, 0, .5f);
        addMobSpawnAmount(cfg, "WitherSkeleton", MATERIALTYPE_LIVING, null, 0, 30);
    }

    public static void addMobSpawnRF(Configuration cfg, Class<? extends EntityLiving> clazz, int rf) {
        String name = EntityList.classToStringMapping.get(clazz);
        addMobSpawnRF(cfg, name, rf);
    }

    private static void addMobSpawnRF(Configuration cfg, String name, int rf) {
        rf = cfg.get(CATEGORY_MOBSPAWNRF, name, rf).getInt();
        mobSpawnRf.put(name, rf);
    }

    public static void addMobSpawnAmount(Configuration cfg, Class<? extends EntityLiving> clazz, int materialType, Object object, int meta, float amount) {
        String name = EntityList.classToStringMapping.get(clazz);
        addMobSpawnAmount(cfg, name, materialType, object, meta, amount);
    }

    private static void addMobSpawnAmount(Configuration cfg, String name, int materialType, Object object, int meta, float amount) {
        List<MobSpawnAmount> list = mobSpawnAmounts.get(name);
        if (list == null) {
            list = new ArrayList<>(3);
            list.add(null);
            list.add(null);
            list.add(null);
            mobSpawnAmounts.put(name, list);
        }

        String type;
        ResourceLocation itemname;
        if (object instanceof Item) {
            type = "I";
            itemname = Item.itemRegistry.getNameForObject((Item) object);
        } else if (object instanceof Block) {
            type = "B";
            itemname = Block.blockRegistry.getNameForObject((Block) object);
        } else {
            type = "L";
            itemname = null;
        }
        String[] splitted = cfg.get(CATEGORY_MOBSPAWNAMOUNTS, name + ".spawnamount." + materialType,
                new String[] { type, itemname == null ? "" : itemname.toString(), Integer.toString(meta), Float.toString(amount) }).getStringList();
        try {
            type = splitted[0];
            String n = splitted[1];
            if ("".equals(n)) {
                itemname = null;
            } else {
                itemname = new ResourceLocation(n);
            }
            meta = Integer.parseInt(splitted[2]);
            amount = Float.parseFloat(splitted[3]);
        } catch (NumberFormatException e) {
            Logging.logError("Something went wrong parsing the spawnamount setting for '" + name + "'!");
            return;
        }

        ItemStack stack = null;
        if ("I".equals(type)) {
            Item item = Item.itemRegistry.getObject(itemname);
            stack = new ItemStack(item, 1, meta);
        } else if ("B".equals(type)) {
            Block block = Block.blockRegistry.getObject(itemname);
            stack = new ItemStack(block, 1, meta);
        } else if ("S".equals(type)) {
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
                    Block block = ((ItemBlock) item).getBlock();
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

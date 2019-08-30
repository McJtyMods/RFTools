package mcjty.rftools.blocks.spawner;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
    public static final Map<ResourceLocation,Float> livingMatter = new HashMap<>();

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

    // @todo 1.14
//    public static void init(Configuration cfg) {
//        cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_SPAWNER, "Settings for the spawner system");
//        cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_MOBSPAWNAMOUNTS, "Amount of materials needed to spawn mobs");
//        cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_MOBSPAWNRF, "Amount of RF needed to spawn mobs");
//        cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_LIVINGMATTER, "Blocks and items that are seen as living for the spawner");
//        SPAWNER_MAXENERGY = cfg.get(CATEGORY_SPAWNER, "spawnerMaxRF", SPAWNER_MAXENERGY,
//                "Maximum RF storage that the spawner can hold").getInt();
//        SPAWNER_RECEIVEPERTICK = cfg.get(CATEGORY_SPAWNER, "spawnerRFPerTick", SPAWNER_RECEIVEPERTICK,
//                "RF per tick that the spawner can receive").getInt();
//
//        BEAMER_MAXENERGY = cfg.get(CATEGORY_SPAWNER, "beamerMaxRF", BEAMER_MAXENERGY,
//                "Maximum RF storage that the matter beamer can hold").getInt();
//        BEAMER_RECEIVEPERTICK = cfg.get(CATEGORY_SPAWNER, "beamerRFPerTick", BEAMER_RECEIVEPERTICK,
//                "RF per tick that the matter beamer can receive").getInt();
//        beamRfPerObject = cfg.get(CATEGORY_SPAWNER, "beamerRfPerSend", beamRfPerObject,
//                "RF per tick that the matter beamer will use for sending over a single object").getInt();
//        beamBlocksPerSend = cfg.get(CATEGORY_SPAWNER, "beamerBlocksPerSend", beamBlocksPerSend,
//                "The amount of blocks that the matter beamer will use send in one operation (every 20 ticks)").getInt();
//
//        maxMatterStorage = cfg.get(CATEGORY_SPAWNER, "spawnerMaxMatterStorage", maxMatterStorage,
//                "The maximum amount of energized matter that this spawner can store (per type)").getInt();
//        maxBeamDistance = cfg.get(CATEGORY_SPAWNER, "maxBeamDistance", maxBeamDistance,
//                "The maximum distance that a laser can travel between the beamer and the spawner").getInt();
//
////        maxEntitiesAroundSpawner = cfg.get(CATEGORY_SPAWNER, "maxEntitiesAroundSpawner", maxEntitiesAroundSpawner,
////                "If the number of entities around the spawner exceeds this number it will automatically stop spawning").getInt();
//
//        readLivingConfig(cfg);
//
//        defaultMobSpawnRf = 10000;
//        defaultSpawnAmounts.add(new MobSpawnAmount(new ItemStack(Items.DIAMOND), 1.0f));
//        defaultSpawnAmounts.add(new MobSpawnAmount(new ItemStack(Blocks.DIRT), 20));
//        defaultSpawnAmounts.add(new MobSpawnAmount(ItemStack.EMPTY, 120.0f));
//
//        if (cfg.getCategory(CATEGORY_MOBSPAWNAMOUNTS).isEmpty()) {
//            setupInitialMobSpawnConfig(cfg);
//        }
//    }

//    private static void readLivingConfig(Configuration cfg) {
//        ConfigCategory category = cfg.getCategory(CATEGORY_LIVINGMATTER);
//        if (category.isEmpty()) {
//            setupInitialLivingConfig(cfg);
//        }
//        for (Map.Entry<String, Property> entry : category.entrySet()) {
//            String[] value = entry.getValue().getStringList();
//            try {
//                // value[0] is type and is no longer used
//                String name = value[1];
//                Float factor = Float.parseFloat(value[2]);
//                livingMatter.put(new ResourceLocation(name), factor);
//            } catch (Exception e) {
//                Logging.logError("Badly formatted 'livingmatter' configuration option!");
//                return;
//            }
//        }
//    }

//    private static void setupInitialLivingConfig(Configuration cfg) {
//        int counter = 0;
//        counter = addLiving(cfg, Blocks.LEAVES, counter, 0.5f);
//        counter = addLiving(cfg, Blocks.LEAVES2, counter, 0.5f);
//        counter = addLiving(cfg, Blocks.SAPLING, counter, 0.5f);
//        counter = addLiving(cfg, Blocks.HAY_BLOCK, counter, 1.5f);
//        counter = addLiving(cfg, Blocks.MELON_BLOCK, counter, 1.0f);
//        counter = addLiving(cfg, Blocks.CACTUS, counter, 0.4f);
//        counter = addLiving(cfg, Blocks.RED_FLOWER, counter, 0.3f);
//        counter = addLiving(cfg, Blocks.YELLOW_FLOWER, counter, 0.3f);
//        counter = addLiving(cfg, Blocks.CHORUS_FLOWER, counter, 1.1f);
//        counter = addLiving(cfg, Blocks.BROWN_MUSHROOM, counter, 0.4f);
//        counter = addLiving(cfg, Blocks.RED_MUSHROOM, counter, 0.4f);
//        counter = addLiving(cfg, Blocks.PUMPKIN, counter, 0.9f);
//        counter = addLiving(cfg, Blocks.VINE, counter, 0.4f);
//        counter = addLiving(cfg, Blocks.WATERLILY, counter, 0.4f);
//        counter = addLiving(cfg, Blocks.COCOA, counter, 0.8f);
//        counter = addLiving(cfg, Items.APPLE, counter, 1.0f);
//        counter = addLiving(cfg, Items.WHEAT, counter, 1.1f);
//        counter = addLiving(cfg, Items.WHEAT_SEEDS, counter, 0.4f);
//        counter = addLiving(cfg, Items.POTATO, counter, 1.5f);
//        counter = addLiving(cfg, Items.CARROT, counter, 1.5f);
//        counter = addLiving(cfg, Items.PUMPKIN_SEEDS, counter, 0.4f);
//        counter = addLiving(cfg, Items.MELON_SEEDS, counter, 0.4f);
//        counter = addLiving(cfg, Items.BEEF, counter, 1.5f);
//        counter = addLiving(cfg, Items.PORKCHOP, counter, 1.5f);
//        counter = addLiving(cfg, Items.MUTTON, counter, 1.5f);
//        counter = addLiving(cfg, Items.CHICKEN, counter, 1.5f);
//        counter = addLiving(cfg, Items.RABBIT, counter, 1.2f);
//        counter = addLiving(cfg, Items.RABBIT_FOOT, counter, 1.0f);
//        counter = addLiving(cfg, Items.RABBIT_HIDE, counter, 0.5f);
//        counter = addLiving(cfg, Items.BEETROOT, counter, 0.8f);
//        counter = addLiving(cfg, Items.BEETROOT_SEEDS, counter, 0.4f);
//        counter = addLiving(cfg, Items.CHORUS_FRUIT, counter, 1.5f);
//        counter = addLiving(cfg, Items.FISH, counter, 1.5f);
//        counter = addLiving(cfg, Items.REEDS, counter, 1f);
//    }

//    private static int addLiving(Configuration cfg, Block block, int counter, float factor) {
//        cfg.get(CATEGORY_LIVINGMATTER, "living." + counter, new String[] { "B", block.getRegistryName().toString(), Float.toString(factor) });
//        return counter+1;
//    }
//
//    private static int addLiving(Configuration cfg, Item item, int counter, float factor) {
//        cfg.get(CATEGORY_LIVINGMATTER, "living." + counter, new String[] { "I", item.getRegistryName().toString(), Float.toString(factor) });
//        return counter+1;
//    }
//
//    public static void readMobSpawnAmountConfig(Configuration cfg) {
//        ConfigCategory category = cfg.getCategory(CATEGORY_MOBSPAWNAMOUNTS);
//        for (Map.Entry<String, Property> entry : category.entrySet()) {
//            String key = entry.getKey();
//
//            String[] splitted = entry.getValue().getStringList();
//
//            int materialType;
//            if (key.endsWith(".spawnamount.0")) {
//                materialType = MATERIALTYPE_KEY;
//            } else if (key.endsWith(".spawnamount.1")) {
//                materialType = MATERIALTYPE_BULK;
//            } else {
//                materialType = MATERIALTYPE_LIVING;
//            }
//            String id = key.substring(0, key.indexOf(".spawnamount"));
//            setSpawnAmounts(id, materialType, splitted);
//        }
//
//        category = cfg.getCategory(CATEGORY_MOBSPAWNRF);
//        for (Map.Entry<String, Property> entry : category.entrySet()) {
//            String key = entry.getKey();
//            int rf = entry.getValue().getInt();
//            mobSpawnRf.put(key, rf);
//        }
//    }
//
//    private static void setupInitialMobSpawnConfig(Configuration cfg) {
//        addMobSpawnRF(cfg, EntityBat.class, 100);
//        addMobSpawnAmount(cfg, EntityBat.class, MATERIALTYPE_KEY, Items.FEATHER, 0, .1f);
//        addMobSpawnAmount(cfg, EntityBat.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityBat.class, MATERIALTYPE_LIVING, null, 0, 10);
//        addMobSpawnRF(cfg, EntityBlaze.class, 1000);
//        addMobSpawnAmount(cfg, EntityBlaze.class, MATERIALTYPE_KEY, Items.BLAZE_ROD, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityBlaze.class, MATERIALTYPE_BULK, Blocks.NETHERRACK, 0, .5f);
//        addMobSpawnAmount(cfg, EntityBlaze.class, MATERIALTYPE_LIVING, null, 0, 30);
//        addMobSpawnRF(cfg, EntityCaveSpider.class, 500);
//        addMobSpawnAmount(cfg, EntityCaveSpider.class, MATERIALTYPE_KEY, Items.STRING, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityCaveSpider.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityCaveSpider.class, MATERIALTYPE_LIVING, null, 0, 10);
//        addMobSpawnRF(cfg, EntityChicken.class, 500);
//        addMobSpawnAmount(cfg, EntityChicken.class, MATERIALTYPE_KEY, Items.FEATHER, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityChicken.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityChicken.class, MATERIALTYPE_LIVING, null, 0, 15);
//        addMobSpawnRF(cfg, EntityCow.class, 800);
//        addMobSpawnAmount(cfg, EntityCow.class, MATERIALTYPE_KEY, Items.LEATHER, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityCow.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityCow.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntityCreeper.class, 800);
//        addMobSpawnAmount(cfg, EntityCreeper.class, MATERIALTYPE_KEY, Items.GUNPOWDER, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityCreeper.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .5f);
//        addMobSpawnAmount(cfg, EntityCreeper.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntityDragon.class, 100000);
//        addMobSpawnAmount(cfg, EntityDragon.class, MATERIALTYPE_KEY, Items.EXPERIENCE_BOTTLE, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityDragon.class, MATERIALTYPE_BULK, Blocks.END_STONE, 0, 100);
//        addMobSpawnAmount(cfg, EntityDragon.class, MATERIALTYPE_LIVING, null, 0, 200);
//        addMobSpawnRF(cfg, EntityEnderman.class, 2000);
//        addMobSpawnAmount(cfg, EntityEnderman.class, MATERIALTYPE_KEY, Items.ENDER_PEARL, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityEnderman.class, MATERIALTYPE_BULK, Blocks.END_STONE, 0, .5f);
//        addMobSpawnAmount(cfg, EntityEnderman.class, MATERIALTYPE_LIVING, null, 0, 40);
//        addMobSpawnRF(cfg, EntityGhast.class, 2000);
//        addMobSpawnAmount(cfg, EntityGhast.class, MATERIALTYPE_KEY, Items.GHAST_TEAR, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityGhast.class, MATERIALTYPE_BULK, Blocks.NETHERRACK, 0, 1.0f);
//        addMobSpawnAmount(cfg, EntityGhast.class, MATERIALTYPE_LIVING, null, 0, 50);
//        addMobSpawnRF(cfg, EntityHorse.class, 1000);
//        addMobSpawnAmount(cfg, EntityHorse.class, MATERIALTYPE_KEY, Items.LEATHER, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityHorse.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .5f);
//        addMobSpawnAmount(cfg, EntityHorse.class, MATERIALTYPE_LIVING, null, 0, 30);
//        addMobSpawnRF(cfg, EntityIronGolem.class, 2000);
//        addMobSpawnAmount(cfg, EntityIronGolem.class, MATERIALTYPE_KEY, Items.IRON_INGOT, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityIronGolem.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, 6.0f);
//        addMobSpawnAmount(cfg, EntityIronGolem.class, MATERIALTYPE_LIVING, Blocks.RED_FLOWER, 0, 0.5f);
//        addMobSpawnRF(cfg, EntityMagmaCube.class, 600);
//        addMobSpawnAmount(cfg, EntityMagmaCube.class, MATERIALTYPE_KEY, Items.MAGMA_CREAM, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityMagmaCube.class, MATERIALTYPE_BULK, Blocks.NETHERRACK, 0, .2f);
//        addMobSpawnAmount(cfg, EntityMagmaCube.class, MATERIALTYPE_LIVING, null, 0, 10);
//        addMobSpawnRF(cfg, EntityMooshroom.class, 800);
//        addMobSpawnAmount(cfg, EntityMooshroom.class, MATERIALTYPE_KEY, Items.LEATHER, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityMooshroom.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, 1.0f);
//        addMobSpawnAmount(cfg, EntityMooshroom.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntityOcelot.class, 800);
//        addMobSpawnAmount(cfg, EntityOcelot.class, MATERIALTYPE_KEY, Items.FISH, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityOcelot.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, 1.0f);
//        addMobSpawnAmount(cfg, EntityOcelot.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntityPig.class, 800);
//        addMobSpawnAmount(cfg, EntityPig.class, MATERIALTYPE_KEY, Items.LEATHER, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityPig.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityPig.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntitySheep.class, 800);
//        addMobSpawnAmount(cfg, EntitySheep.class, MATERIALTYPE_KEY, Blocks.WOOL, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntitySheep.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntitySheep.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntitySkeleton.class, 800);
//        addMobSpawnAmount(cfg, EntitySkeleton.class, MATERIALTYPE_KEY, Items.BONE, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntitySkeleton.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .5f);
//        addMobSpawnAmount(cfg, EntitySkeleton.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntitySlime.class, 600);
//        addMobSpawnAmount(cfg, EntitySlime.class, MATERIALTYPE_KEY, Items.SLIME_BALL, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntitySlime.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .5f);
//        addMobSpawnAmount(cfg, EntitySlime.class, MATERIALTYPE_LIVING, null, 0, 15);
//        addMobSpawnRF(cfg, EntitySnowman.class, 600);
//        addMobSpawnAmount(cfg, EntitySnowman.class, MATERIALTYPE_KEY, Items.SNOWBALL, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntitySnowman.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, 1.0f);
//        addMobSpawnAmount(cfg, EntitySnowman.class, MATERIALTYPE_LIVING, null, 0, 15);
//        addMobSpawnRF(cfg, EntitySpider.class, 500);
//        addMobSpawnAmount(cfg, EntitySpider.class, MATERIALTYPE_KEY, Items.STRING, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntitySpider.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntitySpider.class, MATERIALTYPE_LIVING, null, 0, 15);
//        addMobSpawnRF(cfg, EntitySquid.class, 500);
//        addMobSpawnAmount(cfg, EntitySquid.class, MATERIALTYPE_KEY, Items.DYE, 0, 0.1f);     // Ink sac
//        addMobSpawnAmount(cfg, EntitySquid.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .5f);
//        addMobSpawnAmount(cfg, EntitySquid.class, MATERIALTYPE_LIVING, null, 0, 10);
//        addMobSpawnRF(cfg, EntityVillager.class, 2000);
//        addMobSpawnAmount(cfg, EntityVillager.class, MATERIALTYPE_KEY, Items.BOOK, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityVillager.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, 5.0f);
//        addMobSpawnAmount(cfg, EntityVillager.class, MATERIALTYPE_LIVING, null, 0, 30);
//        addMobSpawnRF(cfg, EntityWitch.class, 1200);
//        addMobSpawnAmount(cfg, EntityWitch.class, MATERIALTYPE_KEY, Items.GLASS_BOTTLE, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityWitch.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, 1.0f);
//        addMobSpawnAmount(cfg, EntityWitch.class, MATERIALTYPE_LIVING, null, 0, 30);
//        addMobSpawnRF(cfg, EntityWither.class, 20000);
//        addMobSpawnAmount(cfg, EntityWither.class, MATERIALTYPE_KEY, Items.NETHER_STAR, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityWither.class, MATERIALTYPE_BULK, Blocks.SOUL_SAND, 0, 0.5f);
//        addMobSpawnAmount(cfg, EntityWither.class, MATERIALTYPE_LIVING, null, 0, 100);
//        addMobSpawnRF(cfg, EntityWolf.class, 800);
//        addMobSpawnAmount(cfg, EntityWolf.class, MATERIALTYPE_KEY, Items.BONE, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityWolf.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .5f);
//        addMobSpawnAmount(cfg, EntityWolf.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntityPigZombie.class, 1200);
//        addMobSpawnAmount(cfg, EntityPigZombie.class, MATERIALTYPE_KEY, Items.GOLD_NUGGET, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityPigZombie.class, MATERIALTYPE_BULK, Blocks.NETHERRACK, 0, .5f);
//        addMobSpawnAmount(cfg, EntityPigZombie.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntityZombie.class, 800);
//        addMobSpawnAmount(cfg, EntityZombie.class, MATERIALTYPE_KEY, Items.ROTTEN_FLESH, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityZombie.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityZombie.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntityGuardian.class, 1000);
//        addMobSpawnAmount(cfg, EntityGuardian.class, MATERIALTYPE_KEY, Items.PRISMARINE_SHARD, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityGuardian.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityGuardian.class, MATERIALTYPE_LIVING, null, 0, 30);
//        addMobSpawnRF(cfg, EntityShulker.class, 600);
//        addMobSpawnAmount(cfg, EntityShulker.class, MATERIALTYPE_KEY, Items.ENDER_PEARL, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityShulker.class, MATERIALTYPE_BULK, Blocks.END_STONE, 0, .2f);
//        addMobSpawnAmount(cfg, EntityShulker.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, EntityEndermite.class, 400);
//        addMobSpawnAmount(cfg, EntityEndermite.class, MATERIALTYPE_KEY, Items.ENDER_PEARL, 0, 0.05f);
//        addMobSpawnAmount(cfg, EntityEndermite.class, MATERIALTYPE_BULK, Blocks.END_STONE, 0, .2f);
//        addMobSpawnAmount(cfg, EntityEndermite.class, MATERIALTYPE_LIVING, null, 0, 10);
//        addMobSpawnRF(cfg, EntitySilverfish.class, 400);
//        addMobSpawnAmount(cfg, EntitySilverfish.class, MATERIALTYPE_KEY, Items.IRON_INGOT, 0, 0.05f);
//        addMobSpawnAmount(cfg, EntitySilverfish.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntitySilverfish.class, MATERIALTYPE_LIVING, null, 0, 10);
//        addMobSpawnRF(cfg, EntityRabbit.class, 300);
//        addMobSpawnAmount(cfg, EntityRabbit.class, MATERIALTYPE_KEY, Items.RABBIT_STEW, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityRabbit.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityRabbit.class, MATERIALTYPE_LIVING, null, 0, 10);
//        addMobSpawnRF(cfg, EntityPolarBear.class, 1500);
//        addMobSpawnAmount(cfg, EntityPolarBear.class, MATERIALTYPE_KEY, Items.FISH, 0, 0.1f);
//        addMobSpawnAmount(cfg, EntityPolarBear.class, MATERIALTYPE_BULK, Blocks.DIRT, 0, .2f);
//        addMobSpawnAmount(cfg, EntityPolarBear.class, MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, "minecraft:wither_skeleton", 1500);
//        addMobSpawnAmount(cfg, "minecraft:wither_skeleton", MATERIALTYPE_KEY, Items.BONE, 0, 0.1f);
//        addMobSpawnAmount(cfg, "minecraft:wither_skeleton", MATERIALTYPE_BULK, Blocks.NETHERRACK, 0, .5f);
//        addMobSpawnAmount(cfg, "minecraft:wither_skeleton", MATERIALTYPE_LIVING, null, 0, 30);
//        addMobSpawnRF(cfg, "minecraft:stray", 800);
//        addMobSpawnAmount(cfg, "minecraft:stray", MATERIALTYPE_KEY, Items.BONE, 0, 0.1f);
//        addMobSpawnAmount(cfg, "minecraft:stray", MATERIALTYPE_BULK, Blocks.NETHERRACK, 0, .5f);
//        addMobSpawnAmount(cfg, "minecraft:stray", MATERIALTYPE_LIVING, null, 0, 20);
//        addMobSpawnRF(cfg, "WitherSkeleton", 1500);
//        addMobSpawnAmount(cfg, "WitherSkeleton", MATERIALTYPE_KEY, Items.BONE, 0, 0.1f);
//        addMobSpawnAmount(cfg, "WitherSkeleton", MATERIALTYPE_BULK, Blocks.NETHERRACK, 0, .5f);
//        addMobSpawnAmount(cfg, "WitherSkeleton", MATERIALTYPE_LIVING, null, 0, 30);
//        addMobSpawnRF(cfg, "StraySkeleton", 800);
//        addMobSpawnAmount(cfg, "StraySkeleton", MATERIALTYPE_KEY, Items.BONE, 0, 0.1f);
//        addMobSpawnAmount(cfg, "StraySkeleton", MATERIALTYPE_BULK, Blocks.NETHERRACK, 0, .5f);
//        addMobSpawnAmount(cfg, "StraySkeleton", MATERIALTYPE_LIVING, null, 0, 20);
//    }
//
//    public static void addMobSpawnRF(Configuration cfg, Class<? extends MobEntity> clazz, int rf) {
//        String id = EntityTools.findEntityIdByClass(clazz);
//        addMobSpawnRF(cfg, id, rf);
//    }
//
//    private static void addMobSpawnRF(Configuration cfg, String name, int rf) {
//        cfg.get(CATEGORY_MOBSPAWNRF, name, rf);
//    }
//
//    public static void addMobSpawnAmount(Configuration cfg, Class<? extends MobEntity> clazz, int materialType, Object object, int meta, float amount) {
//        String id = EntityTools.findEntityIdByClass(clazz);
//        addMobSpawnAmount(cfg, id, materialType, object, meta, amount);
//    }
//
//    private static void addMobSpawnAmount(Configuration cfg, String id, int materialType, Object object, int meta, float amount) {
//        String type;
//        ResourceLocation itemname;
//        if (object instanceof Item) {
//            type = "I";
//            itemname = Item.REGISTRY.getNameForObject((Item) object);
//        } else if (object instanceof Block) {
//            type = "B";
//            itemname = Block.REGISTRY.getNameForObject((Block) object);
//        } else {
//            type = "L";
//            itemname = null;
//        }
//        cfg.get(CATEGORY_MOBSPAWNAMOUNTS, id + ".spawnamount." + materialType,
//                new String[] { type, itemname == null ? "" : itemname.toString(), Integer.toString(meta), Float.toString(amount) });
//    }

//    private static void setSpawnAmounts(String id, int materialType, String[] splitted) {
//        String type;
//        ResourceLocation itemname;
//        int meta;
//        float amount;
//        try {
//            type = splitted[0];
//            String n = splitted[1];
//            if ("".equals(n)) {
//                itemname = null;
//            } else {
//                itemname = new ResourceLocation(n);
//            }
//            meta = Integer.parseInt(splitted[2]);
//            amount = Float.parseFloat(splitted[3]);
//        } catch (NumberFormatException e) {
//            Logging.logError("Something went wrong parsing the spawnamount setting for '" + id + "'!");
//            return;
//        }
//
//        ItemStack stack = ItemStack.EMPTY;
//        if ("I".equals(type)) {
//            Item item = Item.REGISTRY.getObject(itemname);
//            stack = new ItemStack(item, 1, meta);
//        } else if ("B".equals(type)) {
//            Block block = Block.REGISTRY.getObject(itemname);
//            stack = new ItemStack(block, 1, meta);
//        } else if ("S".equals(type)) {
//        }
//        List<MobSpawnAmount> list = mobSpawnAmounts.get(id);
//        if (list == null) {
//            list = new ArrayList<>(3);
//            list.add(null);
//            list.add(null);
//            list.add(null);
//            mobSpawnAmounts.put(id, list);
//        }
//
//        list.set(materialType, new MobSpawnAmount(stack, amount));
//    }

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
            if (object.isEmpty()) {
                // Living?
                Item item = stack.getItem();
                return livingMatter.get(item.getRegistryName());
            }
            if (stack.isItemEqual(object)) {
                return 1.0f;
            }
            return null;
        }
    }
}

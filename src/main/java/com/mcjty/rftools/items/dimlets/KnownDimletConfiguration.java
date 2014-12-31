package com.mcjty.rftools.items.dimlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.MobDescriptor;
import com.mcjty.rftools.dimension.SkyDescriptor;
import com.mcjty.rftools.dimension.world.types.*;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class KnownDimletConfiguration {
    public static final String CATEGORY_KNOWNDIMLETS = "knowndimlets";
    public static final String CATEGORY_RARITY = "rarity";
    public static final String CATEGORY_TYPERARIRTY = "typerarity";
    public static final String CATEGORY_TYPERFCREATECOST = "typerfcreatecost";
    public static final String CATEGORY_TYPERFMAINTAINCOST = "typerfmaintaincost";
    public static final String CATEGORY_TYPETICKCOST = "typetickcost";
    public static final String CATEGORY_MOBSPAWNS = "mobspawns";
    public static final String CATEGORY_GENERAL = "general";

    // This map keeps track of all known dimlets by id. Also the reverse map.
    public static final Map<Integer,DimletEntry> idToDimlet = new HashMap<Integer, DimletEntry>();
    public static final Map<DimletEntry,Integer> dimletToID = new HashMap<DimletEntry, Integer>();

    // Map the id of a dimlet to a display name.
    public static final Map<Integer,String> idToDisplayName = new HashMap<Integer, String>();

    // All craftable dimlets.
    public static final Set<Integer> craftableDimlets = new HashSet<Integer>();

    private static final Set<DimletKey> dimletBlackList = new HashSet<DimletKey>();
    private static final Set<DimletKey> dimletIsExpensive = new HashSet<DimletKey>();

    private static int lastId = 0;

    public static void initGeneralConfig(Configuration cfg) {
        DimletCosts.baseDimensionCreationCost = cfg.get(CATEGORY_GENERAL, "baseDimensionCreationCost", DimletCosts.baseDimensionCreationCost,
                "The base cost (in RF/tick) for creating a dimension").getInt();
        DimletCosts.baseDimensionMaintenanceCost = cfg.get(CATEGORY_GENERAL, "baseDimensionMaintenanceCost", DimletCosts.baseDimensionMaintenanceCost,
                "The base cost (in RF/tick) for maintaining a dimension").getInt();
        DimletCosts.baseDimensionTickCost = cfg.get(CATEGORY_GENERAL, "baseDimensionTickCost", DimletCosts.baseDimensionTickCost,
                "The base time (in ticks) for creating a dimension").getInt();
    }

    private static void registerDimletEntry(int id, DimletEntry dimletEntry) {
        idToDimlet.put(id, dimletEntry);
        dimletToID.put(dimletEntry, id);
        DimletRandomizer.dimletIds.add(id);
    }

    private static int registerDimlet(Configuration cfg, Map<DimletKey,Integer> idsInConfig, DimletKey key) {
        String k = "dimlet." + key.getType().getName() + "." + key.getName();
        int id;
        if (dimletBlackList.contains(key)) {
            // Blacklisted! But it is possibly overridden by the user in the config.
            id = cfg.get(CATEGORY_KNOWNDIMLETS, k, -1).getInt();
        } else {
            if (idsInConfig.containsKey(key)) {
                // This dimlet was already registered before. We use the id that was defined in the config.
                id = idsInConfig.get(key);
            } else {
                id = cfg.get(CATEGORY_KNOWNDIMLETS, k, lastId + 1).getInt();
                if (id > lastId) {
                    lastId = id;
                }
            }
        }

        if (id == -1) {
            return -1;
        }

        int rfCreateCost = checkCostConfig(cfg, "rfcreate.", key, DimletCosts.dimletBuiltinRfCreate, DimletCosts.typeRfCreateCost);
        int rfMaintainCost = checkCostConfig(cfg, "rfmaintain.", key, DimletCosts.dimletBuiltinRfMaintain, DimletCosts.typeRfMaintainCost);
        int tickCost = checkCostConfig(cfg, "ticks.", key, DimletCosts.dimletBuiltinTickCost, DimletCosts.typeTickCost);
        int rarity = checkCostConfig(cfg, "rarity.", key, DimletRandomizer.dimletBuiltinRarity, DimletRandomizer.typeRarity);
        boolean expensive = checkFlagConfig(cfg, "expensive.", key, dimletIsExpensive);

        DimletEntry entry = new DimletEntry(key, rfCreateCost, rfMaintainCost, tickCost, rarity, expensive);
        registerDimletEntry(id, entry);

        return id;
    }

    private static boolean checkFlagConfig(Configuration cfg, String prefix, DimletKey key, Set<DimletKey> builtinDefaults) {
        String k;
        k = prefix + key.getType().getName() + "." + key.getName();
        boolean defaultValue = builtinDefaults.contains(key);
        if (cfg.getCategory(CATEGORY_KNOWNDIMLETS).containsKey(k)) {
            return cfg.get(CATEGORY_KNOWNDIMLETS, k, defaultValue).getBoolean();
        } else {
            return defaultValue;
        }
    }

    private static int checkCostConfig(Configuration cfg, String prefix, DimletKey key, Map<DimletKey,Integer> builtinDefaults, Map<DimletType,Integer> typeDefaults) {
        String k;
        k = prefix + key.getType().getName() + "." + key.getName();
        Integer defaultValue = builtinDefaults.get(key);
        if (defaultValue == null) {
            defaultValue = typeDefaults.get(key.getType());
        }
        int cost;
        if (defaultValue.equals(typeDefaults.get(key.getType())) && !cfg.getCategory(CATEGORY_KNOWNDIMLETS).containsKey(k)) {
            // Still using default. We don't want to force a config value so we first check to see
            // if it is there.
            cost = defaultValue;
        } else {
            cost = cfg.get(CATEGORY_KNOWNDIMLETS, k, defaultValue).getInt();
        }
        return cost;
    }

    /**
     * This initializes all dimlets based on all loaded mods. This should be called from postInit.
     */
    public static void init(Configuration cfg) {
        readBuiltinConfig();

        Map<DimletKey,Integer> idsInConfig = getDimletsFromConfig(cfg);

        initBiomeItems(cfg, idsInConfig);

        int idDigit0 = initDigitItem(cfg, idsInConfig, 0);
        int idDigit1 = initDigitItem(cfg, idsInConfig, 1);
        int idDigit2 = initDigitItem(cfg, idsInConfig, 2);
        int idDigit3 = initDigitItem(cfg, idsInConfig, 3);
        int idDigit4 = initDigitItem(cfg, idsInConfig, 4);
        int idDigit5 = initDigitItem(cfg, idsInConfig, 5);
        int idDigit6 = initDigitItem(cfg, idsInConfig, 6);
        int idDigit7 = initDigitItem(cfg, idsInConfig, 7);
        int idDigit8 = initDigitItem(cfg, idsInConfig, 8);
        int idDigit9 = initDigitItem(cfg, idsInConfig, 9);

        int idMaterialNone = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_MATERIAL, "None"));
        idToDisplayName.put(idMaterialNone, DimletType.DIMLET_MATERIAL.getName() + " None Dimlet");
        DimletMapping.idToBlock.put(idMaterialNone, null);

        initMaterialItem(cfg, idsInConfig, Blocks.diamond_block);
        initMaterialItem(cfg, idsInConfig, Blocks.diamond_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.emerald_block);
        initMaterialItem(cfg, idsInConfig, Blocks.emerald_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.quartz_block);
        initMaterialItem(cfg, idsInConfig, Blocks.quartz_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.gold_block);
        initMaterialItem(cfg, idsInConfig, Blocks.gold_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.iron_block);
        initMaterialItem(cfg, idsInConfig, Blocks.iron_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.coal_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.lapis_block);
        initMaterialItem(cfg, idsInConfig, Blocks.lapis_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.coal_block);
        initMaterialItem(cfg, idsInConfig, Blocks.redstone_block);
        initMaterialItem(cfg, idsInConfig, Blocks.redstone_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.dirt);
        initMaterialItem(cfg, idsInConfig, Blocks.sandstone);
        initMaterialItem(cfg, idsInConfig, Blocks.end_stone);
        initMaterialItem(cfg, idsInConfig, Blocks.netherrack);
        initMaterialItem(cfg, idsInConfig, Blocks.cobblestone);
        initMaterialItem(cfg, idsInConfig, Blocks.obsidian);
        initMaterialItem(cfg, idsInConfig, Blocks.soul_sand);
        initMaterialItem(cfg, idsInConfig, Blocks.glass);
        initMaterialItem(cfg, idsInConfig, Blocks.stained_glass);
        initMaterialItem(cfg, idsInConfig, ModBlocks.dimensionalShardBlock);

        initFoliageItem(cfg, idsInConfig);

        int idLiquidNone = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_LIQUID, "None"));
        DimletMapping.idToFluid.put(idLiquidNone, null);
        idToDisplayName.put(idLiquidNone, DimletType.DIMLET_LIQUID.getName() + " None Dimlet");

        initLiquidItems(cfg, idsInConfig);

        initSpecialItem(cfg, idsInConfig, "Peaceful", SpecialType.SPECIAL_PEACEFUL);
        initSpecialItem(cfg, idsInConfig, "Efficiency", SpecialType.SPECIAL_EFFICIENCY);

        int idDefaultMobs = initMobItem(cfg, idsInConfig, null, "Default", 1, 1, 1, 1);
        initMobItem(cfg, idsInConfig, EntityZombie.class, "Zombie", 100, 8, 8, 60);
        initMobItem(cfg, idsInConfig, EntitySkeleton.class, "Skeleton", 100, 8, 8, 60);
        initMobItem(cfg, idsInConfig, EntityEnderman.class, "Enderman", 20, 2, 4, 20);
        initMobItem(cfg, idsInConfig, EntityBlaze.class, "Blaze", 20, 2, 4, 20);
        initMobItem(cfg, idsInConfig, EntityCreeper.class, "Creeper", 100, 8, 8, 60);
        initMobItem(cfg, idsInConfig, EntityCaveSpider.class, "Cave Spider", 100, 8, 8, 60);
        initMobItem(cfg, idsInConfig, EntityGhast.class, "Ghast", 20, 2, 4, 20);
        initMobItem(cfg, idsInConfig, EntityIronGolem.class, "Iron Golem", 20, 1, 2, 6);
        initMobItem(cfg, idsInConfig, EntityMagmaCube.class, "Magma Cube", 50, 2, 4, 30);
        initMobItem(cfg, idsInConfig, EntityPigZombie.class, "Zombie Pigman", 20, 2, 4, 10);
        initMobItem(cfg, idsInConfig, EntitySlime.class, "Slime", 50, 2, 4, 30);
        initMobItem(cfg, idsInConfig, EntitySnowman.class, "Snowman", 50, 2, 4, 30);
        initMobItem(cfg, idsInConfig, EntitySpider.class, "Spider", 100, 8, 8, 60);
        initMobItem(cfg, idsInConfig, EntityWitch.class, "Witch", 10, 1, 1, 20);
        initMobItem(cfg, idsInConfig, EntityBat.class, "Bat", 10, 8, 8, 20);
        initMobItem(cfg, idsInConfig, EntityChicken.class, "Chicken", 10, 3, 4, 40);
        initMobItem(cfg, idsInConfig, EntityCow.class, "Cow", 10, 3, 4, 40);
        initMobItem(cfg, idsInConfig, EntityHorse.class, "Horse", 10, 3, 4, 40);
        initMobItem(cfg, idsInConfig, EntityMooshroom.class, "Mooshroom", 10, 3, 4, 40);
        initMobItem(cfg, idsInConfig, EntityOcelot.class, "Ocelot", 5, 2, 3, 20);
        initMobItem(cfg, idsInConfig, EntityPig.class, "Pig", 10, 3, 4, 40);
        initMobItem(cfg, idsInConfig, EntitySheep.class, "Sheep", 10, 3, 4, 40);
        initMobItem(cfg, idsInConfig, EntitySquid.class, "Squid", 10, 3, 4, 40);
        initMobItem(cfg, idsInConfig, EntityWolf.class, "Wolf", 10, 3, 4, 20);

        int idNormalDay = initSkyItem(cfg, idsInConfig, "Normal Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.0f).build());
        initSkyItem(cfg, idsInConfig, "Bright Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.5f).build());
        initSkyItem(cfg, idsInConfig, "Dark Day", new SkyDescriptor.Builder().sunBrightnessFactor(0.4f).skyColorFactor(0.6f, 0.6f, 0.6f).build());
        int idNormalNight = initSkyItem(cfg, idsInConfig, "Normal Night", new SkyDescriptor.Builder().starBrightnessFactor(1.0f).build());
        initSkyItem(cfg, idsInConfig, "Bright Night", new SkyDescriptor.Builder().starBrightnessFactor(1.5f).build());
        initSkyItem(cfg, idsInConfig, "Dark Night", new SkyDescriptor.Builder().starBrightnessFactor(0.4f).build());
        initSkyItem(cfg, idsInConfig, "Red Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 0.2f).build());
        initSkyItem(cfg, idsInConfig, "Green Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 0.2f).build());
        initSkyItem(cfg, idsInConfig, "Blue Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 0.2f, 1.0f).build());
        initSkyItem(cfg, idsInConfig, "Yellow Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 1.0f, 0.2f).build());
        initSkyItem(cfg, idsInConfig, "Cyan Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 1.0f).build());
        initSkyItem(cfg, idsInConfig, "Purple Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 1.0f).build());

        int idStructureNone = initStructureItem(cfg, idsInConfig, "None", StructureType.STRUCTURE_NONE);
        initStructureItem(cfg, idsInConfig, "Village", StructureType.STRUCTURE_VILLAGE);
        initStructureItem(cfg, idsInConfig, "Stronghold", StructureType.STRUCTURE_STRONGHOLD);
        initStructureItem(cfg, idsInConfig, "Dungeon", StructureType.STRUCTURE_DUNGEON);
        initStructureItem(cfg, idsInConfig, "Fortress", StructureType.STRUCTURE_FORTRESS);
        initStructureItem(cfg, idsInConfig, "Mineshaft", StructureType.STRUCTURE_MINESHAFT);
        initStructureItem(cfg, idsInConfig, "Scattered", StructureType.STRUCTURE_SCATTERED);

        int idTerrainVoid = initTerrainItem(cfg, idsInConfig, "Void", TerrainType.TERRAIN_VOID);
        int idTerrainFlat = initTerrainItem(cfg, idsInConfig, "Flat", TerrainType.TERRAIN_FLAT);
        initTerrainItem(cfg, idsInConfig, "Amplified", TerrainType.TERRAIN_AMPLIFIED);
        initTerrainItem(cfg, idsInConfig, "Normal", TerrainType.TERRAIN_NORMAL);
        initTerrainItem(cfg, idsInConfig, "Cave World", TerrainType.TERRAIN_CAVES);
        initTerrainItem(cfg, idsInConfig, "Island", TerrainType.TERRAIN_ISLAND);

        int idFeatureNone = initFeatureItem(cfg, idsInConfig, "None", FeatureType.FEATURE_NONE);
        initFeatureItem(cfg, idsInConfig, "Caves", FeatureType.FEATURE_CAVES);
        initFeatureItem(cfg, idsInConfig, "Ravines", FeatureType.FEATURE_RAVINES);
        initFeatureItem(cfg, idsInConfig, "Spheres", FeatureType.FEATURE_SPHERES);
        initFeatureItem(cfg, idsInConfig, "Oregen", FeatureType.FEATURE_OREGEN);
        initFeatureItem(cfg, idsInConfig, "Lakes", FeatureType.FEATURE_LAKES);
        initFeatureItem(cfg, idsInConfig, "Tendrils", FeatureType.FEATURE_TENDRILS);
        initFeatureItem(cfg, idsInConfig, "Canyons", FeatureType.FEATURE_CANYONS);

        int idEffectNone = initEffectItem(cfg, idsInConfig, "None", EffectType.EFFECT_NONE);
        initEffectItem(cfg, idsInConfig, "Poison", EffectType.EFFECT_POISON);
        initEffectItem(cfg, idsInConfig, "Poison II", EffectType.EFFECT_POISON2);
        initEffectItem(cfg, idsInConfig, "Poison III", EffectType.EFFECT_POISON3);
//        initEffectItem(cfg, idsInConfig, "No Gravity", EffectType.EFFECT_NOGRAVITY);
        initEffectItem(cfg, idsInConfig, "Regeneration", EffectType.EFFECT_REGENERATION);
        initEffectItem(cfg, idsInConfig, "Regeneration II", EffectType.EFFECT_REGENERATION2);
        initEffectItem(cfg, idsInConfig, "Regeneration III", EffectType.EFFECT_REGENERATION3);
        initEffectItem(cfg, idsInConfig, "Slowness", EffectType.EFFECT_MOVESLOWDOWN);
        initEffectItem(cfg, idsInConfig, "Slowness II", EffectType.EFFECT_MOVESLOWDOWN2);
        initEffectItem(cfg, idsInConfig, "Slowness III", EffectType.EFFECT_MOVESLOWDOWN3);
        initEffectItem(cfg, idsInConfig, "Speed", EffectType.EFFECT_MOVESPEED);
        initEffectItem(cfg, idsInConfig, "Speed II", EffectType.EFFECT_MOVESPEED2);
        initEffectItem(cfg, idsInConfig, "Speed III", EffectType.EFFECT_MOVESPEED3);
        initEffectItem(cfg, idsInConfig, "Mining Fatigue", EffectType.EFFECT_DIGSLOWDOWN);
        initEffectItem(cfg, idsInConfig, "Mining Fatigue II", EffectType.EFFECT_DIGSLOWDOWN2);
        initEffectItem(cfg, idsInConfig, "Mining Fatigue III", EffectType.EFFECT_DIGSLOWDOWN3);
        initEffectItem(cfg, idsInConfig, "Haste", EffectType.EFFECT_DIGSPEED);
        initEffectItem(cfg, idsInConfig, "Haste II", EffectType.EFFECT_DIGSPEED2);
        initEffectItem(cfg, idsInConfig, "Haste III", EffectType.EFFECT_DIGSPEED3);
        initEffectItem(cfg, idsInConfig, "Damage Boost", EffectType.EFFECT_DAMAGEBOOST);
        initEffectItem(cfg, idsInConfig, "Damage Boost II", EffectType.EFFECT_DAMAGEBOOST2);
        initEffectItem(cfg, idsInConfig, "Damage Boost III", EffectType.EFFECT_DAMAGEBOOST3);
        initEffectItem(cfg, idsInConfig, "Heal", EffectType.EFFECT_HEAL);
        initEffectItem(cfg, idsInConfig, "Harm", EffectType.EFFECT_HARM);
        initEffectItem(cfg, idsInConfig, "Jump", EffectType.EFFECT_JUMP);
        initEffectItem(cfg, idsInConfig, "Jump II", EffectType.EFFECT_JUMP2);
        initEffectItem(cfg, idsInConfig, "Jump III", EffectType.EFFECT_JUMP3);
        initEffectItem(cfg, idsInConfig, "Confusion", EffectType.EFFECT_CONFUSION);
        initEffectItem(cfg, idsInConfig, "Resistance", EffectType.EFFECT_RESISTANCE);
        initEffectItem(cfg, idsInConfig, "Resistance II", EffectType.EFFECT_RESISTANCE2);
        initEffectItem(cfg, idsInConfig, "Resistance III", EffectType.EFFECT_RESISTANCE3);
        initEffectItem(cfg, idsInConfig, "Fire Resistance", EffectType.EFFECT_FIRERESISTANCE);
        initEffectItem(cfg, idsInConfig, "Water Breathing", EffectType.EFFECT_WATERBREATHING);
        initEffectItem(cfg, idsInConfig, "Invisibility", EffectType.EFFECT_INVISIBILITY);
        initEffectItem(cfg, idsInConfig, "Blindness", EffectType.EFFECT_BLINDNESS);
        initEffectItem(cfg, idsInConfig, "Nightvision", EffectType.EFFECT_NIGHTVISION);
        initEffectItem(cfg, idsInConfig, "Hunger", EffectType.EFFECT_HUNGER);
        initEffectItem(cfg, idsInConfig, "Hunger II", EffectType.EFFECT_HUNGER2);
        initEffectItem(cfg, idsInConfig, "Hunger III", EffectType.EFFECT_HUNGER3);
        initEffectItem(cfg, idsInConfig, "Weakness", EffectType.EFFECT_WEAKNESS);
        initEffectItem(cfg, idsInConfig, "Weakness II", EffectType.EFFECT_WEAKNESS2);
        initEffectItem(cfg, idsInConfig, "Weakness III", EffectType.EFFECT_WEAKNESS3);
        initEffectItem(cfg, idsInConfig, "Wither", EffectType.EFFECT_WITHER);
        initEffectItem(cfg, idsInConfig, "Wither II", EffectType.EFFECT_WITHER2);
        initEffectItem(cfg, idsInConfig, "Wither III", EffectType.EFFECT_WITHER3);
        initEffectItem(cfg, idsInConfig, "Health Boost", EffectType.EFFECT_HEALTHBOOST);
        initEffectItem(cfg, idsInConfig, "Health Boost II", EffectType.EFFECT_HEALTHBOOST2);
        initEffectItem(cfg, idsInConfig, "Health Boost III", EffectType.EFFECT_HEALTHBOOST3);
        initEffectItem(cfg, idsInConfig, "Absorption", EffectType.EFFECT_ABSORPTION);
        initEffectItem(cfg, idsInConfig, "Absorption II", EffectType.EFFECT_ABSORPTION2);
        initEffectItem(cfg, idsInConfig, "Absorption III", EffectType.EFFECT_ABSORPTION3);
        initEffectItem(cfg, idsInConfig, "Saturation", EffectType.EFFECT_SATURATION);
        initEffectItem(cfg, idsInConfig, "Saturation II", EffectType.EFFECT_SATURATION2);
        initEffectItem(cfg, idsInConfig, "Saturation III", EffectType.EFFECT_SATURATION3);

        int idNormalTime = initTimeItem(cfg, idsInConfig, "Normal", null, null);
        initTimeItem(cfg, idsInConfig, "Noon", 0.0f, null);
        initTimeItem(cfg, idsInConfig, "Midnight", 0.5f, null);
        initTimeItem(cfg, idsInConfig, "Morning", 0.2f, null);
        initTimeItem(cfg, idsInConfig, "Evening", 0.75f, null);
        initTimeItem(cfg, idsInConfig, "Fast", null, 2.0f);
        initTimeItem(cfg, idsInConfig, "Slow", null, 0.5f);

        ModItems.knownDimlet = new KnownDimlet();
        ModItems.knownDimlet.setUnlocalizedName("KnownDimlet");
        ModItems.knownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        GameRegistry.registerItem(ModItems.knownDimlet, "knownDimlet");

        GameRegistry.addRecipe(new ItemStack(ModItems.dimletTemplate), "sss", "sps", "sss", 's', ModItems.dimensionalShard, 'p', Items.paper);

        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idEffectNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.apple, 'p', Items.paper);
        craftableDimlets.add(idEffectNone);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idFeatureNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.string, 'p', Items.paper);
        craftableDimlets.add(idFeatureNone);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idStructureNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bone, 'p', Items.paper);
        craftableDimlets.add(idStructureNone);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idTerrainVoid), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', Items.paper);
        craftableDimlets.add(idTerrainVoid);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idTerrainFlat), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', ModItems.dimletTemplate);
        craftableDimlets.add(idTerrainFlat);

        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idMaterialNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Blocks.dirt, 'p', Items.paper);
        craftableDimlets.add(idMaterialNone);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idLiquidNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bucket, 'p', Items.paper);
        craftableDimlets.add(idLiquidNone);

        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idNormalDay), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.glowstone_dust, 'p', ModItems.dimletTemplate);
        craftableDimlets.add(idNormalDay);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idNormalNight), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.coal, 'p', Items.paper);
        craftableDimlets.add(idNormalNight);

        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDefaultMobs), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.rotten_flesh, 'p', ModItems.dimletTemplate);
        craftableDimlets.add(idDefaultMobs);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idNormalTime), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.clock, 'p', ModItems.dimletTemplate);
        craftableDimlets.add(idNormalTime);

        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit0), " r ", "rtr", "ppp", 'r', Items.redstone, 't', redstoneTorch, 'p', Items.paper);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit0), "   ", " 9 ", "   ", '9', new ItemStack(ModItems.knownDimlet, 1, idDigit9));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit1), "   ", " 0 ", "   ", '0', new ItemStack(ModItems.knownDimlet, 1, idDigit0));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit2), "   ", " 1 ", "   ", '1', new ItemStack(ModItems.knownDimlet, 1, idDigit1));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit3), "   ", " 2 ", "   ", '2', new ItemStack(ModItems.knownDimlet, 1, idDigit2));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit4), "   ", " 3 ", "   ", '3', new ItemStack(ModItems.knownDimlet, 1, idDigit3));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit5), "   ", " 4 ", "   ", '4', new ItemStack(ModItems.knownDimlet, 1, idDigit4));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit6), "   ", " 5 ", "   ", '5', new ItemStack(ModItems.knownDimlet, 1, idDigit5));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit7), "   ", " 6 ", "   ", '6', new ItemStack(ModItems.knownDimlet, 1, idDigit6));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit8), "   ", " 7 ", "   ", '7', new ItemStack(ModItems.knownDimlet, 1, idDigit7));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idDigit9), "   ", " 8 ", "   ", '8', new ItemStack(ModItems.knownDimlet, 1, idDigit8));
        craftableDimlets.add(idDigit0);
        craftableDimlets.add(idDigit1);
        craftableDimlets.add(idDigit2);
        craftableDimlets.add(idDigit3);
        craftableDimlets.add(idDigit4);
        craftableDimlets.add(idDigit5);
        craftableDimlets.add(idDigit6);
        craftableDimlets.add(idDigit7);
        craftableDimlets.add(idDigit8);
        craftableDimlets.add(idDigit9);

        DimletRandomizer.setupWeightedRandomList(cfg);
        setupChestLoot();
    }

    /**
     * Get all dimlets which are currently already registered in the config file.
     */
    private static Map<DimletKey, Integer> getDimletsFromConfig(Configuration cfg) {
        Map<DimletKey, Integer> idsInConfig;
        idsInConfig = new HashMap<DimletKey, Integer>();

        ConfigCategory category = cfg.getCategory(CATEGORY_KNOWNDIMLETS);
        for (Map.Entry<String, Property> entry : category.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("dimlet.")) {
                int indexDotAfterType = key.indexOf('.', 7);
                String typeName = key.substring(7, indexDotAfterType);
                DimletType type = DimletType.getTypeByName(typeName);
                String name = key.substring(indexDotAfterType+1);
                Integer id = entry.getValue().getInt();
                if (id != -1) {
                    if (id > lastId) {
                        lastId = id;
                    }
                    idsInConfig.put(new DimletKey(type, name), id);
                }
            }
        }
        return idsInConfig;
    }

    private static int initDigitItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, int digit) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_DIGIT, "" + digit));
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_DIGIT.getName() + " " + digit + " Dimlet");
            DimletMapping.idToDigit.put(id, "" + digit);
        }
        return id;
    }

    private static void initMaterialItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, Block block) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_MATERIAL, block.getUnlocalizedName()));
        if (id != -1) {
            ItemStack stack = new ItemStack(block);
            idToDisplayName.put(id, DimletType.DIMLET_MATERIAL.getName() + " " + stack.getDisplayName() + " Dimlet");
            DimletMapping.idToBlock.put(id, block);
        }
    }

    /**
     * Read the built-in blacklist and default configuration for dimlets.
     */
    private static void readBuiltinConfig() {
        try {
            InputStream inputstream = RFTools.class.getResourceAsStream("/assets/rftools/text/dimlets.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if ("blacklist".equals(entry.getKey())) {
                    readBlacklist(entry.getValue());
                } else if ("dimlets".equals(entry.getKey())) {
                    readDimlets(entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readBlacklist(JsonElement element) {
        for (JsonElement entry : element.getAsJsonArray()) {
            String typeName = entry.getAsJsonArray().get(0).getAsString();
            String name = entry.getAsJsonArray().get(1).getAsString();
            DimletType type = DimletType.getTypeByName(typeName);
            if (type == null) {
                RFTools.logError("Error in dimlets.json! Unknown type '" + typeName + "'!");
                return;
            }
            dimletBlackList.add(new DimletKey(type, name));
        }
    }

    private static void readDimlets(JsonElement element) {
        for (JsonElement entry : element.getAsJsonArray()) {
            JsonArray array = entry.getAsJsonArray();
            String typeName = array.get(0).getAsString();
            String name = array.get(1).getAsString();
            Integer rfcreate = array.get(2).getAsInt();
            Integer rfmaintain = array.get(3).getAsInt();
            Integer tickCost = array.get(4).getAsInt();
            Integer rarity = array.get(5).getAsInt();
            Integer expensive = array.get(6).getAsInt();
            DimletType type = DimletType.getTypeByName(typeName);
            if (type == null) {
                RFTools.logError("Error in dimlets.json! Unknown type '" + typeName + "'!");
                return;
            }
            DimletKey key = new DimletKey(type, name);
            DimletCosts.dimletBuiltinRfCreate.put(key, rfcreate);
            DimletCosts.dimletBuiltinRfMaintain.put(key, rfmaintain);
            DimletCosts.dimletBuiltinTickCost.put(key, tickCost);
            DimletRandomizer.dimletBuiltinRarity.put(key, rarity);
            if (expensive != 0) {
                dimletIsExpensive.add(key);
            }
        }
    }

    private static void setupChestLoot() {
        setupChestLoot(ChestGenHooks.DUNGEON_CHEST);
        setupChestLoot(ChestGenHooks.MINESHAFT_CORRIDOR);
        setupChestLoot(ChestGenHooks.PYRAMID_DESERT_CHEST);
        setupChestLoot(ChestGenHooks.PYRAMID_JUNGLE_CHEST);
        setupChestLoot(ChestGenHooks.STRONGHOLD_CORRIDOR);
        setupChestLoot(ChestGenHooks.VILLAGE_BLACKSMITH);
    }

    private static void setupChestLoot(String category) {
        ChestGenHooks chest = ChestGenHooks.getInfo(category);
        chest.addItem(new WeightedRandomChestContent(ModItems.unknownDimlet, 0, 1, 3, 50));
    }

    private static void initBiomeItems(Configuration cfg, Map<DimletKey,Integer> idsInConfig) {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                String name = biome.biomeName;
                int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_BIOME, name));
                if (id != -1) {
                    DimletMapping.idToBiome.put(id, biome);
                    idToDisplayName.put(id, DimletType.DIMLET_BIOME.getName() + " " + name + " Dimlet");
                }
            }
        }
    }

    private static void initFoliageItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_FOLIAGE, "Oak"));
        if (id != -1) {
            idToDisplayName.put(id, "Foliage Oak Dimlet");
        }
    }

    private static void initLiquidItems(Configuration cfg, Map<DimletKey,Integer> idsInConfig) {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            if (me.getValue().canBePlacedInWorld()) {
                int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_LIQUID, me.getKey()));
                if (id != -1) {
                    String displayName = new FluidStack(me.getValue(), 1).getLocalizedName();
                    DimletMapping.idToFluid.put(id, me.getValue().getBlock());
                    idToDisplayName.put(id, DimletType.DIMLET_LIQUID.getName() + " " + displayName + " Dimlet");
                }
            }
        }
    }

    private static int initSpecialItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, SpecialType specialType) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_SPECIAL, name));
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_SPECIAL.getName() + " " + name + " Dimlet");
            DimletMapping.idToSpecialType.put(id, specialType);
        }
        return id;
    }

    private static int initMobItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, Class <? extends EntityLiving> entity, String name,
                                   int chance, int mingroup, int maxgroup, int maxentity) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_MOBS, name));
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_MOBS.getName() + " " + name + " Dimlet");
            chance = cfg.get(CATEGORY_MOBSPAWNS, name + ".chance", chance).getInt();
            mingroup = cfg.get(CATEGORY_MOBSPAWNS, name + ".mingroup", mingroup).getInt();
            maxgroup = cfg.get(CATEGORY_MOBSPAWNS, name + ".maxgroup", maxgroup).getInt();
            maxentity = cfg.get(CATEGORY_MOBSPAWNS, name + ".maxentity", maxentity).getInt();
            DimletMapping.idtoMob.put(id, new MobDescriptor(entity, chance, mingroup, maxgroup, maxentity));
        }
        return id;
    }

    private static int initSkyItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, SkyDescriptor skyDescriptor) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_SKY, name));
        if (id != -1) {
            DimletMapping.idToSkyDescriptor.put(id, skyDescriptor);
            idToDisplayName.put(id, DimletType.DIMLET_SKY.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initStructureItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, StructureType structureType) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_STRUCTURE, name));
        if (id != -1) {
            DimletMapping.idToStructureType.put(id, structureType);
            idToDisplayName.put(id, DimletType.DIMLET_STRUCTURE.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTerrainItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, TerrainType terrainType) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_TERRAIN, name));
        if (id != -1) {
            DimletMapping.idToTerrainType.put(id, terrainType);
            idToDisplayName.put(id, DimletType.DIMLET_TERRAIN.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initEffectItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, EffectType effectType) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_EFFECT, "" + name));
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_EFFECT.getName() + " " + name + " Dimlet");
            DimletMapping.idToEffectType.put(id, effectType);
        }
        return id;
    }

    private static int initFeatureItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, FeatureType featureType) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_FEATURE, name));
        if (id != -1) {
            DimletMapping.idToFeatureType.put(id, featureType);
            idToDisplayName.put(id, DimletType.DIMLET_FEATURE.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTimeItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, Float angle, Float speed) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_TIME, name));
        if (id != -1) {
            DimletMapping.idToCelestialAngle.put(id, angle);
            DimletMapping.idToSpeed.put(id, speed);
            idToDisplayName.put(id, DimletType.DIMLET_TIME.getName() + " " + name + " Dimlet");
        }
        return id;
    }

}

package com.mcjty.rftools.items.dimlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mcjty.rftools.CommonProxy;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.description.MobDescriptor;
import com.mcjty.rftools.dimension.description.SkyDescriptor;
import com.mcjty.rftools.dimension.world.types.SkyType;
import com.mcjty.rftools.dimension.world.types.*;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.varia.BlockMeta;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.io.*;
import java.util.*;

public class KnownDimletConfiguration {
    public static final String CATEGORY_KNOWNDIMLETS = "knowndimlets";              // This is part of dimlets.cfg
    public static final String CATEGORY_DIMLETSETTINGS = "dimletsettings";
    public static final String CATEGORY_RARITY = "rarity";
    public static final String CATEGORY_TYPERARIRTY = "typerarity";
    public static final String CATEGORY_TYPERFCREATECOST = "typerfcreatecost";
    public static final String CATEGORY_TYPERFMAINTAINCOST = "typerfmaintaincost";
    public static final String CATEGORY_TYPETICKCOST = "typetickcost";
    public static final String CATEGORY_MOBSPAWNS = "mobspawns";
    public static final String CATEGORY_GENERAL = "general";

    // This map keeps track of all known dimlets by id. Also the reverse map.
    public static final Map<Integer,DimletEntry> idToDimlet = new HashMap<Integer, DimletEntry>();
    public static final Map<DimletKey,Integer> dimletToID = new HashMap<DimletKey, Integer>();

    // Map the id of a dimlet to a display name.
    public static final Map<Integer,String> idToDisplayName = new HashMap<Integer, String>();

    // Map the id of a dimlet to extra information for the tooltip.
    public static final Map<Integer,List<String>> idToExtraInformation = new HashMap<Integer, List<String>>();

    // All craftable dimlets.
    public static final Set<Integer> craftableDimlets = new HashSet<Integer>();

    private static final Set<DimletKey> dimletBlackList = new HashSet<DimletKey>();
    private static final Set<DimletKey> dimletRandomNotAllowed = new HashSet<DimletKey>();

    private static int lastId = 0;

    public static void initGeneralConfig(Configuration cfg) {
        DimletCosts.baseDimensionCreationCost = cfg.get(CATEGORY_GENERAL, "baseDimensionCreationCost", DimletCosts.baseDimensionCreationCost,
                "The base cost (in RF/tick) for creating a dimension").getInt();
        DimletCosts.baseDimensionMaintenanceCost = cfg.get(CATEGORY_GENERAL, "baseDimensionMaintenanceCost", DimletCosts.baseDimensionMaintenanceCost,
                "The base cost (in RF/tick) for maintaining a dimension").getInt();
        DimletCosts.baseDimensionTickCost = cfg.get(CATEGORY_GENERAL, "baseDimensionTickCost", DimletCosts.baseDimensionTickCost,
                "The base time (in ticks) for creating a dimension").getInt();
    }


    private static <T> void remapIdsInMap(Map<Integer, Integer> mapFromTo, Map<Integer,T> baseMap) {
        Map<Integer,T> oldMap = new HashMap<Integer, T>(baseMap);
        baseMap.clear();
        for (Map.Entry<Integer, T> entry : oldMap.entrySet()) {
            Integer id = entry.getKey();
            T de = entry.getValue();
            if (mapFromTo.containsKey(id)) {
                baseMap.put(mapFromTo.get(id), de);
            } else {
                baseMap.put(id, de);
            }
        }
    }

    private static <T> void remapIdsInMapReversed(Map<Integer, Integer> mapFromTo, Map<T,Integer> baseMap) {
        Map<T,Integer> oldMap = new HashMap<T,Integer>(baseMap);
        baseMap.clear();
        for (Map.Entry<T, Integer> entry : oldMap.entrySet()) {
            Integer id = entry.getValue();
            T de = entry.getKey();
            if (mapFromTo.containsKey(id)) {
                baseMap.put(de, mapFromTo.get(id));
            } else {
                baseMap.put(de, id);
            }
        }
    }

    private static void remapIdsInSet(Map<Integer, Integer> mapFromTo, Set<Integer> baseSet) {
        Set<Integer> oldSet = new HashSet<Integer>(baseSet);
        baseSet.clear();
        for (Integer id : oldSet) {
            if (mapFromTo.containsKey(id)) {
                baseSet.add(mapFromTo.get(id));
            } else {
                baseSet.add(id);
            }
        }
    }

    private static void remapIdsInList(Map<Integer, Integer> mapFromTo, List<Integer> baseList) {
        List<Integer> oldList = new ArrayList<Integer>(baseList);
        baseList.clear();
        for (Integer id : oldList) {
            if (mapFromTo.containsKey(id)) {
                baseList.add(mapFromTo.get(id));
            } else {
                baseList.add(id);
            }
        }
    }

    /**
     * This is on the client side in case the server finds mismatching dimlet id's between
     * the server and the client. Here this will be corrected.
     */
    public static void remapIds(Map<Integer,Integer> mapFromTo) {
        remapIdsInMap(mapFromTo, idToDimlet);
        remapIdsInMapReversed(mapFromTo, dimletToID);
        remapIdsInMap(mapFromTo, idToDisplayName);
        remapIdsInSet(mapFromTo, craftableDimlets);
        remapIdsInMap(mapFromTo, idToExtraInformation);

        remapIdsInMap(mapFromTo, DimletMapping.idToTerrainType);
        remapIdsInMap(mapFromTo, DimletMapping.idToSpecialType);
        remapIdsInMap(mapFromTo, DimletMapping.idToFeatureType);
        remapIdsInMap(mapFromTo, DimletMapping.idToControllerType);
        remapIdsInMap(mapFromTo, DimletMapping.idToEffectType);
        remapIdsInMap(mapFromTo, DimletMapping.idToStructureType);
        remapIdsInMap(mapFromTo, DimletMapping.idToBiome);
        remapIdsInMap(mapFromTo, DimletMapping.idToDigit);
        remapIdsInMap(mapFromTo, DimletMapping.idToBlock);
        remapIdsInMap(mapFromTo, DimletMapping.idToFluid);
        remapIdsInMap(mapFromTo, DimletMapping.idToSkyDescriptor);
        remapIdsInMap(mapFromTo, DimletMapping.idtoMob);
        remapIdsInMap(mapFromTo, DimletMapping.idToCelestialAngle);
        remapIdsInMap(mapFromTo, DimletMapping.idToSpeed);
        remapIdsInSet(mapFromTo, DimletMapping.celestialBodies);

        remapIdsInList(mapFromTo, DimletRandomizer.dimletIds);
        DimletRandomizer.randomDimlets.remapValues(mapFromTo);
        DimletRandomizer.randomMaterialDimlets.remapValues(mapFromTo);
        DimletRandomizer.randomLiquidDimlets.remapValues(mapFromTo);
        DimletRandomizer.randomMobDimlets.remapValues(mapFromTo);
        DimletRandomizer.randomStructureDimlets.remapValues(mapFromTo);
        DimletRandomizer.randomEffectDimlets.remapValues(mapFromTo);
        DimletRandomizer.randomFeatureDimlets.remapValues(mapFromTo);
        
        initCrafting();
    }

    private static void registerDimletEntry(int id, DimletEntry dimletEntry) {
        idToDimlet.put(id, dimletEntry);
        dimletToID.put(dimletEntry.getKey(), id);
        DimletRandomizer.dimletIds.add(id);
    }

    private static int registerDimlet(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, DimletKey key) {
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

        int rfCreateCost = checkCostConfig(mainCfg, "rfcreate.", key, DimletCosts.dimletBuiltinRfCreate, DimletCosts.typeRfCreateCost);
        int rfMaintainCost = checkCostConfig(mainCfg, "rfmaintain.", key, DimletCosts.dimletBuiltinRfMaintain, DimletCosts.typeRfMaintainCost);
        int tickCost = checkCostConfig(mainCfg, "ticks.", key, DimletCosts.dimletBuiltinTickCost, DimletCosts.typeTickCost);
        int rarity = checkCostConfig(mainCfg, "rarity.", key, DimletRandomizer.dimletBuiltinRarity, DimletRandomizer.typeRarity);
        boolean randomNotAllowed = checkFlagConfig(mainCfg, "expensive.", key, dimletRandomNotAllowed);

        DimletEntry entry = new DimletEntry(key, rfCreateCost, rfMaintainCost, tickCost, rarity, randomNotAllowed);
        registerDimletEntry(id, entry);

        return id;
    }

    private static boolean checkFlagConfig(Configuration cfg, String prefix, DimletKey key, Set<DimletKey> builtinDefaults) {
        String k;
        k = prefix + key.getType().getName() + "." + key.getName();
        boolean defaultValue = builtinDefaults.contains(key);
        if (cfg.getCategory(CATEGORY_DIMLETSETTINGS).containsKey(k)) {
            return cfg.get(CATEGORY_DIMLETSETTINGS, k, defaultValue).getBoolean();
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
        if (defaultValue.equals(typeDefaults.get(key.getType())) && !cfg.getCategory(CATEGORY_DIMLETSETTINGS).containsKey(k)) {
            // Still using default. We don't want to force a config value so we first check to see
            // if it is there.
            cost = defaultValue;
        } else {
            cost = cfg.get(CATEGORY_DIMLETSETTINGS, k, defaultValue).getInt();
        }
        return cost;
    }

    private static void clean() {
        lastId = 0;

        idToDimlet.clear();
        dimletToID.clear();
        idToDisplayName.clear();
        craftableDimlets.clear();
        dimletBlackList.clear();
        dimletRandomNotAllowed.clear();

        DimletMapping.clean();
        DimletRandomizer.clean();
    }

    private static void addExtraInformation(int id, String... info) {
        List<String> extraInfo = new ArrayList<String>();
        Collections.addAll(extraInfo, info);
        idToExtraInformation.put(id, extraInfo);
    }

    public static boolean isInitialized() {
        return !idToDimlet.isEmpty();
    }

    /**
     * This initializes all dimlets based on all loaded mods. This should be called from postInit.
     */
    public static void init() {
        clean();

        File modConfigDir = CommonProxy.modConfigDir;
        Configuration mainCfg = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "main.cfg"));
        Configuration cfg = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "dimlets.cfg"));
        mainCfg.load();
        cfg.load();

        readBuiltinConfig();

        Map<DimletKey,Integer> idsInConfig = getDimletsFromConfig(cfg);

        int idControllerDefault = initControllerItem(cfg, mainCfg, idsInConfig, "Default", ControllerType.CONTROLLER_DEFAULT);
        int idControllerSingle = initControllerItem(cfg, mainCfg, idsInConfig, "Single", ControllerType.CONTROLLER_SINGLE);
        initControllerItem(cfg, mainCfg, idsInConfig, "Checkerboard", ControllerType.CONTROLLER_CHECKERBOARD);
        initControllerItem(cfg, mainCfg, idsInConfig, "Cold", ControllerType.CONTROLLER_COLD);
        initControllerItem(cfg, mainCfg, idsInConfig, "Medium", ControllerType.CONTROLLER_MEDIUM);
        initControllerItem(cfg, mainCfg, idsInConfig, "Warm", ControllerType.CONTROLLER_WARM);
        initControllerItem(cfg, mainCfg, idsInConfig, "Dry", ControllerType.CONTROLLER_DRY);
        initControllerItem(cfg, mainCfg, idsInConfig, "Wet", ControllerType.CONTROLLER_WET);
        initControllerItem(cfg, mainCfg, idsInConfig, "Fields", ControllerType.CONTROLLER_FIELDS);
        initControllerItem(cfg, mainCfg, idsInConfig, "Mountains", ControllerType.CONTROLLER_MOUNTAINS);
        initControllerItem(cfg, mainCfg, idsInConfig, "Filtered", ControllerType.CONTROLLER_FILTERED);
        initControllerItem(cfg, mainCfg, idsInConfig, "Magical", ControllerType.CONTROLLER_MAGICAL);
        initControllerItem(cfg, mainCfg, idsInConfig, "Forest", ControllerType.CONTROLLER_FOREST);
        BiomeControllerMapping.setupControllerBiomes();
        addExtraInformation(idControllerDefault, "The Default controller just uses the same", "biome distribution as the overworld");

        int idDigit0 = initDigitItem(cfg, mainCfg, idsInConfig, 0);
        int idDigit1 = initDigitItem(cfg, mainCfg, idsInConfig, 1);
        int idDigit2 = initDigitItem(cfg, mainCfg, idsInConfig, 2);
        int idDigit3 = initDigitItem(cfg, mainCfg, idsInConfig, 3);
        int idDigit4 = initDigitItem(cfg, mainCfg, idsInConfig, 4);
        int idDigit5 = initDigitItem(cfg, mainCfg, idsInConfig, 5);
        int idDigit6 = initDigitItem(cfg, mainCfg, idsInConfig, 6);
        int idDigit7 = initDigitItem(cfg, mainCfg, idsInConfig, 7);
        int idDigit8 = initDigitItem(cfg, mainCfg, idsInConfig, 8);
        int idDigit9 = initDigitItem(cfg, mainCfg, idsInConfig, 9);

        int idMaterialNone = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_MATERIAL, "None"));
        idToDisplayName.put(idMaterialNone, DimletType.DIMLET_MATERIAL.getName() + " None Dimlet");
        DimletMapping.idToBlock.put(idMaterialNone, null);
        addExtraInformation(idMaterialNone, "Use this material none dimlet to get normal", "biome specific stone generation");

        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.diamond_block, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.diamond_ore, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.emerald_block, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.emerald_ore, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.quartz_block, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.quartz_ore, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.gold_block, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.gold_ore, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.iron_block, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.iron_ore, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.coal_ore, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.lapis_block, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.lapis_ore, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.coal_block, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.redstone_block, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.redstone_ore, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.dirt, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.sandstone, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.end_stone, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.netherrack, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.cobblestone, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.obsidian, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.soul_sand, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.glass, 0);
        for (int i = 0 ; i < 16 ; i++) {
            initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.stained_glass, i);
            initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.stained_hardened_clay, i);
        }
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.glowstone, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.mossy_cobblestone, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.ice, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.packed_ice, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.clay, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.hardened_clay, 0);
        initMaterialItem(cfg, mainCfg, idsInConfig, ModBlocks.dimensionalShardBlock, 0);

        initModMaterialItem(cfg, mainCfg, idsInConfig, "chisel", "marble", 0);
        initModMaterialItem(cfg, mainCfg, idsInConfig, "chisel", "limestone", 0);

        initFoliageItem(cfg, mainCfg, idsInConfig);

        int idLiquidNone = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_LIQUID, "None"));
        DimletMapping.idToFluid.put(idLiquidNone, null);
        idToDisplayName.put(idLiquidNone, DimletType.DIMLET_LIQUID.getName() + " None Dimlet");
        addExtraInformation(idLiquidNone, "Use this liquid none dimlet to get normal", "water generation");

        int idPeaceful = initSpecialItem(cfg, mainCfg, idsInConfig, "Peaceful", SpecialType.SPECIAL_PEACEFUL);
        addExtraInformation(idPeaceful, "Normal mob spawning is disabled", "if you use this dimlet");
        int idEfficiency = initSpecialItem(cfg, mainCfg, idsInConfig, "Efficiency", SpecialType.SPECIAL_EFFICIENCY);
        addExtraInformation(idEfficiency, "Reduce the maintenance RF/tick of the", "generated dimension with 20%", "This is cumulative");

        int idDefaultMobs = initMobItem(cfg, mainCfg, idsInConfig, null, "Default", 1, 1, 1, 1);
        initMobItem(cfg, mainCfg, idsInConfig, EntityZombie.class, "Zombie", 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySkeleton.class, "Skeleton", 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntityEnderman.class, "Enderman", 20, 2, 4, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityBlaze.class, "Blaze", 20, 2, 4, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityCreeper.class, "Creeper", 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntityCaveSpider.class, "Cave Spider", 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntityGhast.class, "Ghast", 20, 2, 4, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityIronGolem.class, "Iron Golem", 20, 1, 2, 6);
        initMobItem(cfg, mainCfg, idsInConfig, EntityMagmaCube.class, "Magma Cube", 50, 2, 4, 30);
        initMobItem(cfg, mainCfg, idsInConfig, EntityPigZombie.class, "Zombie Pigman", 20, 2, 4, 10);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySlime.class, "Slime", 50, 2, 4, 30);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySnowman.class, "Snowman", 50, 2, 4, 30);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySpider.class, "Spider", 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntityWitch.class, "Witch", 10, 1, 1, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityBat.class, "Bat", 10, 8, 8, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityChicken.class, "Chicken", 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityCow.class, "Cow", 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityHorse.class, "Horse", 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityMooshroom.class, "Mooshroom", 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityOcelot.class, "Ocelot", 5, 2, 3, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityPig.class, "Pig", 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySheep.class, "Sheep", 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySquid.class, "Squid", 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityWolf.class, "Wolf", 10, 3, 4, 20);
        addExtraInformation(idDefaultMobs, "With this default dimlet you will just get", "the default mob spawning");

        int idSkyNormal = initSkyItem(cfg, mainCfg, idsInConfig, "Normal", new SkyDescriptor.Builder().skyType(SkyType.SKY_NORMAL).build(), false);
        int idNormalDay = initSkyItem(cfg, mainCfg, idsInConfig, "Normal Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.0f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Bright Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.5f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Dark Day", new SkyDescriptor.Builder().sunBrightnessFactor(0.4f).skyColorFactor(0.6f, 0.6f, 0.6f).build(), false);
        int idNormalNight = initSkyItem(cfg, mainCfg, idsInConfig, "Normal Night", new SkyDescriptor.Builder().starBrightnessFactor(1.0f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Bright Night", new SkyDescriptor.Builder().starBrightnessFactor(1.5f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Dark Night", new SkyDescriptor.Builder().starBrightnessFactor(0.4f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Red Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 0.2f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Green Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 0.2f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Blue Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 0.2f, 1.0f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Yellow Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 1.0f, 0.2f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Cyan Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 1.0f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Purple Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 1.0f).build(), false);

        initSkyItem(cfg, mainCfg, idsInConfig, "Normal Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 1.0f, 1.0f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Black Fog", new SkyDescriptor.Builder().fogColorFactor(0.0f, 0.0f, 0.0f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Red Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 0.2f, 0.2f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Green Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 1.0f, 0.2f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Blue Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 0.2f, 1.0f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Yellow Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 1.0f, 0.2f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Cyan Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 1.0f, 1.0f).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Purple Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 0.2f, 1.0f).build(), false);

        initSkyItem(cfg, mainCfg, idsInConfig, "Ender", new SkyDescriptor.Builder().skyType(SkyType.SKY_ENDER).build(), false);
        initSkyItem(cfg, mainCfg, idsInConfig, "Inferno", new SkyDescriptor.Builder().skyType(SkyType.SKY_INFERNO).build(), false);

        initSkyItem(cfg, mainCfg, idsInConfig, "Body None", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_NONE).build(), false);   // False because we don't want to select this randomly.
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SUN).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Large Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGESUN).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Small Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SMALLSUN).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Red Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_REDSUN).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_MOON).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Large Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGEMOON).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Small Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SMALLMOON).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Red Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_REDMOON).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Planet", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_PLANET).build(), true);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Large Planet", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGEPLANET).build(), true);

        addExtraInformation(idSkyNormal, "A normal type of sky", "(as opposed to ender or inferno)");
        addExtraInformation(idNormalDay, "Normal brightness level for daytime sky");
        addExtraInformation(idNormalNight, "Normal brightness level for nighttime sky");

        int idStructureNone = initStructureItem(cfg, mainCfg, idsInConfig, "None", StructureType.STRUCTURE_NONE);
        initStructureItem(cfg, mainCfg, idsInConfig, "Village", StructureType.STRUCTURE_VILLAGE);
        initStructureItem(cfg, mainCfg, idsInConfig, "Stronghold", StructureType.STRUCTURE_STRONGHOLD);
        initStructureItem(cfg, mainCfg, idsInConfig, "Dungeon", StructureType.STRUCTURE_DUNGEON);
        initStructureItem(cfg, mainCfg, idsInConfig, "Fortress", StructureType.STRUCTURE_FORTRESS);
        initStructureItem(cfg, mainCfg, idsInConfig, "Mineshaft", StructureType.STRUCTURE_MINESHAFT);
        initStructureItem(cfg, mainCfg, idsInConfig, "Scattered", StructureType.STRUCTURE_SCATTERED);
        addExtraInformation(idStructureNone, "With this none dimlet you can disable", "all normal structure spawning");

        int idTerrainVoid = initTerrainItem(cfg, mainCfg, idsInConfig, "Void", TerrainType.TERRAIN_VOID);
        int idTerrainFlat = initTerrainItem(cfg, mainCfg, idsInConfig, "Flat", TerrainType.TERRAIN_FLAT);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Amplified", TerrainType.TERRAIN_AMPLIFIED);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Normal", TerrainType.TERRAIN_NORMAL);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Cavern", TerrainType.TERRAIN_CAVERN);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Island", TerrainType.TERRAIN_ISLAND);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Islands", TerrainType.TERRAIN_ISLANDS);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Chaotic", TerrainType.TERRAIN_CHAOTIC);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Plateaus", TerrainType.TERRAIN_PLATEAUS);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Grid", TerrainType.TERRAIN_GRID);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Low Cavern", TerrainType.TERRAIN_LOW_CAVERN);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Flooded Cavern", TerrainType.TERRAIN_FLOODED_CAVERN);

        int idFeatureNone = initFeatureItem(cfg, mainCfg, idsInConfig, "None", FeatureType.FEATURE_NONE);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Caves", FeatureType.FEATURE_CAVES);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Ravines", FeatureType.FEATURE_RAVINES);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Orbs", FeatureType.FEATURE_ORBS);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Oregen", FeatureType.FEATURE_OREGEN);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Lakes", FeatureType.FEATURE_LAKES);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Tendrils", FeatureType.FEATURE_TENDRILS);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Canyons", FeatureType.FEATURE_CANYONS);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Maze", FeatureType.FEATURE_MAZE);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Liquid Orbs", FeatureType.FEATURE_LIQUIDORBS);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Shallow Ocean", FeatureType.FEATURE_SHALLOW_OCEAN);
        addExtraInformation(idFeatureNone, "With this none dimlet you can disable", "all special features");

        int idEffectNone = initEffectItem(cfg, mainCfg, idsInConfig, "None", EffectType.EFFECT_NONE);
        initEffectItem(cfg, mainCfg, idsInConfig, "Poison", EffectType.EFFECT_POISON);
        initEffectItem(cfg, mainCfg, idsInConfig, "Poison II", EffectType.EFFECT_POISON2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Poison III", EffectType.EFFECT_POISON3);
//        initEffectItem(cfg, mainCfg, idsInConfig, "No Gravity", EffectType.EFFECT_NOGRAVITY);
        initEffectItem(cfg, mainCfg, idsInConfig, "Regeneration", EffectType.EFFECT_REGENERATION);
        initEffectItem(cfg, mainCfg, idsInConfig, "Regeneration II", EffectType.EFFECT_REGENERATION2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Regeneration III", EffectType.EFFECT_REGENERATION3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Slowness", EffectType.EFFECT_MOVESLOWDOWN);
        initEffectItem(cfg, mainCfg, idsInConfig, "Slowness II", EffectType.EFFECT_MOVESLOWDOWN2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Slowness III", EffectType.EFFECT_MOVESLOWDOWN3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Slowness IV", EffectType.EFFECT_MOVESLOWDOWN4);
        initEffectItem(cfg, mainCfg, idsInConfig, "Speed", EffectType.EFFECT_MOVESPEED);
        initEffectItem(cfg, mainCfg, idsInConfig, "Speed II", EffectType.EFFECT_MOVESPEED2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Speed III", EffectType.EFFECT_MOVESPEED3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Mining Fatigue", EffectType.EFFECT_DIGSLOWDOWN);
        initEffectItem(cfg, mainCfg, idsInConfig, "Mining Fatigue II", EffectType.EFFECT_DIGSLOWDOWN2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Mining Fatigue III", EffectType.EFFECT_DIGSLOWDOWN3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Mining Fatigue IV", EffectType.EFFECT_DIGSLOWDOWN4);
        initEffectItem(cfg, mainCfg, idsInConfig, "Haste", EffectType.EFFECT_DIGSPEED);
        initEffectItem(cfg, mainCfg, idsInConfig, "Haste II", EffectType.EFFECT_DIGSPEED2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Haste III", EffectType.EFFECT_DIGSPEED3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Damage Boost", EffectType.EFFECT_DAMAGEBOOST);
        initEffectItem(cfg, mainCfg, idsInConfig, "Damage Boost II", EffectType.EFFECT_DAMAGEBOOST2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Damage Boost III", EffectType.EFFECT_DAMAGEBOOST3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Instant Health", EffectType.EFFECT_INSTANTHEALTH);
        initEffectItem(cfg, mainCfg, idsInConfig, "Harm", EffectType.EFFECT_HARM);
        initEffectItem(cfg, mainCfg, idsInConfig, "Jump", EffectType.EFFECT_JUMP);
        initEffectItem(cfg, mainCfg, idsInConfig, "Jump II", EffectType.EFFECT_JUMP2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Jump III", EffectType.EFFECT_JUMP3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Confusion", EffectType.EFFECT_CONFUSION);
        initEffectItem(cfg, mainCfg, idsInConfig, "Resistance", EffectType.EFFECT_RESISTANCE);
        initEffectItem(cfg, mainCfg, idsInConfig, "Resistance II", EffectType.EFFECT_RESISTANCE2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Resistance III", EffectType.EFFECT_RESISTANCE3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Fire Resistance", EffectType.EFFECT_FIRERESISTANCE);
        initEffectItem(cfg, mainCfg, idsInConfig, "Water Breathing", EffectType.EFFECT_WATERBREATHING);
        initEffectItem(cfg, mainCfg, idsInConfig, "Invisibility", EffectType.EFFECT_INVISIBILITY);
        initEffectItem(cfg, mainCfg, idsInConfig, "Blindness", EffectType.EFFECT_BLINDNESS);
        initEffectItem(cfg, mainCfg, idsInConfig, "Nightvision", EffectType.EFFECT_NIGHTVISION);
        initEffectItem(cfg, mainCfg, idsInConfig, "Hunger", EffectType.EFFECT_HUNGER);
        initEffectItem(cfg, mainCfg, idsInConfig, "Hunger II", EffectType.EFFECT_HUNGER2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Hunger III", EffectType.EFFECT_HUNGER3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Weakness", EffectType.EFFECT_WEAKNESS);
        initEffectItem(cfg, mainCfg, idsInConfig, "Weakness II", EffectType.EFFECT_WEAKNESS2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Weakness III", EffectType.EFFECT_WEAKNESS3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Wither", EffectType.EFFECT_WITHER);
        initEffectItem(cfg, mainCfg, idsInConfig, "Wither II", EffectType.EFFECT_WITHER2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Wither III", EffectType.EFFECT_WITHER3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Health Boost", EffectType.EFFECT_HEALTHBOOST);
        initEffectItem(cfg, mainCfg, idsInConfig, "Health Boost II", EffectType.EFFECT_HEALTHBOOST2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Health Boost III", EffectType.EFFECT_HEALTHBOOST3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Absorption", EffectType.EFFECT_ABSORPTION);
        initEffectItem(cfg, mainCfg, idsInConfig, "Absorption II", EffectType.EFFECT_ABSORPTION2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Absorption III", EffectType.EFFECT_ABSORPTION3);
        initEffectItem(cfg, mainCfg, idsInConfig, "Saturation", EffectType.EFFECT_SATURATION);
        initEffectItem(cfg, mainCfg, idsInConfig, "Saturation II", EffectType.EFFECT_SATURATION2);
        initEffectItem(cfg, mainCfg, idsInConfig, "Saturation III", EffectType.EFFECT_SATURATION3);
        addExtraInformation(idEffectNone, "With this none dimlet you can disable", "all special effects");

        int idNormalTime = initTimeItem(cfg, mainCfg, idsInConfig, "Normal", null, null);
        initTimeItem(cfg, mainCfg, idsInConfig, "Noon", 0.0f, null);
        initTimeItem(cfg, mainCfg, idsInConfig, "Midnight", 0.5f, null);
        initTimeItem(cfg, mainCfg, idsInConfig, "Morning", 0.2f, null);
        initTimeItem(cfg, mainCfg, idsInConfig, "Evening", 0.75f, null);
        initTimeItem(cfg, mainCfg, idsInConfig, "Fast", null, 2.0f);
        initTimeItem(cfg, mainCfg, idsInConfig, "Slow", null, 0.5f);
        addExtraInformation(idNormalTime, "With this normal dimlet you will get", "default day/night timing");

        initBiomeItems(cfg, mainCfg, idsInConfig);
        initLiquidItems(cfg, mainCfg, idsInConfig);

        craftableDimlets.add(idEffectNone);
        craftableDimlets.add(idFeatureNone);
        craftableDimlets.add(idStructureNone);
        craftableDimlets.add(idTerrainVoid);
        craftableDimlets.add(idTerrainFlat);
        craftableDimlets.add(idControllerDefault);
        craftableDimlets.add(idControllerSingle);
        craftableDimlets.add(idMaterialNone);
        craftableDimlets.add(idLiquidNone);
        craftableDimlets.add(idSkyNormal);
        craftableDimlets.add(idNormalDay);
        craftableDimlets.add(idNormalNight);
        craftableDimlets.add(idDefaultMobs);
        craftableDimlets.add(idNormalTime);
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

        readUserDimlets(cfg, mainCfg, idsInConfig, modConfigDir);

        DimletRandomizer.setupWeightedRandomList(mainCfg);
        setupChestLoot();

        if (mainCfg.hasChanged()) {
            mainCfg.save();
        }
        if (cfg.hasChanged()) {
            cfg.save();
        }
    }

    private static void initDigitCrafting(String from, String to) {
        int idFrom = dimletToID.get(new DimletKey(DimletType.DIMLET_DIGIT, from));
        int idTo = dimletToID.get(new DimletKey(DimletType.DIMLET_DIGIT, to));
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idTo), "   ", " 9 ", "   ", '9', new ItemStack(ModItems.knownDimlet, 1, idFrom));
    }

    public static void initCrafting() {
        List recipeList = CraftingManager.getInstance().getRecipeList();
        int i = 0;
        while (i < recipeList.size()) {
            if (recipeList.get(i) instanceof ShapedRecipes) {
                ShapedRecipes r = (ShapedRecipes) recipeList.get(i);
                if (r.getRecipeOutput().getItem() == ModItems.knownDimlet && r.recipeItems[4].getItem() == ModItems.knownDimlet) {
                    recipeList.remove(i);
                    i--;
                }
            }
            i++;
        }

        initDigitCrafting("0", "1");
        initDigitCrafting("1", "2");
        initDigitCrafting("2", "3");
        initDigitCrafting("3", "4");
        initDigitCrafting("4", "5");
        initDigitCrafting("5", "6");
        initDigitCrafting("6", "7");
        initDigitCrafting("7", "8");
        initDigitCrafting("8", "9");
        initDigitCrafting("9", "0");
    }

    private static int initModMaterialItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String modid, String blockname, int meta) {
        Block block = GameRegistry.findBlock(modid, blockname);
        if (block != null) {
            return initMaterialItem(cfg, mainCfg, idsInConfig, block, meta);
        } else {
            return -1;
        }
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

    private static int initDigitItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, int digit) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_DIGIT, "" + digit));
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_DIGIT.getName() + " " + digit + " Dimlet");
            DimletMapping.idToDigit.put(id, String.valueOf(digit));
        }
        return id;
    }

    private static int initMaterialItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, Block block, int meta) {
        String unlocalizedName = block.getUnlocalizedName();
        if (meta != 0) {
            unlocalizedName += meta;
        }
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_MATERIAL, unlocalizedName));
        if (id != -1) {
            ItemStack stack = new ItemStack(block, 1, meta);
            idToDisplayName.put(id, DimletType.DIMLET_MATERIAL.getName() + " " + stack.getDisplayName() + " Dimlet");
            DimletMapping.idToBlock.put(id, new BlockMeta(block, (byte)meta));
        }
        return id;
    }

    /**
     * Read user-specified dimlets.
     */
    private static void readUserDimlets(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, File modConfigDir) {
        try {
            File file = new File(modConfigDir.getPath() + File.separator + "rftools", "userdimlets.json");
            FileInputStream inputstream = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if ("material".equals(entry.getKey())) {
                    JsonElement value = entry.getValue();
                    JsonArray array = value.getAsJsonArray();
                    String modid = array.get(0).getAsString();
                    String name = array.get(1).getAsString();
                    Integer meta = array.get(2).getAsInt();
                    Integer rfcreate = array.get(3).getAsInt();
                    Integer rfmaintain = array.get(4).getAsInt();
                    Integer tickCost = array.get(5).getAsInt();
                    Integer rarity = array.get(6).getAsInt();
                    Integer expensive = array.get(7).getAsInt();
                    int id = initModMaterialItem(cfg, mainCfg, idsInConfig, modid, name, meta);
                    if (id != -1) {
                        DimletKey key = idToDimlet.get(id).getKey();
                        DimletCosts.dimletBuiltinRfCreate.put(key, rfcreate);
                        DimletCosts.dimletBuiltinRfMaintain.put(key, rfmaintain);
                        DimletCosts.dimletBuiltinTickCost.put(key, tickCost);
                        DimletRandomizer.dimletBuiltinRarity.put(key, rarity);
                        if (expensive != 0) {
                            dimletRandomNotAllowed.add(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            RFTools.log("Could not read 'userdimlets.json', this is not an error!");
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
                dimletRandomNotAllowed.add(key);
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

    private static int initControllerItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, String name, ControllerType type) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_CONTROLLER, name));
        if (id != -1) {
            DimletMapping.idToControllerType.put(id, type);
            idToDisplayName.put(id, DimletType.DIMLET_CONTROLLER.getName() + " " + name + " Dimlet");
        }
        return -1;
    }

    private static void initBiomeItems(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig) {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                String name = biome.biomeName;
                int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_BIOME, name));
                if (id != -1) {
                    DimletMapping.idToBiome.put(id, biome);
                    idToDisplayName.put(id, DimletType.DIMLET_BIOME.getName() + " " + name + " Dimlet");
                }
            }
        }
    }

    private static void initFoliageItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_FOLIAGE, "Oak"));
        if (id != -1) {
            idToDisplayName.put(id, "Foliage Oak Dimlet");
        }
    }

    private static void initLiquidItems(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig) {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            if (me.getValue().canBePlacedInWorld()) {
                int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_LIQUID, me.getKey()));
                if (id != -1) {
                    String displayName = new FluidStack(me.getValue(), 1).getLocalizedName();
                    DimletMapping.idToFluid.put(id, me.getValue().getBlock());
                    idToDisplayName.put(id, DimletType.DIMLET_LIQUID.getName() + " " + displayName + " Dimlet");
                }
            }
        }
    }

    private static int initSpecialItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, String name, SpecialType specialType) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_SPECIAL, name));
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_SPECIAL.getName() + " " + name + " Dimlet");
            DimletMapping.idToSpecialType.put(id, specialType);
        }
        return id;
    }

    private static int initMobItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, Class <? extends EntityLiving> entity, String name,
                                   int chance, int mingroup, int maxgroup, int maxentity) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_MOBS, name));
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_MOBS.getName() + " " + name + " Dimlet");
            chance = mainCfg.get(CATEGORY_MOBSPAWNS, name + ".chance", chance).getInt();
            mingroup = mainCfg.get(CATEGORY_MOBSPAWNS, name + ".mingroup", mingroup).getInt();
            maxgroup = mainCfg.get(CATEGORY_MOBSPAWNS, name + ".maxgroup", maxgroup).getInt();
            maxentity = mainCfg.get(CATEGORY_MOBSPAWNS, name + ".maxentity", maxentity).getInt();
            DimletMapping.idtoMob.put(id, new MobDescriptor(entity, chance, mingroup, maxgroup, maxentity));
        }
        return id;
    }

    private static int initSkyItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, SkyDescriptor skyDescriptor, boolean isbody) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_SKY, name));
        if (id != -1) {
            DimletMapping.idToSkyDescriptor.put(id, skyDescriptor);
            idToDisplayName.put(id, DimletType.DIMLET_SKY.getName() + " " + name + " Dimlet");
            if (isbody) {
                DimletMapping.celestialBodies.add(id);
            }
        }
        return id;
    }

    private static int initStructureItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, String name, StructureType structureType) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_STRUCTURE, name));
        if (id != -1) {
            DimletMapping.idToStructureType.put(id, structureType);
            idToDisplayName.put(id, DimletType.DIMLET_STRUCTURE.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTerrainItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, String name, TerrainType terrainType) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_TERRAIN, name));
        if (id != -1) {
            DimletMapping.idToTerrainType.put(id, terrainType);
            idToDisplayName.put(id, DimletType.DIMLET_TERRAIN.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initEffectItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, String name, EffectType effectType) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_EFFECT, "" + name));
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_EFFECT.getName() + " " + name + " Dimlet");
            DimletMapping.idToEffectType.put(id, effectType);
        }
        return id;
    }

    private static int initFeatureItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, String name, FeatureType featureType) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_FEATURE, name));
        if (id != -1) {
            DimletMapping.idToFeatureType.put(id, featureType);
            idToDisplayName.put(id, DimletType.DIMLET_FEATURE.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTimeItem(Configuration cfg, Configuration mainCfg, Map<DimletKey,Integer> idsInConfig, String name, Float angle, Float speed) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_TIME, name));
        if (id != -1) {
            DimletMapping.idToCelestialAngle.put(id, angle);
            DimletMapping.idToSpeed.put(id, speed);
            idToDisplayName.put(id, DimletType.DIMLET_TIME.getName() + " " + name + " Dimlet");
        }
        return id;
    }

}

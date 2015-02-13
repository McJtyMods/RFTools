package com.mcjty.rftools.items.dimlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mcjty.rftools.CommonProxy;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.crafting.KnownDimletShapedRecipe;
import com.mcjty.rftools.dimension.description.MobDescriptor;
import com.mcjty.rftools.dimension.description.SkyDescriptor;
import com.mcjty.rftools.dimension.world.types.*;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.varia.BlockMeta;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
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


    /**
     * This is on the client side in case the server finds mismatching dimlet id's between
     * the server and the client. Here this will be corrected.
     */
    public static void remapIds(Map<Integer,Integer> mapFromTo) {
        if (!isInitialized()) {
            return; // Nothing to do.
        }
        DimletMapping mapping = DimletMapping.getDimletMapping(Minecraft.getMinecraft().theWorld);

        mapping.remapIds(mapFromTo);
        DimletMapping.remapIdsInMap(mapFromTo, idToDisplayName);
        DimletMapping.remapIdsInSet(mapFromTo, craftableDimlets);
        DimletMapping.remapIdsInMap(mapFromTo, idToExtraInformation);

        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToTerrainType);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToSpecialType);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToFeatureType);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToControllerType);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToEffectType);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToStructureType);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToBiome);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToDigit);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToBlock);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToFluid);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToSkyDescriptor);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idtoMob);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToCelestialAngle);
        DimletMapping.remapIdsInMap(mapFromTo, DimletObjectMapping.idToSpeed);
        DimletMapping.remapIdsInSet(mapFromTo, DimletObjectMapping.celestialBodies);

        DimletMapping.remapIdsInList(mapFromTo, DimletRandomizer.dimletIds);
        if (DimletRandomizer.randomDimlets != null) {
            DimletRandomizer.randomDimlets.remapValues(mapFromTo);
        }
        if (DimletRandomizer.randomMaterialDimlets != null) {
            DimletRandomizer.randomMaterialDimlets.remapValues(mapFromTo);
        }
        if (DimletRandomizer.randomLiquidDimlets != null) {
            DimletRandomizer.randomLiquidDimlets.remapValues(mapFromTo);
        }
        if (DimletRandomizer.randomMobDimlets != null) {
            DimletRandomizer.randomMobDimlets.remapValues(mapFromTo);
        }
        if (DimletRandomizer.randomStructureDimlets != null) {
            DimletRandomizer.randomStructureDimlets.remapValues(mapFromTo);
        }
        if (DimletRandomizer.randomEffectDimlets != null) {
            DimletRandomizer.randomEffectDimlets.remapValues(mapFromTo);
        }
        if (DimletRandomizer.randomFeatureDimlets != null) {
            DimletRandomizer.randomFeatureDimlets.remapValues(mapFromTo);
        }
        
        initCrafting(Minecraft.getMinecraft().theWorld);
    }

    private static void registerDimletEntry(int id, DimletEntry dimletEntry, DimletMapping mapping) {
        mapping.registerDimletEntry(id, dimletEntry);
        DimletRandomizer.dimletIds.add(id);
    }

    private static int registerDimlet(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, DimletKey key, DimletMapping mapping) {
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

        if (rfMaintainCost > 0) {
            float factor = DimletConfiguration.maintenanceCostPercentage / 100.0f;
            if (factor < -0.9f) {
                factor = -0.9f;
            }
            rfMaintainCost += rfMaintainCost * factor;
        }

        DimletEntry entry = new DimletEntry(key, rfCreateCost, rfMaintainCost, tickCost, rarity, randomNotAllowed);
        registerDimletEntry(id, entry, mapping);

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

    public static void clean() {
        lastId = 0;

        if (DimletMapping.getInstance() != null) {
            DimletMapping.getInstance().clear();
        }
        idToDisplayName.clear();
        craftableDimlets.clear();
        dimletBlackList.clear();
        dimletRandomNotAllowed.clear();

        DimletObjectMapping.clean();
        DimletRandomizer.clean();
    }

    private static void addExtraInformation(int id, String... info) {
        List<String> extraInfo = new ArrayList<String>();
        Collections.addAll(extraInfo, info);
        idToExtraInformation.put(id, extraInfo);
    }

    public static boolean isInitialized() {
        return DimletMapping.isInitialized();
    }

    public static void initServer(World world) {
        init(world);
    }

    public static void initClient(World world) {
        init(world);
    }

    /**
     * This initializes all dimlets based on all loaded mods.
     */
    public static void init(World world) {
        clean();

        DimletMapping mapping = DimletMapping.getDimletMapping(world);

        File modConfigDir = CommonProxy.modConfigDir;
        Configuration mainCfg = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "main.cfg"));
        Configuration cfg = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "dimlets.cfg"));
        mainCfg.load();
        cfg.load();

        readBuiltinConfig();

        Map<DimletKey,Integer> idsInConfig = getDimletsFromConfig(cfg);

        int idControllerDefault = initControllerItem(cfg, mainCfg, idsInConfig, "Default", ControllerType.CONTROLLER_DEFAULT, mapping);
        int idControllerSingle = initControllerItem(cfg, mainCfg, idsInConfig, "Single", ControllerType.CONTROLLER_SINGLE, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Checkerboard", ControllerType.CONTROLLER_CHECKERBOARD, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Cold", ControllerType.CONTROLLER_COLD, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Medium", ControllerType.CONTROLLER_MEDIUM, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Warm", ControllerType.CONTROLLER_WARM, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Dry", ControllerType.CONTROLLER_DRY, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Wet", ControllerType.CONTROLLER_WET, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Fields", ControllerType.CONTROLLER_FIELDS, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Mountains", ControllerType.CONTROLLER_MOUNTAINS, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Filtered", ControllerType.CONTROLLER_FILTERED, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Magical", ControllerType.CONTROLLER_MAGICAL, mapping);
        initControllerItem(cfg, mainCfg, idsInConfig, "Forest", ControllerType.CONTROLLER_FOREST, mapping);
        BiomeControllerMapping.setupControllerBiomes();
        addExtraInformation(idControllerDefault, "The Default controller just uses the same", "biome distribution as the overworld");

        int idDigit0 = initDigitItem(cfg, mainCfg, idsInConfig, 0, mapping);
        int idDigit1 = initDigitItem(cfg, mainCfg, idsInConfig, 1, mapping);
        int idDigit2 = initDigitItem(cfg, mainCfg, idsInConfig, 2, mapping);
        int idDigit3 = initDigitItem(cfg, mainCfg, idsInConfig, 3, mapping);
        int idDigit4 = initDigitItem(cfg, mainCfg, idsInConfig, 4, mapping);
        int idDigit5 = initDigitItem(cfg, mainCfg, idsInConfig, 5, mapping);
        int idDigit6 = initDigitItem(cfg, mainCfg, idsInConfig, 6, mapping);
        int idDigit7 = initDigitItem(cfg, mainCfg, idsInConfig, 7, mapping);
        int idDigit8 = initDigitItem(cfg, mainCfg, idsInConfig, 8, mapping);
        int idDigit9 = initDigitItem(cfg, mainCfg, idsInConfig, 9, mapping);

        int idMaterialNone = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_MATERIAL, "None"), mapping);
        idToDisplayName.put(idMaterialNone, DimletType.DIMLET_MATERIAL.getName() + " None Dimlet");
        DimletObjectMapping.idToBlock.put(idMaterialNone, null);
        addExtraInformation(idMaterialNone, "Use this material none dimlet to get normal", "biome specific stone generation");

        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.diamond_block, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.diamond_ore, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.emerald_block, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.emerald_ore, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.quartz_block, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.quartz_ore, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.gold_block, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.gold_ore, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.iron_block, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.iron_ore, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.coal_ore, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.lapis_block, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.lapis_ore, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.coal_block, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.redstone_block, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.redstone_ore, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.dirt, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.sandstone, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.end_stone, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.netherrack, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.cobblestone, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.obsidian, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.soul_sand, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.glass, 0, mapping);
        for (int i = 0 ; i < 16 ; i++) {
            initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.stained_glass, i, mapping);
            initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.stained_hardened_clay, i, mapping);
        }
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.glowstone, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.mossy_cobblestone, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.ice, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.packed_ice, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.clay, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, Blocks.hardened_clay, 0, mapping);
        initMaterialItem(cfg, mainCfg, idsInConfig, ModBlocks.dimensionalShardBlock, 0, mapping);

        initModMaterialItem(cfg, mainCfg, idsInConfig, "chisel", "marble", 0, mapping);
        initModMaterialItem(cfg, mainCfg, idsInConfig, "chisel", "limestone", 0, mapping);

        initFoliageItem(cfg, mainCfg, idsInConfig, mapping);

        int idLiquidNone = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_LIQUID, "None"), mapping);
        DimletObjectMapping.idToFluid.put(idLiquidNone, null);
        idToDisplayName.put(idLiquidNone, DimletType.DIMLET_LIQUID.getName() + " None Dimlet");
        addExtraInformation(idLiquidNone, "Use this liquid none dimlet to get normal", "water generation");

        int idPeaceful = initSpecialItem(cfg, mainCfg, idsInConfig, "Peaceful", SpecialType.SPECIAL_PEACEFUL, mapping);
        addExtraInformation(idPeaceful, "Normal mob spawning is disabled", "if you use this dimlet");
        int idEfficiency = initSpecialItem(cfg, mainCfg, idsInConfig, "Efficiency", SpecialType.SPECIAL_EFFICIENCY, mapping);
        addExtraInformation(idEfficiency, "Reduce the maintenance RF/tick of the", "generated dimension with 20%", "This is cumulative");
        int idEfficiencyLow = initSpecialItem(cfg, mainCfg, idsInConfig, "Mediocre Efficiency", SpecialType.SPECIAL_EFFICIENCY_LOW, mapping);
        addExtraInformation(idEfficiencyLow, "Reduce the maintenance RF/tick of the", "generated dimension with 5%", "This is cumulative");
        int idShelter = initSpecialItem(cfg, mainCfg, idsInConfig, "Shelter", SpecialType.SPECIAL_SHELTER, mapping);
        addExtraInformation(idShelter, "Generate a better sheltered spawn", "platform in the dimension");

        int idDefaultMobs = initMobItem(cfg, mainCfg, idsInConfig, null, "Default", mapping, 1, 1, 1, 1);
        initMobItem(cfg, mainCfg, idsInConfig, EntityZombie.class, "Zombie", mapping, 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySkeleton.class, "Skeleton", mapping, 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntityEnderman.class, "Enderman", mapping, 20, 2, 4, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityBlaze.class, "Blaze", mapping, 20, 2, 4, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityCreeper.class, "Creeper", mapping, 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntityCaveSpider.class, "Cave Spider", mapping, 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntityGhast.class, "Ghast", mapping, 20, 2, 4, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityIronGolem.class, "Iron Golem", mapping, 20, 1, 2, 6);
        initMobItem(cfg, mainCfg, idsInConfig, EntityMagmaCube.class, "Magma Cube", mapping, 50, 2, 4, 30);
        initMobItem(cfg, mainCfg, idsInConfig, EntityPigZombie.class, "Zombie Pigman", mapping, 20, 2, 4, 10);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySlime.class, "Slime", mapping, 50, 2, 4, 30);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySnowman.class, "Snowman", mapping, 50, 2, 4, 30);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySpider.class, "Spider", mapping, 100, 8, 8, 60);
        initMobItem(cfg, mainCfg, idsInConfig, EntityWitch.class, "Witch", mapping, 10, 1, 1, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityBat.class, "Bat", mapping, 10, 8, 8, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityChicken.class, "Chicken", mapping, 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityCow.class, "Cow", mapping, 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityHorse.class, "Horse", mapping, 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityMooshroom.class, "Mooshroom", mapping, 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityOcelot.class, "Ocelot", mapping, 5, 2, 3, 20);
        initMobItem(cfg, mainCfg, idsInConfig, EntityPig.class, "Pig", mapping, 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySheep.class, "Sheep", mapping, 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntitySquid.class, "Squid", mapping, 10, 3, 4, 40);
        initMobItem(cfg, mainCfg, idsInConfig, EntityWolf.class, "Wolf", mapping, 10, 3, 4, 20);
        addExtraInformation(idDefaultMobs, "With this default dimlet you will just get", "the default mob spawning");

        int idSkyNormal = initSkyItem(cfg, mainCfg, idsInConfig, "Normal", new SkyDescriptor.Builder().skyType(SkyType.SKY_NORMAL).build(), false, mapping);
        int idNormalDay = initSkyItem(cfg, mainCfg, idsInConfig, "Normal Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.0f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Bright Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.5f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Dark Day", new SkyDescriptor.Builder().sunBrightnessFactor(0.4f).skyColorFactor(0.6f, 0.6f, 0.6f).build(), false, mapping);
        int idNormalNight = initSkyItem(cfg, mainCfg, idsInConfig, "Normal Night", new SkyDescriptor.Builder().starBrightnessFactor(1.0f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Bright Night", new SkyDescriptor.Builder().starBrightnessFactor(1.5f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Dark Night", new SkyDescriptor.Builder().starBrightnessFactor(0.4f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Red Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Green Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Blue Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 0.2f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Yellow Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 1.0f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Cyan Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Purple Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 1.0f).build(), false, mapping);

        initSkyItem(cfg, mainCfg, idsInConfig, "Normal Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 1.0f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Black Fog", new SkyDescriptor.Builder().fogColorFactor(0.0f, 0.0f, 0.0f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Red Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 0.2f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Green Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 1.0f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Blue Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 0.2f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Yellow Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 1.0f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Cyan Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 1.0f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Purple Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 0.2f, 1.0f).build(), false, mapping);

        initSkyItem(cfg, mainCfg, idsInConfig, "Ender", new SkyDescriptor.Builder().skyType(SkyType.SKY_ENDER).build(), false, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Inferno", new SkyDescriptor.Builder().skyType(SkyType.SKY_INFERNO).build(), false, mapping);

        initSkyItem(cfg, mainCfg, idsInConfig, "Body None", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_NONE).build(), false, mapping);   // False because we don't want to select this randomly.
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SUN).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Large Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGESUN).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Small Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SMALLSUN).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Red Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_REDSUN).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_MOON).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Large Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGEMOON).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Small Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SMALLMOON).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Red Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_REDMOON).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Planet", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_PLANET).build(), true, mapping);
        initSkyItem(cfg, mainCfg, idsInConfig, "Body Large Planet", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGEPLANET).build(), true, mapping);

        addExtraInformation(idSkyNormal, "A normal type of sky", "(as opposed to ender or inferno)");
        addExtraInformation(idNormalDay, "Normal brightness level for daytime sky");
        addExtraInformation(idNormalNight, "Normal brightness level for nighttime sky");

        int idStructureNone = initStructureItem(cfg, mainCfg, idsInConfig, "None", StructureType.STRUCTURE_NONE, mapping);
        initStructureItem(cfg, mainCfg, idsInConfig, "Village", StructureType.STRUCTURE_VILLAGE, mapping);
        initStructureItem(cfg, mainCfg, idsInConfig, "Stronghold", StructureType.STRUCTURE_STRONGHOLD, mapping);
        initStructureItem(cfg, mainCfg, idsInConfig, "Dungeon", StructureType.STRUCTURE_DUNGEON, mapping);
        initStructureItem(cfg, mainCfg, idsInConfig, "Fortress", StructureType.STRUCTURE_FORTRESS, mapping);
        initStructureItem(cfg, mainCfg, idsInConfig, "Mineshaft", StructureType.STRUCTURE_MINESHAFT, mapping);
        initStructureItem(cfg, mainCfg, idsInConfig, "Scattered", StructureType.STRUCTURE_SCATTERED, mapping);
        addExtraInformation(idStructureNone, "With this none dimlet you can disable", "all normal structure spawning");

        int idTerrainVoid = initTerrainItem(cfg, mainCfg, idsInConfig, "Void", TerrainType.TERRAIN_VOID, mapping);
        int idTerrainFlat = initTerrainItem(cfg, mainCfg, idsInConfig, "Flat", TerrainType.TERRAIN_FLAT, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Amplified", TerrainType.TERRAIN_AMPLIFIED, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Normal", TerrainType.TERRAIN_NORMAL, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Cavern", TerrainType.TERRAIN_CAVERN, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Island", TerrainType.TERRAIN_ISLAND, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Islands", TerrainType.TERRAIN_ISLANDS, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Chaotic", TerrainType.TERRAIN_CHAOTIC, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Plateaus", TerrainType.TERRAIN_PLATEAUS, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Grid", TerrainType.TERRAIN_GRID, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Low Cavern", TerrainType.TERRAIN_LOW_CAVERN, mapping);
        initTerrainItem(cfg, mainCfg, idsInConfig, "Flooded Cavern", TerrainType.TERRAIN_FLOODED_CAVERN, mapping);

        int idFeatureNone = initFeatureItem(cfg, mainCfg, idsInConfig, "None", FeatureType.FEATURE_NONE, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Caves", FeatureType.FEATURE_CAVES, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Ravines", FeatureType.FEATURE_RAVINES, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Orbs", FeatureType.FEATURE_ORBS, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Oregen", FeatureType.FEATURE_OREGEN, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Lakes", FeatureType.FEATURE_LAKES, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Tendrils", FeatureType.FEATURE_TENDRILS, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Canyons", FeatureType.FEATURE_CANYONS, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Maze", FeatureType.FEATURE_MAZE, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Liquid Orbs", FeatureType.FEATURE_LIQUIDORBS, mapping);
        initFeatureItem(cfg, mainCfg, idsInConfig, "Shallow Ocean", FeatureType.FEATURE_SHALLOW_OCEAN, mapping);
        addExtraInformation(idFeatureNone, "With this none dimlet you can disable", "all special features");

        int idEffectNone = initEffectItem(cfg, mainCfg, idsInConfig, "None", EffectType.EFFECT_NONE, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Poison", EffectType.EFFECT_POISON, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Poison II", EffectType.EFFECT_POISON2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Poison III", EffectType.EFFECT_POISON3, mapping);
//        initEffectItem(cfg, mainCfg, idsInConfig, "No Gravity", EffectType.EFFECT_NOGRAVITY);
        initEffectItem(cfg, mainCfg, idsInConfig, "Regeneration", EffectType.EFFECT_REGENERATION, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Regeneration II", EffectType.EFFECT_REGENERATION2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Regeneration III", EffectType.EFFECT_REGENERATION3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Slowness", EffectType.EFFECT_MOVESLOWDOWN, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Slowness II", EffectType.EFFECT_MOVESLOWDOWN2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Slowness III", EffectType.EFFECT_MOVESLOWDOWN3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Slowness IV", EffectType.EFFECT_MOVESLOWDOWN4, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Speed", EffectType.EFFECT_MOVESPEED, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Speed II", EffectType.EFFECT_MOVESPEED2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Speed III", EffectType.EFFECT_MOVESPEED3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Mining Fatigue", EffectType.EFFECT_DIGSLOWDOWN, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Mining Fatigue II", EffectType.EFFECT_DIGSLOWDOWN2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Mining Fatigue III", EffectType.EFFECT_DIGSLOWDOWN3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Mining Fatigue IV", EffectType.EFFECT_DIGSLOWDOWN4, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Haste", EffectType.EFFECT_DIGSPEED, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Haste II", EffectType.EFFECT_DIGSPEED2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Haste III", EffectType.EFFECT_DIGSPEED3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Damage Boost", EffectType.EFFECT_DAMAGEBOOST, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Damage Boost II", EffectType.EFFECT_DAMAGEBOOST2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Damage Boost III", EffectType.EFFECT_DAMAGEBOOST3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Instant Health", EffectType.EFFECT_INSTANTHEALTH, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Harm", EffectType.EFFECT_HARM, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Jump", EffectType.EFFECT_JUMP, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Jump II", EffectType.EFFECT_JUMP2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Jump III", EffectType.EFFECT_JUMP3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Confusion", EffectType.EFFECT_CONFUSION, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Resistance", EffectType.EFFECT_RESISTANCE, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Resistance II", EffectType.EFFECT_RESISTANCE2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Resistance III", EffectType.EFFECT_RESISTANCE3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Fire Resistance", EffectType.EFFECT_FIRERESISTANCE, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Water Breathing", EffectType.EFFECT_WATERBREATHING, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Invisibility", EffectType.EFFECT_INVISIBILITY, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Blindness", EffectType.EFFECT_BLINDNESS, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Nightvision", EffectType.EFFECT_NIGHTVISION, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Hunger", EffectType.EFFECT_HUNGER, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Hunger II", EffectType.EFFECT_HUNGER2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Hunger III", EffectType.EFFECT_HUNGER3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Weakness", EffectType.EFFECT_WEAKNESS, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Weakness II", EffectType.EFFECT_WEAKNESS2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Weakness III", EffectType.EFFECT_WEAKNESS3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Wither", EffectType.EFFECT_WITHER, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Wither II", EffectType.EFFECT_WITHER2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Wither III", EffectType.EFFECT_WITHER3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Health Boost", EffectType.EFFECT_HEALTHBOOST, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Health Boost II", EffectType.EFFECT_HEALTHBOOST2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Health Boost III", EffectType.EFFECT_HEALTHBOOST3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Absorption", EffectType.EFFECT_ABSORPTION, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Absorption II", EffectType.EFFECT_ABSORPTION2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Absorption III", EffectType.EFFECT_ABSORPTION3, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Saturation", EffectType.EFFECT_SATURATION, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Saturation II", EffectType.EFFECT_SATURATION2, mapping);
        initEffectItem(cfg, mainCfg, idsInConfig, "Saturation III", EffectType.EFFECT_SATURATION3, mapping);
        addExtraInformation(idEffectNone, "With this none dimlet you can disable", "all special effects");

        int idNormalTime = initTimeItem(cfg, mainCfg, idsInConfig, "Normal", null, null, mapping);
        initTimeItem(cfg, mainCfg, idsInConfig, "Noon", 0.0f, null, mapping);
        initTimeItem(cfg, mainCfg, idsInConfig, "Midnight", 0.5f, null, mapping);
        initTimeItem(cfg, mainCfg, idsInConfig, "Morning", 0.2f, null, mapping);
        initTimeItem(cfg, mainCfg, idsInConfig, "Evening", 0.75f, null, mapping);
        initTimeItem(cfg, mainCfg, idsInConfig, "Fast", null, 2.0f, mapping);
        initTimeItem(cfg, mainCfg, idsInConfig, "Slow", null, 0.5f, mapping);
        addExtraInformation(idNormalTime, "With this normal dimlet you will get", "default day/night timing");

        initBiomeItems(cfg, mainCfg, idsInConfig, mapping);
        initLiquidItems(cfg, mainCfg, idsInConfig, mapping);

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

        readUserDimlets(cfg, mainCfg, idsInConfig, modConfigDir, mapping);

        DimletRandomizer.setupWeightedRandomList(mainCfg, mapping);
        setupChestLoot();

        if (mainCfg.hasChanged()) {
            mainCfg.save();
        }
        if (cfg.hasChanged()) {
            cfg.save();
        }

        mapping.save(world);
    }

    private static void initDigitCrafting(String from, String to, DimletMapping mapping) {
        int idFrom = mapping.getId(DimletType.DIMLET_DIGIT, from);
        int idTo = mapping.getId(DimletType.DIMLET_DIGIT, to);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idTo), "   ", " 9 ", "   ", '9', new ItemStack(ModItems.knownDimlet, 1, idFrom));
    }

    public static void initCrafting(World world) {
        DimletMapping mapping = DimletMapping.getDimletMapping(world);

        List recipeList = CraftingManager.getInstance().getRecipeList();
        int i = 0;
        while (i < recipeList.size()) {
            if (recipeList.get(i) instanceof ShapedRecipes) {
                ShapedRecipes r = (ShapedRecipes) recipeList.get(i);
                if (r.getRecipeOutput().getItem() == ModItems.knownDimlet && r.recipeItems[4].getItem() == ModItems.knownDimlet) {
                    recipeList.remove(i);
                    i--;
                }
            } else if (recipeList.get(i) instanceof KnownDimletShapedRecipe) {
                recipeList.remove(i);
                i--;
            }
            i++;
        }

        initDigitCrafting("0", "1", mapping);
        initDigitCrafting("1", "2", mapping);
        initDigitCrafting("2", "3", mapping);
        initDigitCrafting("3", "4", mapping);
        initDigitCrafting("4", "5", mapping);
        initDigitCrafting("5", "6", mapping);
        initDigitCrafting("6", "7", mapping);
        initDigitCrafting("7", "8", mapping);
        initDigitCrafting("8", "9", mapping);
        initDigitCrafting("9", "0", mapping);

        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_EFFECT, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.apple, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_FEATURE, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.string, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_STRUCTURE, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bone, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_TERRAIN, "Void"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_TERRAIN, "Flat"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_CONTROLLER, "Default"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.comparator, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_CONTROLLER, "Single"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.comparator, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_MATERIAL, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Blocks.dirt, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_LIQUID, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bucket, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_SKY, "Normal"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.feather, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_SKY, "Normal Day"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.glowstone_dust, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_SKY, "Normal Night"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.coal, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_MOBS, "Default"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.rotten_flesh, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_TIME, "Normal"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.clock, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_DIGIT, "0"), " r ", "rtr", "ppp", 'r', Items.redstone, 't', redstoneTorch, 'p', Items.paper));
    }

    private static int initModMaterialItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String modid, String blockname, int meta, DimletMapping mapping) {
        Block block = GameRegistry.findBlock(modid, blockname);
        if (block != null) {
            return initMaterialItem(cfg, mainCfg, idsInConfig, block, meta, mapping);
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

    private static int initDigitItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, int digit, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_DIGIT, "" + digit), mapping);
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_DIGIT.getName() + " " + digit + " Dimlet");
            DimletObjectMapping.idToDigit.put(id, String.valueOf(digit));
        }
        return id;
    }

    private static int initMaterialItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, Block block, int meta, DimletMapping mapping) {
        String unlocalizedName = block.getUnlocalizedName();
        if (meta != 0) {
            unlocalizedName += meta;
        }
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_MATERIAL, unlocalizedName), mapping);
        if (id != -1) {
            ItemStack stack = new ItemStack(block, 1, meta);
            idToDisplayName.put(id, DimletType.DIMLET_MATERIAL.getName() + " " + stack.getDisplayName() + " Dimlet");
            DimletObjectMapping.idToBlock.put(id, new BlockMeta(block, (byte)meta));
        }
        return id;
    }

    /**
     * Read user-specified dimlets.
     */
    private static void readUserDimlets(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, File modConfigDir, DimletMapping mapping) {
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
                    int id = initModMaterialItem(cfg, mainCfg, idsInConfig, modid, name, meta, mapping);
                    if (id != -1) {
                        DimletKey key = mapping.getEntry(id).getKey();
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

    private static int initControllerItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, ControllerType type, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_CONTROLLER, name), mapping);
        if (id != -1) {
            DimletObjectMapping.idToControllerType.put(id, type);
            idToDisplayName.put(id, DimletType.DIMLET_CONTROLLER.getName() + " " + name + " Dimlet");
        }
        return -1;
    }

    private static void initBiomeItems(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, DimletMapping mapping) {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                String name = biome.biomeName;
                int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_BIOME, name), mapping);
                if (id != -1) {
                    DimletObjectMapping.idToBiome.put(id, biome);
                    idToDisplayName.put(id, DimletType.DIMLET_BIOME.getName() + " " + name + " Dimlet");
                }
            }
        }
    }

    private static void initFoliageItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_FOLIAGE, "Oak"), mapping);
        if (id != -1) {
            idToDisplayName.put(id, "Foliage Oak Dimlet");
        }
    }

    private static void initLiquidItems(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, DimletMapping mapping) {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            if (me.getValue().canBePlacedInWorld()) {
                try {
                    String displayName = new FluidStack(me.getValue(), 1).getLocalizedName();
                    int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_LIQUID, me.getKey()), mapping);
                    if (id != -1) {
                        DimletObjectMapping.idToFluid.put(id, me.getValue().getBlock());
                        idToDisplayName.put(id, DimletType.DIMLET_LIQUID.getName() + " " + displayName + " Dimlet");
                    }
                } catch (Exception e) {
                    RFTools.logError("Something went wrong getting the name of a fluid:");
                    RFTools.logError("Fluid: " + me.getKey() + ", unlocalizedName: " + me.getValue().getUnlocalizedName());
                }
            }
        }
    }

    private static int initSpecialItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, SpecialType specialType, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_SPECIAL, name), mapping);
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_SPECIAL.getName() + " " + name + " Dimlet");
            DimletObjectMapping.idToSpecialType.put(id, specialType);
        }
        return id;
    }

    private static int initMobItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, Class<? extends EntityLiving> entity, String name,
                                   DimletMapping mapping, int chance, int mingroup, int maxgroup, int maxentity) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_MOBS, name), mapping);
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_MOBS.getName() + " " + name + " Dimlet");
            chance = mainCfg.get(CATEGORY_MOBSPAWNS, name + ".chance", chance).getInt();
            mingroup = mainCfg.get(CATEGORY_MOBSPAWNS, name + ".mingroup", mingroup).getInt();
            maxgroup = mainCfg.get(CATEGORY_MOBSPAWNS, name + ".maxgroup", maxgroup).getInt();
            maxentity = mainCfg.get(CATEGORY_MOBSPAWNS, name + ".maxentity", maxentity).getInt();
            DimletObjectMapping.idtoMob.put(id, new MobDescriptor(entity, chance, mingroup, maxgroup, maxentity));
        }
        return id;
    }

    private static int initSkyItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, SkyDescriptor skyDescriptor, boolean isbody, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_SKY, name), mapping);
        if (id != -1) {
            DimletObjectMapping.idToSkyDescriptor.put(id, skyDescriptor);
            idToDisplayName.put(id, DimletType.DIMLET_SKY.getName() + " " + name + " Dimlet");
            if (isbody) {
                DimletObjectMapping.celestialBodies.add(id);
            }
        }
        return id;
    }

    private static int initStructureItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, StructureType structureType, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_STRUCTURE, name), mapping);
        if (id != -1) {
            DimletObjectMapping.idToStructureType.put(id, structureType);
            idToDisplayName.put(id, DimletType.DIMLET_STRUCTURE.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTerrainItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, TerrainType terrainType, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_TERRAIN, name), mapping);
        if (id != -1) {
            DimletObjectMapping.idToTerrainType.put(id, terrainType);
            idToDisplayName.put(id, DimletType.DIMLET_TERRAIN.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initEffectItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, EffectType effectType, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_EFFECT, "" + name), mapping);
        if (id != -1) {
            idToDisplayName.put(id, DimletType.DIMLET_EFFECT.getName() + " " + name + " Dimlet");
            DimletObjectMapping.idToEffectType.put(id, effectType);
        }
        return id;
    }

    private static int initFeatureItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, FeatureType featureType, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_FEATURE, name), mapping);
        if (id != -1) {
            DimletObjectMapping.idToFeatureType.put(id, featureType);
            idToDisplayName.put(id, DimletType.DIMLET_FEATURE.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTimeItem(Configuration cfg, Configuration mainCfg, Map<DimletKey, Integer> idsInConfig, String name, Float angle, Float speed, DimletMapping mapping) {
        int id = registerDimlet(cfg, mainCfg, idsInConfig, new DimletKey(DimletType.DIMLET_TIME, name), mapping);
        if (id != -1) {
            DimletObjectMapping.idToCelestialAngle.put(id, angle);
            DimletObjectMapping.idToSpeed.put(id, speed);
            idToDisplayName.put(id, DimletType.DIMLET_TIME.getName() + " " + name + " Dimlet");
        }
        return id;
    }

}

package com.mcjty.rftools.items.dimlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mcjty.rftools.CommonProxy;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.crafting.KnownDimletShapedRecipe;
import com.mcjty.rftools.dimension.description.SkyDescriptor;
import com.mcjty.rftools.dimension.description.WeatherDescriptor;
import com.mcjty.rftools.dimension.world.types.*;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.varia.BlockMeta;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.oredict.OreDictionary;

import java.io.*;
import java.util.*;

public class KnownDimletConfiguration {
    public static final String CATEGORY_KNOWNDIMLETS = "knowndimlets";              // This is part of dimlets.cfg
    public static final String CATEGORY_DIMLETSETTINGS = "dimletsettings";          // This is part of dimlets.cfg
    public static final String CATEGORY_RARITY = "rarity";
    public static final String CATEGORY_TYPERARIRTY = "typerarity";
    public static final String CATEGORY_TYPERFCREATECOST = "typerfcreatecost";
    public static final String CATEGORY_TYPERFMAINTAINCOST = "typerfmaintaincost";
    public static final String CATEGORY_TYPETICKCOST = "typetickcost";
    public static final String CATEGORY_MOBSPAWNS = "mobspawns";
    public static final String CATEGORY_GENERAL = "general";

    // Map the id of a dimlet to a display name.
    public static final Map<DimletKey,String> idToDisplayName = new HashMap<DimletKey, String>();
    public static final Map<DimletKey,DimletEntry> idToDimletEntry = new HashMap<DimletKey, DimletEntry>();

    // Map the id of a dimlet to extra information for the tooltip.
    public static final Map<DimletKey,List<String>> idToExtraInformation = new HashMap<DimletKey, List<String>>();

    // All craftable dimlets.
    public static final Set<DimletKey> craftableDimlets = new HashSet<DimletKey>();

    private static final Set<DimletKey> dimletBlackList = new HashSet<DimletKey>(); // Note, the keys here can contain wildcards
    private static final Set<DimletKey> dimletRandomNotAllowed = new HashSet<DimletKey>();

    private static int lastId = 0;

    public static void initGeneralConfig(Configuration cfg) {
        DimletCosts.baseDimensionCreationCost = cfg.get(CATEGORY_GENERAL, "baseDimensionCreationCost", DimletCosts.baseDimensionCreationCost,
                "The base cost (in RF/tick) for creating a dimension").getInt();
        DimletCosts.baseDimensionMaintenanceCost = cfg.get(CATEGORY_GENERAL, "baseDimensionMaintenanceCost", DimletCosts.baseDimensionMaintenanceCost,
                "The base cost (in RF/tick) for maintaining a dimension").getInt();
        DimletCosts.baseDimensionTickCost = cfg.get(CATEGORY_GENERAL, "baseDimensionTickCost", DimletCosts.baseDimensionTickCost,
                "The base time (in ticks) for creating a dimension").getInt();

        DimletRandomizer.readRandomConfig(cfg);
        MobConfiguration.readMobConfig(cfg);
    }


    private static void registerDimletEntry(int id, DimletEntry dimletEntry, DimletMapping mapping) {
        DimletKey key = dimletEntry.getKey();
        mapping.registerDimletKey(id, key);
        DimletRandomizer.dimletIds.add(key);
        idToDimletEntry.put(key, dimletEntry);
    }

    private static boolean isBlacklistedKey(DimletKey key) {
        if (dimletBlackList.contains(key)) {
            return true;
        }

        for (DimletKey blackKey : dimletBlackList) {
            if (key.getType() == blackKey.getType()) {
                String blackName = blackKey.getName();
                if (blackName.endsWith("*")) {
                    if (key.getName().startsWith(blackName.substring(0, blackName.length()-1))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static int registerDimlet(Configuration cfg, DimletKey key, DimletMapping mapping) {
        String k = "dimlet." + key.getType().getName() + "." + key.getName();

        Integer id = mapping.getId(key);

        // Check blacklist but not if we are on a client connecting to a server.
        if (cfg != null && isBlacklistedKey(key)) {
            id = -1;
        } else if (id == null) {
            // We don't have this key in world data.
            if (cfg == null) {
                // We are on a client connecting to a server. This dimlet does not exist on the server.
                id = -1;
            } else if (cfg.hasKey(CATEGORY_KNOWNDIMLETS, k)) {
                // Import it from the old config.
                id = cfg.get(CATEGORY_KNOWNDIMLETS, k, -1).getInt();
            } else {
                // Not in world and not in user config. We need a new id.
                id = lastId + 1;
                lastId = id;
            }
        } else {
            // This key exists in world data. Check if it is not blacklisted by the user (if we are not on a client connecting to a server).
            if (cfg != null && cfg.hasKey(CATEGORY_KNOWNDIMLETS, k)) {
                int blackid = cfg.get(CATEGORY_KNOWNDIMLETS, k, -1).getInt();
                if (blackid == -1) {
                    id = -1;
                }
            }
        }

        if (id == -1) {
            // Remove from mapping if it is there.
            mapping.removeId(id);
            RFTools.log("Blacklisted dimlet " + key.getType().getName() + ", " + key.getName());
            return id;
        }

        int rfCreateCost = checkCostConfig(cfg, "rfcreate.", key, DimletCosts.dimletBuiltinRfCreate, DimletCosts.typeRfCreateCost);
        int rfMaintainCost = checkCostConfig(cfg, "rfmaintain.", key, DimletCosts.dimletBuiltinRfMaintain, DimletCosts.typeRfMaintainCost);
        int tickCost = checkCostConfig(cfg, "ticks.", key, DimletCosts.dimletBuiltinTickCost, DimletCosts.typeTickCost);
        int rarity = checkCostConfig(cfg, "rarity.", key, DimletRandomizer.dimletBuiltinRarity, DimletRandomizer.typeRarity);
        boolean randomNotAllowed = checkFlagConfig(cfg, "expensive.", key, dimletRandomNotAllowed);

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

        idToDisplayName.clear();
        idToDimletEntry.clear();
        craftableDimlets.clear();
        dimletBlackList.clear();
        dimletRandomNotAllowed.clear();

        DimletObjectMapping.clean();
        DimletRandomizer.clean();
    }

    private static void addExtraInformation(DimletKey key, String... info) {
        List<String> extraInfo = new ArrayList<String>();
        Collections.addAll(extraInfo, info);
        idToExtraInformation.put(key, extraInfo);
    }

    public static boolean isInitialized() {
        return DimletMapping.isInitialized();
    }

    public static void init(World world) {
        File modConfigDir = CommonProxy.modConfigDir;
        Configuration cfg = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "dimlets.cfg"));
        cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_KNOWNDIMLETS, "Dimlet configuration");
        cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_DIMLETSETTINGS, "Settings for specific dimlets");

        cfg.load();
        init(world, cfg);
        if (cfg.hasChanged()) {
            cfg.save();
        }
    }

    /**
     * This initializes all dimlets based on all loaded mods.
     */
    public static void init(World world, Configuration cfg) {
        clean();

        DimletMapping mapping = DimletMapping.getDimletMapping(world);

        File modConfigDir = CommonProxy.modConfigDir;

        readBuiltinConfig();

        updateLastId(cfg, mapping);

        initControllerItem(cfg, "Default", ControllerType.CONTROLLER_DEFAULT, mapping);
        initControllerItem(cfg, "Single", ControllerType.CONTROLLER_SINGLE, mapping);
        initControllerItem(cfg, "Checkerboard", ControllerType.CONTROLLER_CHECKERBOARD, mapping);
        initControllerItem(cfg, "Cold", ControllerType.CONTROLLER_COLD, mapping);
        initControllerItem(cfg, "Medium", ControllerType.CONTROLLER_MEDIUM, mapping);
        initControllerItem(cfg, "Warm", ControllerType.CONTROLLER_WARM, mapping);
        initControllerItem(cfg, "Dry", ControllerType.CONTROLLER_DRY, mapping);
        initControllerItem(cfg, "Wet", ControllerType.CONTROLLER_WET, mapping);
        initControllerItem(cfg, "Fields", ControllerType.CONTROLLER_FIELDS, mapping);
        initControllerItem(cfg, "Mountains", ControllerType.CONTROLLER_MOUNTAINS, mapping);
        initControllerItem(cfg, "Filtered", ControllerType.CONTROLLER_FILTERED, mapping);
        initControllerItem(cfg, "Magical", ControllerType.CONTROLLER_MAGICAL, mapping);
        initControllerItem(cfg, "Forest", ControllerType.CONTROLLER_FOREST, mapping);
        BiomeControllerMapping.setupControllerBiomes();
        DimletKey keyControllerDefault = new DimletKey(DimletType.DIMLET_CONTROLLER, "Default");
        addExtraInformation(keyControllerDefault, "The Default controller just uses the same", "biome distribution as the overworld");

        initDigitItem(cfg, 0, mapping);
        initDigitItem(cfg, 1, mapping);
        initDigitItem(cfg, 2, mapping);
        initDigitItem(cfg, 3, mapping);
        initDigitItem(cfg, 4, mapping);
        initDigitItem(cfg, 5, mapping);
        initDigitItem(cfg, 6, mapping);
        initDigitItem(cfg, 7, mapping);
        initDigitItem(cfg, 8, mapping);
        initDigitItem(cfg, 9, mapping);

        DimletKey keyMaterialNone = new DimletKey(DimletType.DIMLET_MATERIAL, "None");
        registerDimlet(cfg, keyMaterialNone, mapping);
        idToDisplayName.put(keyMaterialNone, DimletType.DIMLET_MATERIAL.getName() + " None Dimlet");
        DimletObjectMapping.idToBlock.put(keyMaterialNone, null);
        addExtraInformation(keyMaterialNone, "Use this material none dimlet to get normal", "biome specific stone generation");

        initMaterialDimlets(cfg, mapping);

        initFoliageItem(cfg, mapping);

        DimletKey keyLiquidNone = new DimletKey(DimletType.DIMLET_LIQUID, "None");
        registerDimlet(cfg, keyLiquidNone, mapping);
        DimletObjectMapping.idToFluid.put(keyLiquidNone, null);
        idToDisplayName.put(keyLiquidNone, DimletType.DIMLET_LIQUID.getName() + " None Dimlet");
        addExtraInformation(keyLiquidNone, "Use this liquid none dimlet to get normal", "water generation");

        initSpecialItem(cfg, "Peaceful", SpecialType.SPECIAL_PEACEFUL, mapping);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Peaceful"), "Normal mob spawning is disabled", "if you use this dimlet");
        initSpecialItem(cfg, "Efficiency", SpecialType.SPECIAL_EFFICIENCY, mapping);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Efficiency"), "Reduce the maintenance RF/tick of the", "generated dimension with 20%", "This is cumulative");
        initSpecialItem(cfg, "Mediocre Efficiency", SpecialType.SPECIAL_EFFICIENCY_LOW, mapping);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Mediocre Efficiency"), "Reduce the maintenance RF/tick of the", "generated dimension with 5%", "This is cumulative");
        initSpecialItem(cfg, "Shelter", SpecialType.SPECIAL_SHELTER, mapping);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Shelter"), "Generate a better sheltered spawn", "platform in the dimension");
        initSpecialItem(cfg, "Seed", SpecialType.SPECIAL_SEED, mapping);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Seed"), "Force a specific seed for a dimension.", "Right click in dimension to store seed.", "Shift-right click to lock seed");

        initMobItem(cfg, "Default", mapping);
        initMobItem(cfg, "Zombie", mapping);
        initMobItem(cfg, "Skeleton", mapping);
        initMobItem(cfg, "Enderman", mapping);
        initMobItem(cfg, "Blaze", mapping);
        initMobItem(cfg, "Creeper", mapping);
        initMobItem(cfg, "Cave Spider", mapping);
        initMobItem(cfg, "Ghast", mapping);
        initMobItem(cfg, "Iron Golem", mapping);
        initMobItem(cfg, "Magma Cube", mapping);
        initMobItem(cfg, "Zombie Pigman", mapping);
        initMobItem(cfg, "Slime", mapping);
        initMobItem(cfg, "Snowman", mapping);
        initMobItem(cfg, "Spider", mapping);
        initMobItem(cfg, "Witch", mapping);
        initMobItem(cfg, "Bat", mapping);
        initMobItem(cfg, "Chicken", mapping);
        initMobItem(cfg, "Cow", mapping);
        initMobItem(cfg, "Horse", mapping);
        initMobItem(cfg, "Mooshroom", mapping);
        initMobItem(cfg, "Ocelot", mapping);
        initMobItem(cfg, "Pig", mapping);
        initMobItem(cfg, "Sheep", mapping);
        initMobItem(cfg, "Squid", mapping);
        initMobItem(cfg, "Wolf", mapping);
        initMobItem(cfg, "Villager", mapping);
        initMobItem(cfg, "Wither", mapping);
        initMobItem(cfg, "Dragon", mapping);
        DimletKey keyDefaultMobs = new DimletKey(DimletType.DIMLET_MOBS, "Default");
        addExtraInformation(keyDefaultMobs, "With this default dimlet you will just get", "the default mob spawning");

        initSkyItem(cfg, "Normal", new SkyDescriptor.Builder().skyType(SkyType.SKY_NORMAL).build(), false, mapping);
        initSkyItem(cfg, "Normal Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.0f).build(), false, mapping);
        initSkyItem(cfg, "Bright Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.5f).build(), false, mapping);
        initSkyItem(cfg, "Dark Day", new SkyDescriptor.Builder().sunBrightnessFactor(0.4f).skyColorFactor(0.6f, 0.6f, 0.6f).build(), false, mapping);
        initSkyItem(cfg, "Normal Night", new SkyDescriptor.Builder().starBrightnessFactor(1.0f).build(), false, mapping);
        initSkyItem(cfg, "Bright Night", new SkyDescriptor.Builder().starBrightnessFactor(1.5f).build(), false, mapping);
        initSkyItem(cfg, "Dark Night", new SkyDescriptor.Builder().starBrightnessFactor(0.4f).build(), false, mapping);
        initSkyItem(cfg, "Red Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, "Green Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, "Blue Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 0.2f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, "Yellow Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 1.0f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, "Cyan Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, "Purple Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 1.0f).build(), false, mapping);

        initSkyItem(cfg, "Normal Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 1.0f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, "Black Fog", new SkyDescriptor.Builder().fogColorFactor(0.0f, 0.0f, 0.0f).build(), false, mapping);
        initSkyItem(cfg, "Red Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 0.2f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, "Green Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 1.0f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, "Blue Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 0.2f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, "Yellow Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 1.0f, 0.2f).build(), false, mapping);
        initSkyItem(cfg, "Cyan Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 1.0f, 1.0f).build(), false, mapping);
        initSkyItem(cfg, "Purple Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 0.2f, 1.0f).build(), false, mapping);

        initSkyItem(cfg, "Ender", new SkyDescriptor.Builder().skyType(SkyType.SKY_ENDER).build(), false, mapping);
        initSkyItem(cfg, "Inferno", new SkyDescriptor.Builder().skyType(SkyType.SKY_INFERNO).build(), false, mapping);

        initSkyItem(cfg, "Body None", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_NONE).build(), false, mapping);   // False because we don't want to select this randomly.
        initSkyItem(cfg, "Body Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SUN).build(), true, mapping);
        initSkyItem(cfg, "Body Large Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGESUN).build(), true, mapping);
        initSkyItem(cfg, "Body Small Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SMALLSUN).build(), true, mapping);
        initSkyItem(cfg, "Body Red Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_REDSUN).build(), true, mapping);
        initSkyItem(cfg, "Body Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_MOON).build(), true, mapping);
        initSkyItem(cfg, "Body Large Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGEMOON).build(), true, mapping);
        initSkyItem(cfg, "Body Small Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SMALLMOON).build(), true, mapping);
        initSkyItem(cfg, "Body Red Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_REDMOON).build(), true, mapping);
        initSkyItem(cfg, "Body Planet", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_PLANET).build(), true, mapping);
        initSkyItem(cfg, "Body Large Planet", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGEPLANET).build(), true, mapping);

        DimletKey keySkyNormal = new DimletKey(DimletType.DIMLET_SKY, "Normal");
        DimletKey keySkyNormalDay = new DimletKey(DimletType.DIMLET_SKY, "Normal Day");
        DimletKey keySkyNormalNight = new DimletKey(DimletType.DIMLET_SKY, "Normal Night");
        addExtraInformation(keySkyNormal, "A normal type of sky", "(as opposed to ender or inferno)");
        addExtraInformation(keySkyNormalDay, "Normal brightness level for daytime sky");
        addExtraInformation(keySkyNormalNight, "Normal brightness level for nighttime sky");

        initWeatherItem(cfg, "Default", new WeatherDescriptor.Builder().build(), mapping);
        initWeatherItem(cfg, "No Rain", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_NORAIN).build(), mapping);
        initWeatherItem(cfg, "Light Rain", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_LIGHTRAIN).build(), mapping);
        initWeatherItem(cfg, "Hard Rain", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_HARDRAIN).build(), mapping);
        initWeatherItem(cfg, "No Thunder", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_NOTHUNDER).build(), mapping);
        initWeatherItem(cfg, "Light Thunder", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_LIGHTTHUNDER).build(), mapping);
        initWeatherItem(cfg, "Hard Thunder", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_HARDTHUNDER).build(), mapping);

        addExtraInformation(new DimletKey(DimletType.DIMLET_WEATHER, "Default"), "Normal default weather");

        initStructureItem(cfg, "None", StructureType.STRUCTURE_NONE, mapping);
        initStructureItem(cfg, "Village", StructureType.STRUCTURE_VILLAGE, mapping);
        initStructureItem(cfg, "Stronghold", StructureType.STRUCTURE_STRONGHOLD, mapping);
        initStructureItem(cfg, "Dungeon", StructureType.STRUCTURE_DUNGEON, mapping);
        initStructureItem(cfg, "Fortress", StructureType.STRUCTURE_FORTRESS, mapping);
        initStructureItem(cfg, "Mineshaft", StructureType.STRUCTURE_MINESHAFT, mapping);
        initStructureItem(cfg, "Scattered", StructureType.STRUCTURE_SCATTERED, mapping);
        DimletKey keyStructureNone = new DimletKey(DimletType.DIMLET_STRUCTURE, "None");
        addExtraInformation(keyStructureNone, "With this none dimlet you can disable", "all normal structure spawning");

        initTerrainItem(cfg, "Void", TerrainType.TERRAIN_VOID, mapping);
        initTerrainItem(cfg, "Flat", TerrainType.TERRAIN_FLAT, mapping);
        initTerrainItem(cfg, "Amplified", TerrainType.TERRAIN_AMPLIFIED, mapping);
        initTerrainItem(cfg, "Normal", TerrainType.TERRAIN_NORMAL, mapping);
        initTerrainItem(cfg, "Cavern", TerrainType.TERRAIN_CAVERN, mapping);
        initTerrainItem(cfg, "Island", TerrainType.TERRAIN_ISLAND, mapping);
        initTerrainItem(cfg, "Islands", TerrainType.TERRAIN_ISLANDS, mapping);
        initTerrainItem(cfg, "Chaotic", TerrainType.TERRAIN_CHAOTIC, mapping);
        initTerrainItem(cfg, "Plateaus", TerrainType.TERRAIN_PLATEAUS, mapping);
        initTerrainItem(cfg, "Grid", TerrainType.TERRAIN_GRID, mapping);
        initTerrainItem(cfg, "Low Cavern", TerrainType.TERRAIN_LOW_CAVERN, mapping);
        initTerrainItem(cfg, "Flooded Cavern", TerrainType.TERRAIN_FLOODED_CAVERN, mapping);

        initFeatureItem(cfg, "None", FeatureType.FEATURE_NONE, mapping);
        initFeatureItem(cfg, "Caves", FeatureType.FEATURE_CAVES, mapping);
        initFeatureItem(cfg, "Ravines", FeatureType.FEATURE_RAVINES, mapping);
        initFeatureItem(cfg, "Orbs", FeatureType.FEATURE_ORBS, mapping);
        initFeatureItem(cfg, "Oregen", FeatureType.FEATURE_OREGEN, mapping);
        initFeatureItem(cfg, "Lakes", FeatureType.FEATURE_LAKES, mapping);
        initFeatureItem(cfg, "Tendrils", FeatureType.FEATURE_TENDRILS, mapping);
        initFeatureItem(cfg, "Canyons", FeatureType.FEATURE_CANYONS, mapping);
        initFeatureItem(cfg, "Maze", FeatureType.FEATURE_MAZE, mapping);
        initFeatureItem(cfg, "Liquid Orbs", FeatureType.FEATURE_LIQUIDORBS, mapping);
        initFeatureItem(cfg, "Shallow Ocean", FeatureType.FEATURE_SHALLOW_OCEAN, mapping);
        addExtraInformation(new DimletKey(DimletType.DIMLET_FEATURE, "None"), "With this none dimlet you can disable", "all special features");

        initEffectItem(cfg, "None", EffectType.EFFECT_NONE, mapping);
        initEffectItem(cfg, "Poison", EffectType.EFFECT_POISON, mapping);
        initEffectItem(cfg, "Poison II", EffectType.EFFECT_POISON2, mapping);
        initEffectItem(cfg, "Poison III", EffectType.EFFECT_POISON3, mapping);
//        initEffectItem(cfg, idsInConfig, "No Gravity", EffectType.EFFECT_NOGRAVITY);
        initEffectItem(cfg, "Regeneration", EffectType.EFFECT_REGENERATION, mapping);
        initEffectItem(cfg, "Regeneration II", EffectType.EFFECT_REGENERATION2, mapping);
        initEffectItem(cfg, "Regeneration III", EffectType.EFFECT_REGENERATION3, mapping);
        initEffectItem(cfg, "Slowness", EffectType.EFFECT_MOVESLOWDOWN, mapping);
        initEffectItem(cfg, "Slowness II", EffectType.EFFECT_MOVESLOWDOWN2, mapping);
        initEffectItem(cfg, "Slowness III", EffectType.EFFECT_MOVESLOWDOWN3, mapping);
        initEffectItem(cfg, "Slowness IV", EffectType.EFFECT_MOVESLOWDOWN4, mapping);
        initEffectItem(cfg, "Speed", EffectType.EFFECT_MOVESPEED, mapping);
        initEffectItem(cfg, "Speed II", EffectType.EFFECT_MOVESPEED2, mapping);
        initEffectItem(cfg, "Speed III", EffectType.EFFECT_MOVESPEED3, mapping);
        initEffectItem(cfg, "Mining Fatigue", EffectType.EFFECT_DIGSLOWDOWN, mapping);
        initEffectItem(cfg, "Mining Fatigue II", EffectType.EFFECT_DIGSLOWDOWN2, mapping);
        initEffectItem(cfg, "Mining Fatigue III", EffectType.EFFECT_DIGSLOWDOWN3, mapping);
        initEffectItem(cfg, "Mining Fatigue IV", EffectType.EFFECT_DIGSLOWDOWN4, mapping);
        initEffectItem(cfg, "Haste", EffectType.EFFECT_DIGSPEED, mapping);
        initEffectItem(cfg, "Haste II", EffectType.EFFECT_DIGSPEED2, mapping);
        initEffectItem(cfg, "Haste III", EffectType.EFFECT_DIGSPEED3, mapping);
        initEffectItem(cfg, "Damage Boost", EffectType.EFFECT_DAMAGEBOOST, mapping);
        initEffectItem(cfg, "Damage Boost II", EffectType.EFFECT_DAMAGEBOOST2, mapping);
        initEffectItem(cfg, "Damage Boost III", EffectType.EFFECT_DAMAGEBOOST3, mapping);
        initEffectItem(cfg, "Instant Health", EffectType.EFFECT_INSTANTHEALTH, mapping);
        initEffectItem(cfg, "Harm", EffectType.EFFECT_HARM, mapping);
        initEffectItem(cfg, "Jump", EffectType.EFFECT_JUMP, mapping);
        initEffectItem(cfg, "Jump II", EffectType.EFFECT_JUMP2, mapping);
        initEffectItem(cfg, "Jump III", EffectType.EFFECT_JUMP3, mapping);
        initEffectItem(cfg, "Confusion", EffectType.EFFECT_CONFUSION, mapping);
        initEffectItem(cfg, "Resistance", EffectType.EFFECT_RESISTANCE, mapping);
        initEffectItem(cfg, "Resistance II", EffectType.EFFECT_RESISTANCE2, mapping);
        initEffectItem(cfg, "Resistance III", EffectType.EFFECT_RESISTANCE3, mapping);
        initEffectItem(cfg, "Fire Resistance", EffectType.EFFECT_FIRERESISTANCE, mapping);
        initEffectItem(cfg, "Water Breathing", EffectType.EFFECT_WATERBREATHING, mapping);
        initEffectItem(cfg, "Invisibility", EffectType.EFFECT_INVISIBILITY, mapping);
        initEffectItem(cfg, "Blindness", EffectType.EFFECT_BLINDNESS, mapping);
        initEffectItem(cfg, "Nightvision", EffectType.EFFECT_NIGHTVISION, mapping);
        initEffectItem(cfg, "Hunger", EffectType.EFFECT_HUNGER, mapping);
        initEffectItem(cfg, "Hunger II", EffectType.EFFECT_HUNGER2, mapping);
        initEffectItem(cfg, "Hunger III", EffectType.EFFECT_HUNGER3, mapping);
        initEffectItem(cfg, "Weakness", EffectType.EFFECT_WEAKNESS, mapping);
        initEffectItem(cfg, "Weakness II", EffectType.EFFECT_WEAKNESS2, mapping);
        initEffectItem(cfg, "Weakness III", EffectType.EFFECT_WEAKNESS3, mapping);
        initEffectItem(cfg, "Wither", EffectType.EFFECT_WITHER, mapping);
        initEffectItem(cfg, "Wither II", EffectType.EFFECT_WITHER2, mapping);
        initEffectItem(cfg, "Wither III", EffectType.EFFECT_WITHER3, mapping);
        initEffectItem(cfg, "Health Boost", EffectType.EFFECT_HEALTHBOOST, mapping);
        initEffectItem(cfg, "Health Boost II", EffectType.EFFECT_HEALTHBOOST2, mapping);
        initEffectItem(cfg, "Health Boost III", EffectType.EFFECT_HEALTHBOOST3, mapping);
        initEffectItem(cfg, "Absorption", EffectType.EFFECT_ABSORPTION, mapping);
        initEffectItem(cfg, "Absorption II", EffectType.EFFECT_ABSORPTION2, mapping);
        initEffectItem(cfg, "Absorption III", EffectType.EFFECT_ABSORPTION3, mapping);
        initEffectItem(cfg, "Saturation", EffectType.EFFECT_SATURATION, mapping);
        initEffectItem(cfg, "Saturation II", EffectType.EFFECT_SATURATION2, mapping);
        initEffectItem(cfg, "Saturation III", EffectType.EFFECT_SATURATION3, mapping);
        addExtraInformation(new DimletKey(DimletType.DIMLET_EFFECT, "None"), "With this none dimlet you can disable", "all special effects");

        initTimeItem(cfg, "Normal", null, null, mapping);
        initTimeItem(cfg, "Noon", 0.0f, null, mapping);
        initTimeItem(cfg, "Midnight", 0.5f, null, mapping);
        initTimeItem(cfg, "Morning", 0.2f, null, mapping);
        initTimeItem(cfg, "Evening", 0.75f, null, mapping);
        initTimeItem(cfg, "Fast", null, 2.0f, mapping);
        initTimeItem(cfg, "Slow", null, 0.5f, mapping);
        DimletKey keyTimeNormal = new DimletKey(DimletType.DIMLET_TIME, "Normal");
        addExtraInformation(keyTimeNormal, "With this normal dimlet you will get", "default day/night timing");

        initBiomeItems(cfg, mapping);
        initLiquidItems(cfg, mapping);

        craftableDimlets.add(new DimletKey(DimletType.DIMLET_WEATHER, "Default"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_EFFECT, "None"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_FEATURE, "None"));
        craftableDimlets.add(keyStructureNone);
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_TERRAIN, "Void"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_TERRAIN, "Void"));
        craftableDimlets.add(keyControllerDefault);
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_CONTROLLER, "Single"));
        craftableDimlets.add(keyMaterialNone);
        craftableDimlets.add(keyLiquidNone);
        craftableDimlets.add(keySkyNormal);
        craftableDimlets.add(keySkyNormalDay);
        craftableDimlets.add(keySkyNormalNight);
        craftableDimlets.add(keyDefaultMobs);
        craftableDimlets.add(keyTimeNormal);
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "0"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "1"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "2"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "3"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "4"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "5"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "6"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "7"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "8"));
        craftableDimlets.add(new DimletKey(DimletType.DIMLET_DIGIT, "9"));

        readUserDimlets(cfg, modConfigDir, mapping);

        DimletRandomizer.setupWeightedRandomList();
        setupChestLoot();

        mapping.save(world);
    }

    private static void initMaterialDimlets(Configuration cfg, DimletMapping mapping) {
        initMaterialItem(cfg, Blocks.diamond_block, 0, mapping);
        initMaterialItem(cfg, Blocks.diamond_ore, 0, mapping);
        initMaterialItem(cfg, Blocks.emerald_block, 0, mapping);
        initMaterialItem(cfg, Blocks.emerald_ore, 0, mapping);
        initMaterialItem(cfg, Blocks.quartz_block, 0, mapping);
        initMaterialItem(cfg, Blocks.quartz_ore, 0, mapping);
        initMaterialItem(cfg, Blocks.gold_block, 0, mapping);
        initMaterialItem(cfg, Blocks.gold_ore, 0, mapping);
        initMaterialItem(cfg, Blocks.iron_block, 0, mapping);
        initMaterialItem(cfg, Blocks.iron_ore, 0, mapping);
        initMaterialItem(cfg, Blocks.coal_ore, 0, mapping);
        initMaterialItem(cfg, Blocks.lapis_block, 0, mapping);
        initMaterialItem(cfg, Blocks.lapis_ore, 0, mapping);
        initMaterialItem(cfg, Blocks.coal_block, 0, mapping);
        initMaterialItem(cfg, Blocks.redstone_block, 0, mapping);
        initMaterialItem(cfg, Blocks.redstone_ore, 0, mapping);
        initMaterialItem(cfg, Blocks.dirt, 0, mapping);
        initMaterialItem(cfg, Blocks.sandstone, 0, mapping);
        initMaterialItem(cfg, Blocks.end_stone, 0, mapping);
        initMaterialItem(cfg, Blocks.netherrack, 0, mapping);
        initMaterialItem(cfg, Blocks.cobblestone, 0, mapping);
        initMaterialItem(cfg, Blocks.obsidian, 0, mapping);
        initMaterialItem(cfg, Blocks.soul_sand, 0, mapping);
        initMaterialItem(cfg, Blocks.glass, 0, mapping);
        for (int i = 0 ; i < 16 ; i++) {
            initMaterialItem(cfg, Blocks.stained_glass, i, mapping);
            initMaterialItem(cfg, Blocks.stained_hardened_clay, i, mapping);
        }
        initMaterialItem(cfg, Blocks.glowstone, 0, mapping);
        initMaterialItem(cfg, Blocks.mossy_cobblestone, 0, mapping);
        initMaterialItem(cfg, Blocks.ice, 0, mapping);
        initMaterialItem(cfg, Blocks.packed_ice, 0, mapping);
        initMaterialItem(cfg, Blocks.clay, 0, mapping);
        initMaterialItem(cfg, Blocks.hardened_clay, 0, mapping);
        initMaterialItem(cfg, ModBlocks.dimensionalShardBlock, 0, mapping);

        initOreDictionaryDimlets(cfg, mapping);

        initModMaterialItem(cfg, "chisel", "marble", 0, mapping);
        initModMaterialItem(cfg, "chisel", "limestone", 0, mapping);
    }

    private static void initOreDictionaryDimlets(Configuration cfg, DimletMapping mapping) {
        for (String oreName : OreDictionary.getOreNames()) {
            List<ItemStack> stacks = OreDictionary.getOres(oreName);
            if (!stacks.isEmpty() && oreName.startsWith("ore")) {
                ItemStack itemStack = null;
                for (ItemStack stack : stacks) {
                    if (stack.getTagCompound() == null) {
                        itemStack = stack;
                        break;
                    }
                }

                if (itemStack != null) {
                    Item item = itemStack.getItem();
                    if (item instanceof ItemBlock) {
                        ItemBlock itemBlock = (ItemBlock) item;
                        Block block = itemBlock.field_150939_a;
                        String unlocalizedName = block.getUnlocalizedName();
                        int meta = itemStack.getItemDamage();
                        if (meta != 0) {
                            unlocalizedName += meta;
                        }
                        DimletKey key = new DimletKey(DimletType.DIMLET_MATERIAL, unlocalizedName);
                        Integer id = mapping.getId(key);

                        if (id == null || !idToDimletEntry.containsKey(id)) {
                            initMaterialItem(cfg, block, meta, mapping);
                        }
                    }
                }
            }
        }
    }

    private static void initDigitCrafting(String from, String to, World world) {
        DimletKey keyFrom = new DimletKey(DimletType.DIMLET_DIGIT, from);
        DimletKey keyTo = new DimletKey(DimletType.DIMLET_DIGIT, to);
        GameRegistry.addRecipe(makeKnownDimlet(keyTo, world), "   ", " 9 ", "   ", '9', makeKnownDimlet(keyFrom, world));
    }

    public static void initCrafting(World world) {
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

        initDigitCrafting("0", "1", world);
        initDigitCrafting("1", "2", world);
        initDigitCrafting("2", "3", world);
        initDigitCrafting("3", "4", world);
        initDigitCrafting("4", "5", world);
        initDigitCrafting("5", "6", world);
        initDigitCrafting("6", "7", world);
        initDigitCrafting("7", "8", world);
        initDigitCrafting("8", "9", world);
        initDigitCrafting("9", "0", world);

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
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_WEATHER, "Default"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.snowball, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_DIGIT, "0"), " r ", "rtr", "ppp", 'r', Items.redstone, 't', redstoneTorch, 'p', Items.paper));
    }

    private static int initModMaterialItem(Configuration cfg, String modid, String blockname, int meta, DimletMapping mapping) {
        Block block = GameRegistry.findBlock(modid, blockname);
        if (block != null) {
            return initMaterialItem(cfg, block, meta, mapping);
        } else {
            return -1;
        }
    }

    /**
     * Make sure lastId is set to a value beyond all current allocated ids.
     */
    private static void updateLastId(Configuration cfg, DimletMapping mapping) {
        lastId = 0;
        if (cfg != null) {
            ConfigCategory category = cfg.getCategory(CATEGORY_KNOWNDIMLETS);
            for (Map.Entry<String, Property> entry : category.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("dimlet.")) {
                    Integer id = entry.getValue().getInt();
                    if (id != -1) {
                        if (id > lastId) {
                            lastId = id;
                        }
                    }
                }
            }
        }

        for (Integer id : mapping.getIds()) {
            if (id > lastId) {
                lastId = id;
            }
        }
    }

    private static int initDigitItem(Configuration cfg, int digit, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_DIGIT, "" + digit);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            idToDisplayName.put(key, DimletType.DIMLET_DIGIT.getName() + " " + digit + " Dimlet");
            DimletObjectMapping.idToDigit.put(key, String.valueOf(digit));
        }
        return id;
    }

    private static int initMaterialItem(Configuration cfg, Block block, int meta, DimletMapping mapping) {
        String unlocalizedName = block.getUnlocalizedName();
        if (meta != 0) {
            unlocalizedName += meta;
        }
        DimletKey key = new DimletKey(DimletType.DIMLET_MATERIAL, unlocalizedName);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            ItemStack stack = new ItemStack(block, 1, meta);
            idToDisplayName.put(key, DimletType.DIMLET_MATERIAL.getName() + " " + stack.getDisplayName() + " Dimlet");
            DimletObjectMapping.idToBlock.put(key, new BlockMeta(block, (byte)meta));
        }
        return id;
    }

    /**
     * Read user-specified dimlets.
     */
    private static void readUserDimlets(Configuration cfg, File modConfigDir, DimletMapping mapping) {
        try {
            File file = new File(modConfigDir.getPath() + File.separator + "rftools", "userdimlets.json");
            FileInputStream inputstream = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            for (JsonElement entry : element.getAsJsonArray()) {
                JsonArray array = entry.getAsJsonArray();
                String type = array.get(0).getAsString();
                if ("material".equals(type)) {
                    String modid = array.get(1).getAsString();
                    String name = array.get(2).getAsString();
                    Integer meta = array.get(3).getAsInt();
                    initModMaterialItem(cfg, modid, name, meta, mapping);
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

    private static int initControllerItem(Configuration cfg, String name, ControllerType type, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_CONTROLLER, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            DimletObjectMapping.idToControllerType.put(key, type);
            idToDisplayName.put(key, DimletType.DIMLET_CONTROLLER.getName() + " " + name + " Dimlet");
        }
        return -1;
    }

    private static void initBiomeItems(Configuration cfg, DimletMapping mapping) {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                String name = biome.biomeName;
                DimletKey key = new DimletKey(DimletType.DIMLET_BIOME, name);
                int id = registerDimlet(cfg, key, mapping);
                if (id != -1) {
                    DimletObjectMapping.idToBiome.put(key, biome);
                    idToDisplayName.put(key, DimletType.DIMLET_BIOME.getName() + " " + name + " Dimlet");
                }
            }
        }
    }

    private static void initFoliageItem(Configuration cfg, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_FOLIAGE, "Oak");
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            idToDisplayName.put(key, "Foliage Oak Dimlet");
        }
    }

    private static void initLiquidItems(Configuration cfg, DimletMapping mapping) {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            if (me.getValue().canBePlacedInWorld()) {
                try {
                    String displayName = new FluidStack(me.getValue(), 1).getLocalizedName();
                    DimletKey key = new DimletKey(DimletType.DIMLET_LIQUID, me.getKey());
                    int id = registerDimlet(cfg, key, mapping);
                    if (id != -1) {
                        DimletObjectMapping.idToFluid.put(key, me.getValue().getBlock());
                        idToDisplayName.put(key, DimletType.DIMLET_LIQUID.getName() + " " + displayName + " Dimlet");
                    }
                } catch (Exception e) {
                    RFTools.logError("Something went wrong getting the name of a fluid:");
                    RFTools.logError("Fluid: " + me.getKey() + ", unlocalizedName: " + me.getValue().getUnlocalizedName());
                }
            }
        }
    }

    private static int initSpecialItem(Configuration cfg, String name, SpecialType specialType, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_SPECIAL, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            idToDisplayName.put(key, DimletType.DIMLET_SPECIAL.getName() + " " + name + " Dimlet");
            DimletObjectMapping.idToSpecialType.put(key, specialType);
        }
        return id;
    }

    private static int initMobItem(Configuration cfg, String name,
                                   DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_MOBS, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            idToDisplayName.put(key, DimletType.DIMLET_MOBS.getName() + " " + name + " Dimlet");
            DimletObjectMapping.idtoMob.put(key, MobConfiguration.mobClasses.get(name));
        }
        return id;
    }

    private static int initSkyItem(Configuration cfg, String name, SkyDescriptor skyDescriptor, boolean isbody, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_SKY, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            DimletObjectMapping.idToSkyDescriptor.put(key, skyDescriptor);
            idToDisplayName.put(key, DimletType.DIMLET_SKY.getName() + " " + name + " Dimlet");
            if (isbody) {
                DimletObjectMapping.celestialBodies.add(key);
            }
        }
        return id;
    }

    private static int initWeatherItem(Configuration cfg, String name, WeatherDescriptor weatherDescriptor, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_WEATHER, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            DimletObjectMapping.idToWeatherDescriptor.put(key, weatherDescriptor);
            idToDisplayName.put(key, DimletType.DIMLET_WEATHER.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initStructureItem(Configuration cfg, String name, StructureType structureType, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_STRUCTURE, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            DimletObjectMapping.idToStructureType.put(key, structureType);
            idToDisplayName.put(key, DimletType.DIMLET_STRUCTURE.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTerrainItem(Configuration cfg, String name, TerrainType terrainType, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_TERRAIN, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            DimletObjectMapping.idToTerrainType.put(key, terrainType);
            idToDisplayName.put(key, DimletType.DIMLET_TERRAIN.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initEffectItem(Configuration cfg, String name, EffectType effectType, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_EFFECT, "" + name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            idToDisplayName.put(key, DimletType.DIMLET_EFFECT.getName() + " " + name + " Dimlet");
            DimletObjectMapping.idToEffectType.put(key, effectType);
        }
        return id;
    }

    private static int initFeatureItem(Configuration cfg, String name, FeatureType featureType, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_FEATURE, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            DimletObjectMapping.idToFeatureType.put(key, featureType);
            idToDisplayName.put(key, DimletType.DIMLET_FEATURE.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTimeItem(Configuration cfg, String name, Float angle, Float speed, DimletMapping mapping) {
        DimletKey key = new DimletKey(DimletType.DIMLET_TIME, name);
        int id = registerDimlet(cfg, key, mapping);
        if (id != -1) {
            DimletObjectMapping.idToCelestialAngle.put(key, angle);
            DimletObjectMapping.idToSpeed.put(key, speed);
            idToDisplayName.put(key, DimletType.DIMLET_TIME.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    public static DimletEntry getEntry(DimletKey key) {
        return idToDimletEntry.get(key);
    }

    /**
     * Return true if this dimlet is represented in the new way (NBT data).
     */
    public static boolean isNewKnownDimlet(ItemStack dimletStack) {
        NBTTagCompound tagCompound = dimletStack.getTagCompound();
        return tagCompound != null && tagCompound.hasKey("dkey");
    }

    /**
     * Get the unique dimletkey out of a known dimlet.
     */
    public static DimletKey getDimletKey(ItemStack dimletStack, World world) {
        NBTTagCompound tagCompound = dimletStack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("dkey")) {
            DimletType type = DimletType.getTypeByOpcode(tagCompound.getString("ktype"));
            return new DimletKey(type, tagCompound.getString("dkey"));
        } else {
            int damage = dimletStack.getItemDamage();
            DimletMapping mapping = world == null ? DimletMapping.getInstance() : DimletMapping.getDimletMapping(world);
            return mapping.getKey(damage);
        }
    }

    /**
     * Set the dimlet key on a known dimlet item.
     */
    public static void setDimletKey(DimletKey key, ItemStack itemStack) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setString("ktype", key.getType().getOpcode());
        tagCompound.setString("dkey", key.getName());
        itemStack.setTagCompound(tagCompound);

        DimletMapping mapping = DimletMapping.getInstance();
        itemStack.setItemDamage(mapping.getId(key));
    }

    /**
     * Make a known dimlet itemstack.
     */
    public static ItemStack makeKnownDimlet(DimletKey key, World world) {
        DimletMapping mapping;
        if (world == null) {
            mapping = DimletMapping.getInstance();
        } else {
            mapping = DimletMapping.getDimletMapping(world);
        }
        int id = mapping.getId(key);
        ItemStack itemStack = new ItemStack(ModItems.knownDimlet, 1, id);
        setDimletKey(key, itemStack);
        return itemStack;
    }
}

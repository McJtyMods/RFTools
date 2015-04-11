package mcjty.rftools.items.dimlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.rftools.CommonProxy;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.crafting.KnownDimletShapedRecipe;
import mcjty.rftools.dimension.description.MobDescriptor;
import mcjty.rftools.dimension.description.SkyDescriptor;
import mcjty.rftools.dimension.description.WeatherDescriptor;
import mcjty.rftools.dimension.world.types.*;
import mcjty.rftools.items.ModItems;
import mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.*;

public class KnownDimletConfiguration {
    public static final String CATEGORY_KNOWNDIMLETS = "knowndimlets";              // This is part of dimlets.cfg
    public static final String CATEGORY_DIMLETSETTINGS = "dimletsettings";          // This is part of dimlets.cfg
    public static final String CATEGORY_RARITY = "rarity";
    public static final String CATEGORY_MOBSPAWNS = "mobspawns";
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_RECURRENTCOMPLEX = "recurrentcomplex";

    // Map the id of a dimlet to a display name.
    public static final Map<DimletKey,String> idToDisplayName = new HashMap<DimletKey, String>();
    public static final Map<DimletKey,DimletEntry> idToDimletEntry = new HashMap<DimletKey, DimletEntry>();

    // Map the id of a dimlet to extra information for the tooltip.
    public static final Map<DimletKey,List<String>> idToExtraInformation = new HashMap<DimletKey, List<String>>();

    // All craftable dimlets.
    public static final Set<DimletKey> craftableDimlets = new HashSet<DimletKey>();

    private static final Set<DimletKey> dimletBlackList = new HashSet<DimletKey>(); // Note, the keys here can contain wildcards
    private static final Set<DimletKey> dimletRandomNotAllowed = new HashSet<DimletKey>();

    // A set of banned mods for a givem dimlet type.
    private static final Set<Pair<DimletType,String>> bannedMods = new HashSet<Pair<DimletType, String>>();

    // All blacklisted keys.
    private static final Set<DimletKey> blacklistedKeys = new HashSet<DimletKey>();

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

        for (DimletType type : DimletType.values()) {
            type.dimletType.setupFromConfig(cfg);
        }
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

    private static void readUserBlacklist(Configuration cfg) {
        ConfigCategory category = cfg.getCategory(CATEGORY_KNOWNDIMLETS);
        for (Map.Entry<String, Property> entry : category.entrySet()) {
            String name = entry.getKey();
            if (name.startsWith("modban.")) {
                String[] lst = StringUtils.split(name, ".");
                if (lst.length == 3) {
                    String type = lst[1];
                    String modid = lst[2];
                    DimletType dt = DimletType.getTypeByName(type);
                    if (dt == null) {
                        RFTools.log("Bad dimlet type in configuration for 'modban': " + name);
                        continue;
                    }
                    bannedMods.add(Pair.of(dt, modid));
                    RFTools.log("Banned dimlet type " + dt.dimletType.getName() + " for mod '" + modid + "'");
                } else {
                    RFTools.log("Bad format in configuration for 'modban': " + name);
                }
            }
        }

    }

    private static boolean isBlacklistedMod(DimletType type, String modid) {
        Pair<DimletType, String> pair = Pair.of(type, modid);
        return bannedMods.contains(pair);
    }

    public static boolean isBlacklisted(DimletKey key) {
        return blacklistedKeys.contains(key);
    }


    private static int registerDimlet(Configuration cfg, DimletKey key, DimletMapping mapping, boolean master, String modid) {
        String k = "dimlet." + key.getType().dimletType.getName() + "." + key.getName();

        Integer id = mapping.getId(key);

        // Check blacklist but not if we are on a client connecting to a server.
        if (master && isBlacklistedKey(key)) {
            id = -1;
        } else if (master && modid != null && isBlacklistedMod(key.getType(), modid)) {
            id = -1;
        } else if (id == null) {
            // We don't have this key in world data.
            if (!master) {
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
            if (master && cfg.hasKey(CATEGORY_KNOWNDIMLETS, k)) {
                int blackid = cfg.get(CATEGORY_KNOWNDIMLETS, k, -1).getInt();
                if (blackid == -1) {
                    id = -1;
                }
            }
        }

        if (id == -1) {
            // Remove from mapping if it is there.
            mapping.removeKey(key);
            RFTools.log("Blacklisted dimlet " + key.getType().dimletType.getName() + ", " + key.getName());
            blacklistedKeys.add(key);
            return id;
        }

        int rfCreateCost = checkCostConfig(cfg, "rfcreate.", key, DimletCosts.dimletBuiltinRfCreate, key.getType().dimletType.getCreationCost());
        int rfMaintainCost = checkCostConfig(cfg, "rfmaintain.", key, DimletCosts.dimletBuiltinRfMaintain, key.getType().dimletType.getMaintenanceCost());
        int tickCost = checkCostConfig(cfg, "ticks.", key, DimletCosts.dimletBuiltinTickCost, key.getType().dimletType.getTickCost());
        int rarity = checkCostConfig(cfg, "rarity.", key, DimletRandomizer.dimletBuiltinRarity, key.getType().dimletType.getRarity());
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
        k = prefix + key.getType().dimletType.getName() + "." + key.getName();
        boolean defaultValue = builtinDefaults.contains(key);
        if (cfg.getCategory(CATEGORY_DIMLETSETTINGS).containsKey(k)) {
            return cfg.get(CATEGORY_DIMLETSETTINGS, k, defaultValue).getBoolean();
        } else {
            return defaultValue;
        }
    }

    private static int checkCostConfig(Configuration cfg, String prefix, DimletKey key, Map<DimletKey,Integer> builtinDefaults, int defCost) {
        String k;
        k = prefix + key.getType().dimletType.getName() + "." + key.getName();
        Integer defaultValue = builtinDefaults.get(key);
        if (defaultValue == null) {
            defaultValue = defCost;
        }
        int cost;
        if (defaultValue.equals(defCost) && !cfg.getCategory(CATEGORY_DIMLETSETTINGS).containsKey(k)) {
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

    // If master is true then we are the master and need to check the config file for blacklisting and such.
    public static void init(World world, boolean master) {
        File modConfigDir = CommonProxy.modConfigDir;
        Configuration cfg = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "dimlets.cfg"));
        cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_KNOWNDIMLETS, "Dimlet configuration");
        cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_DIMLETSETTINGS, "Settings for specific dimlets");

        cfg.load();
        init(world, cfg, master);
        if (cfg.hasChanged()) {
            cfg.save();
        }
    }

    /**
     * This initializes all dimlets based on all loaded mods.
     */
    private static void init(World world, Configuration cfg, boolean master) {
        clean();

        DimletMapping mapping = DimletMapping.getDimletMapping(world);

        File modConfigDir = CommonProxy.modConfigDir;

        readDimletsJson();
        if (master) {
            readUserBlacklist(cfg);
        }

        updateLastId(cfg, mapping, master);

        initControllerItem(cfg, "Default", ControllerType.CONTROLLER_DEFAULT, mapping, master);
        initControllerItem(cfg, "Single", ControllerType.CONTROLLER_SINGLE, mapping, master);
        initControllerItem(cfg, "Checkerboard", ControllerType.CONTROLLER_CHECKERBOARD, mapping, master);
        initControllerItem(cfg, "Cold", ControllerType.CONTROLLER_COLD, mapping, master);
        initControllerItem(cfg, "Medium", ControllerType.CONTROLLER_MEDIUM, mapping, master);
        initControllerItem(cfg, "Warm", ControllerType.CONTROLLER_WARM, mapping, master);
        initControllerItem(cfg, "Dry", ControllerType.CONTROLLER_DRY, mapping, master);
        initControllerItem(cfg, "Wet", ControllerType.CONTROLLER_WET, mapping, master);
        initControllerItem(cfg, "Fields", ControllerType.CONTROLLER_FIELDS, mapping, master);
        initControllerItem(cfg, "Mountains", ControllerType.CONTROLLER_MOUNTAINS, mapping, master);
        initControllerItem(cfg, "Filtered", ControllerType.CONTROLLER_FILTERED, mapping, master);
        initControllerItem(cfg, "Magical", ControllerType.CONTROLLER_MAGICAL, mapping, master);
        initControllerItem(cfg, "Forest", ControllerType.CONTROLLER_FOREST, mapping, master);
        BiomeControllerMapping.setupControllerBiomes();
        DimletKey keyControllerDefault = new DimletKey(DimletType.DIMLET_CONTROLLER, "Default");
        addExtraInformation(keyControllerDefault, "The Default controller just uses the same", "biome distribution as the overworld");

        initDigitItem(cfg, 0, mapping, master);
        initDigitItem(cfg, 1, mapping, master);
        initDigitItem(cfg, 2, mapping, master);
        initDigitItem(cfg, 3, mapping, master);
        initDigitItem(cfg, 4, mapping, master);
        initDigitItem(cfg, 5, mapping, master);
        initDigitItem(cfg, 6, mapping, master);
        initDigitItem(cfg, 7, mapping, master);
        initDigitItem(cfg, 8, mapping, master);
        initDigitItem(cfg, 9, mapping, master);

        DimletKey keyMaterialNone = new DimletKey(DimletType.DIMLET_MATERIAL, "None");
        registerDimlet(cfg, keyMaterialNone, mapping, master, null);
        idToDisplayName.put(keyMaterialNone, DimletType.DIMLET_MATERIAL.dimletType.getName() + " None Dimlet");
        DimletObjectMapping.idToBlock.put(keyMaterialNone, null);
        addExtraInformation(keyMaterialNone, "Use this material none dimlet to get normal", "biome specific stone generation");

        initMaterialDimlets(cfg, mapping, master);

        initFoliageItem(cfg, mapping, master);

        DimletKey keyLiquidNone = new DimletKey(DimletType.DIMLET_LIQUID, "None");
        registerDimlet(cfg, keyLiquidNone, mapping, master, null);
        DimletObjectMapping.idToFluid.put(keyLiquidNone, null);
        idToDisplayName.put(keyLiquidNone, DimletType.DIMLET_LIQUID.dimletType.getName() + " None Dimlet");
        addExtraInformation(keyLiquidNone, "Use this liquid none dimlet to get normal", "water generation");

        initSpecialItem(cfg, "Peaceful", SpecialType.SPECIAL_PEACEFUL, mapping, master);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Peaceful"), "Normal mob spawning is disabled", "if you use this dimlet");
        initSpecialItem(cfg, "Efficiency", SpecialType.SPECIAL_EFFICIENCY, mapping, master);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Efficiency"), "Reduce the maintenance RF/tick of the", "generated dimension with 20%", "This is cumulative");
        initSpecialItem(cfg, "Mediocre Efficiency", SpecialType.SPECIAL_EFFICIENCY_LOW, mapping, master);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Mediocre Efficiency"), "Reduce the maintenance RF/tick of the", "generated dimension with 5%", "This is cumulative");
        initSpecialItem(cfg, "Shelter", SpecialType.SPECIAL_SHELTER, mapping, master);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Shelter"), "Generate a better sheltered spawn", "platform in the dimension");
        initSpecialItem(cfg, "Seed", SpecialType.SPECIAL_SEED, mapping, master);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Seed"), "Force a specific seed for a dimension.", "Right click in dimension to store seed.", "Shift-right click to lock seed");
        initSpecialItem(cfg, "Spawn", SpecialType.SPECIAL_SPAWN, mapping, master);
        addExtraInformation(new DimletKey(DimletType.DIMLET_SPECIAL, "Spawn"), "With this dimlet you can force", "respawning in the rftools dimension", "(unless power is low).");

        initMobItem(cfg, "Default", mapping, master);
        for (Map.Entry<String, MobDescriptor> entry : MobConfiguration.mobClasses.entrySet()) {
            Class<? extends EntityLiving> entityClass = entry.getValue().getEntityClass();
            if (entityClass != null) {
                String name = entry.getKey();
                if (name != null && !name.isEmpty()) {
                    initMobItem(cfg, name, mapping, master);
                }
            }
        }
        DimletKey keyDefaultMobs = new DimletKey(DimletType.DIMLET_MOBS, "Default");
        addExtraInformation(keyDefaultMobs, "With this default dimlet you will just get", "the default mob spawning");

        initSkyItem(cfg, "Normal", new SkyDescriptor.Builder().skyType(SkyType.SKY_NORMAL).build(), false, mapping, master);
        initSkyItem(cfg, "Normal Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Bright Day", new SkyDescriptor.Builder().sunBrightnessFactor(1.5f).build(), false, mapping, master);
        initSkyItem(cfg, "Dark Day", new SkyDescriptor.Builder().sunBrightnessFactor(0.4f).skyColorFactor(0.6f, 0.6f, 0.6f).build(), false, mapping, master);
        initSkyItem(cfg, "Normal Night", new SkyDescriptor.Builder().starBrightnessFactor(1.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Bright Night", new SkyDescriptor.Builder().starBrightnessFactor(1.5f).build(), false, mapping, master);
        initSkyItem(cfg, "Dark Night", new SkyDescriptor.Builder().starBrightnessFactor(0.4f).build(), false, mapping, master);
        initSkyItem(cfg, "Red Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 0.2f).build(), false, mapping, master);
        initSkyItem(cfg, "Dark Red Color", new SkyDescriptor.Builder().skyColorFactor(0.6f, 0.0f, 0.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Green Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 0.2f).build(), false, mapping, master);
        initSkyItem(cfg, "Dark Green Color", new SkyDescriptor.Builder().skyColorFactor(0f, 0.6f, 0f).build(), false, mapping, master);
        initSkyItem(cfg, "Blue Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 0.2f, 1.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Dark Blue Color", new SkyDescriptor.Builder().skyColorFactor(0.0f, 0.0f, 0.6f).build(), false, mapping, master);
        initSkyItem(cfg, "Yellow Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 1.0f, 0.2f).build(), false, mapping, master);
        initSkyItem(cfg, "Cyan Color", new SkyDescriptor.Builder().skyColorFactor(0.2f, 1.0f, 1.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Dark Cyan Color", new SkyDescriptor.Builder().skyColorFactor(0.0f, 0.6f, 0.6f).build(), false, mapping, master);
        initSkyItem(cfg, "Purple Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.2f, 1.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Dark Purple Color", new SkyDescriptor.Builder().skyColorFactor(0.6f, 0, 0.6f).build(), false, mapping, master);
        initSkyItem(cfg, "Black Color", new SkyDescriptor.Builder().skyColorFactor(0.0f, 0.0f, 0.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Gold Color", new SkyDescriptor.Builder().skyColorFactor(1.0f, 0.6f, 0.0f).build(), false, mapping, master);

        initSkyItem(cfg, "Normal Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 1.0f, 1.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Black Fog", new SkyDescriptor.Builder().fogColorFactor(0.0f, 0.0f, 0.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Red Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 0.2f, 0.2f).build(), false, mapping, master);
        initSkyItem(cfg, "Green Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 1.0f, 0.2f).build(), false, mapping, master);
        initSkyItem(cfg, "Blue Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 0.2f, 1.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Yellow Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 1.0f, 0.2f).build(), false, mapping, master);
        initSkyItem(cfg, "Cyan Fog", new SkyDescriptor.Builder().fogColorFactor(0.2f, 1.0f, 1.0f).build(), false, mapping, master);
        initSkyItem(cfg, "Purple Fog", new SkyDescriptor.Builder().fogColorFactor(1.0f, 0.2f, 1.0f).build(), false, mapping, master);

        initSkyItem(cfg, "Ender", new SkyDescriptor.Builder().skyType(SkyType.SKY_ENDER).build(), false, mapping, master);
        initSkyItem(cfg, "Inferno", new SkyDescriptor.Builder().skyType(SkyType.SKY_INFERNO).build(), false, mapping, master);

        initSkyItem(cfg, "Body None", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_NONE).build(), false, mapping, master);   // False because we don't want to select this randomly.
        initSkyItem(cfg, "Body Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SUN).build(), true, mapping, master);
        initSkyItem(cfg, "Body Large Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGESUN).build(), true, mapping, master);
        initSkyItem(cfg, "Body Small Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SMALLSUN).build(), true, mapping, master);
        initSkyItem(cfg, "Body Red Sun", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_REDSUN).build(), true, mapping, master);
        initSkyItem(cfg, "Body Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_MOON).build(), true, mapping, master);
        initSkyItem(cfg, "Body Large Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGEMOON).build(), true, mapping, master);
        initSkyItem(cfg, "Body Small Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_SMALLMOON).build(), true, mapping, master);
        initSkyItem(cfg, "Body Red Moon", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_REDMOON).build(), true, mapping, master);
        initSkyItem(cfg, "Body Planet", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_PLANET).build(), true, mapping, master);
        initSkyItem(cfg, "Body Large Planet", new SkyDescriptor.Builder().addBody(CelestialBodyType.BODY_LARGEPLANET).build(), true, mapping, master);

        DimletKey keySkyNormal = new DimletKey(DimletType.DIMLET_SKY, "Normal");
        DimletKey keySkyNormalDay = new DimletKey(DimletType.DIMLET_SKY, "Normal Day");
        DimletKey keySkyNormalNight = new DimletKey(DimletType.DIMLET_SKY, "Normal Night");
        addExtraInformation(keySkyNormal, "A normal type of sky", "(as opposed to ender or inferno)");
        addExtraInformation(keySkyNormalDay, "Normal brightness level for daytime sky");
        addExtraInformation(keySkyNormalNight, "Normal brightness level for nighttime sky");

        initWeatherItem(cfg, "Default", new WeatherDescriptor.Builder().build(), mapping, master);
        initWeatherItem(cfg, "No Rain", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_NORAIN).build(), mapping, master);
        initWeatherItem(cfg, "Light Rain", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_LIGHTRAIN).build(), mapping, master);
        initWeatherItem(cfg, "Hard Rain", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_HARDRAIN).build(), mapping, master);
        initWeatherItem(cfg, "No Thunder", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_NOTHUNDER).build(), mapping, master);
        initWeatherItem(cfg, "Light Thunder", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_LIGHTTHUNDER).build(), mapping, master);
        initWeatherItem(cfg, "Hard Thunder", new WeatherDescriptor.Builder().weatherType(WeatherType.WEATHER_HARDTHUNDER).build(), mapping, master);

        addExtraInformation(new DimletKey(DimletType.DIMLET_WEATHER, "Default"), "Normal default weather");

        initStructureItem(cfg, "None", StructureType.STRUCTURE_NONE, mapping, master);
        initStructureItem(cfg, "Village", StructureType.STRUCTURE_VILLAGE, mapping, master);
        initStructureItem(cfg, "Stronghold", StructureType.STRUCTURE_STRONGHOLD, mapping, master);
        initStructureItem(cfg, "Dungeon", StructureType.STRUCTURE_DUNGEON, mapping, master);
        initStructureItem(cfg, "Fortress", StructureType.STRUCTURE_FORTRESS, mapping, master);
        initStructureItem(cfg, "Mineshaft", StructureType.STRUCTURE_MINESHAFT, mapping, master);
        initStructureItem(cfg, "Scattered", StructureType.STRUCTURE_SCATTERED, mapping, master);
        DimletKey keyStructureNone = new DimletKey(DimletType.DIMLET_STRUCTURE, "None");
        addExtraInformation(keyStructureNone, "With this none dimlet you can disable", "all normal structure spawning");
        initRecurrentComplexStructures(cfg, mapping, master);

        initTerrainItem(cfg, "Void", TerrainType.TERRAIN_VOID, mapping, master);
        initTerrainItem(cfg, "Flat", TerrainType.TERRAIN_FLAT, mapping, master);
        initTerrainItem(cfg, "Amplified", TerrainType.TERRAIN_AMPLIFIED, mapping, master);
        initTerrainItem(cfg, "Normal", TerrainType.TERRAIN_NORMAL, mapping, master);
        initTerrainItem(cfg, "Cavern", TerrainType.TERRAIN_CAVERN, mapping, master);
        initTerrainItem(cfg, "Island", TerrainType.TERRAIN_ISLAND, mapping, master);
        initTerrainItem(cfg, "Islands", TerrainType.TERRAIN_ISLANDS, mapping, master);
        initTerrainItem(cfg, "Chaotic", TerrainType.TERRAIN_CHAOTIC, mapping, master);
        initTerrainItem(cfg, "Plateaus", TerrainType.TERRAIN_PLATEAUS, mapping, master);
        initTerrainItem(cfg, "Grid", TerrainType.TERRAIN_GRID, mapping, master);
        initTerrainItem(cfg, "Low Cavern", TerrainType.TERRAIN_LOW_CAVERN, mapping, master);
        initTerrainItem(cfg, "Flooded Cavern", TerrainType.TERRAIN_FLOODED_CAVERN, mapping, master);
        initTerrainItem(cfg, "Nearlands", TerrainType.TERRAIN_NEARLANDS, mapping, master);

        initFeatureItem(cfg, "None", FeatureType.FEATURE_NONE, mapping, master);
        initFeatureItem(cfg, "Caves", FeatureType.FEATURE_CAVES, mapping, master);
        initFeatureItem(cfg, "Ravines", FeatureType.FEATURE_RAVINES, mapping, master);
        initFeatureItem(cfg, "Orbs", FeatureType.FEATURE_ORBS, mapping, master);
        initFeatureItem(cfg, "Oregen", FeatureType.FEATURE_OREGEN, mapping, master);
        initFeatureItem(cfg, "Lakes", FeatureType.FEATURE_LAKES, mapping, master);
        initFeatureItem(cfg, "Tendrils", FeatureType.FEATURE_TENDRILS, mapping, master);
        initFeatureItem(cfg, "Canyons", FeatureType.FEATURE_CANYONS, mapping, master);
        initFeatureItem(cfg, "Maze", FeatureType.FEATURE_MAZE, mapping, master);
        initFeatureItem(cfg, "Liquid Orbs", FeatureType.FEATURE_LIQUIDORBS, mapping, master);
        initFeatureItem(cfg, "Shallow Ocean", FeatureType.FEATURE_SHALLOW_OCEAN, mapping, master);
        initFeatureItem(cfg, "Volcanoes", FeatureType.FEATURE_VOLCANOES, mapping, master);
        initFeatureItem(cfg, "Huge Orbs", FeatureType.FEATURE_HUGEORBS, mapping, master);
        initFeatureItem(cfg, "Huge Liquid Orbs", FeatureType.FEATURE_HUGELIQUIDORBS, mapping, master);
//        initFeatureItem(cfg, "Dense Caves", FeatureType.FEATURE_DENSE_CAVES, mapping);
        addExtraInformation(new DimletKey(DimletType.DIMLET_FEATURE, "None"), "With this none dimlet you can disable", "all special features");

        initEffectItem(cfg, "None", EffectType.EFFECT_NONE, mapping, master);
        initEffectItem(cfg, "Poison", EffectType.EFFECT_POISON, mapping, master);
        initEffectItem(cfg, "Poison II", EffectType.EFFECT_POISON2, mapping, master);
        initEffectItem(cfg, "Poison III", EffectType.EFFECT_POISON3, mapping, master);
//        initEffectItem(cfg, idsInConfig, "No Gravity", EffectType.EFFECT_NOGRAVITY);
        initEffectItem(cfg, "Regeneration", EffectType.EFFECT_REGENERATION, mapping, master);
        initEffectItem(cfg, "Regeneration II", EffectType.EFFECT_REGENERATION2, mapping, master);
        initEffectItem(cfg, "Regeneration III", EffectType.EFFECT_REGENERATION3, mapping, master);
        initEffectItem(cfg, "Slowness", EffectType.EFFECT_MOVESLOWDOWN, mapping, master);
        initEffectItem(cfg, "Slowness II", EffectType.EFFECT_MOVESLOWDOWN2, mapping, master);
        initEffectItem(cfg, "Slowness III", EffectType.EFFECT_MOVESLOWDOWN3, mapping, master);
        initEffectItem(cfg, "Slowness IV", EffectType.EFFECT_MOVESLOWDOWN4, mapping, master);
        initEffectItem(cfg, "Speed", EffectType.EFFECT_MOVESPEED, mapping, master);
        initEffectItem(cfg, "Speed II", EffectType.EFFECT_MOVESPEED2, mapping, master);
        initEffectItem(cfg, "Speed III", EffectType.EFFECT_MOVESPEED3, mapping, master);
        initEffectItem(cfg, "Mining Fatigue", EffectType.EFFECT_DIGSLOWDOWN, mapping, master);
        initEffectItem(cfg, "Mining Fatigue II", EffectType.EFFECT_DIGSLOWDOWN2, mapping, master);
        initEffectItem(cfg, "Mining Fatigue III", EffectType.EFFECT_DIGSLOWDOWN3, mapping, master);
        initEffectItem(cfg, "Mining Fatigue IV", EffectType.EFFECT_DIGSLOWDOWN4, mapping, master);
        initEffectItem(cfg, "Haste", EffectType.EFFECT_DIGSPEED, mapping, master);
        initEffectItem(cfg, "Haste II", EffectType.EFFECT_DIGSPEED2, mapping, master);
        initEffectItem(cfg, "Haste III", EffectType.EFFECT_DIGSPEED3, mapping, master);
        initEffectItem(cfg, "Damage Boost", EffectType.EFFECT_DAMAGEBOOST, mapping, master);
        initEffectItem(cfg, "Damage Boost II", EffectType.EFFECT_DAMAGEBOOST2, mapping, master);
        initEffectItem(cfg, "Damage Boost III", EffectType.EFFECT_DAMAGEBOOST3, mapping, master);
        initEffectItem(cfg, "Instant Health", EffectType.EFFECT_INSTANTHEALTH, mapping, master);
        initEffectItem(cfg, "Harm", EffectType.EFFECT_HARM, mapping, master);
        initEffectItem(cfg, "Jump", EffectType.EFFECT_JUMP, mapping, master);
        initEffectItem(cfg, "Jump II", EffectType.EFFECT_JUMP2, mapping, master);
        initEffectItem(cfg, "Jump III", EffectType.EFFECT_JUMP3, mapping, master);
        initEffectItem(cfg, "Confusion", EffectType.EFFECT_CONFUSION, mapping, master);
        initEffectItem(cfg, "Resistance", EffectType.EFFECT_RESISTANCE, mapping, master);
        initEffectItem(cfg, "Resistance II", EffectType.EFFECT_RESISTANCE2, mapping, master);
        initEffectItem(cfg, "Resistance III", EffectType.EFFECT_RESISTANCE3, mapping, master);
        initEffectItem(cfg, "Fire Resistance", EffectType.EFFECT_FIRERESISTANCE, mapping, master);
        initEffectItem(cfg, "Water Breathing", EffectType.EFFECT_WATERBREATHING, mapping, master);
        initEffectItem(cfg, "Invisibility", EffectType.EFFECT_INVISIBILITY, mapping, master);
        initEffectItem(cfg, "Blindness", EffectType.EFFECT_BLINDNESS, mapping, master);
        initEffectItem(cfg, "Nightvision", EffectType.EFFECT_NIGHTVISION, mapping, master);
        initEffectItem(cfg, "Hunger", EffectType.EFFECT_HUNGER, mapping, master);
        initEffectItem(cfg, "Hunger II", EffectType.EFFECT_HUNGER2, mapping, master);
        initEffectItem(cfg, "Hunger III", EffectType.EFFECT_HUNGER3, mapping, master);
        initEffectItem(cfg, "Weakness", EffectType.EFFECT_WEAKNESS, mapping, master);
        initEffectItem(cfg, "Weakness II", EffectType.EFFECT_WEAKNESS2, mapping, master);
        initEffectItem(cfg, "Weakness III", EffectType.EFFECT_WEAKNESS3, mapping, master);
        initEffectItem(cfg, "Wither", EffectType.EFFECT_WITHER, mapping, master);
        initEffectItem(cfg, "Wither II", EffectType.EFFECT_WITHER2, mapping, master);
        initEffectItem(cfg, "Wither III", EffectType.EFFECT_WITHER3, mapping, master);
        initEffectItem(cfg, "Health Boost", EffectType.EFFECT_HEALTHBOOST, mapping, master);
        initEffectItem(cfg, "Health Boost II", EffectType.EFFECT_HEALTHBOOST2, mapping, master);
        initEffectItem(cfg, "Health Boost III", EffectType.EFFECT_HEALTHBOOST3, mapping, master);
        initEffectItem(cfg, "Absorption", EffectType.EFFECT_ABSORPTION, mapping, master);
        initEffectItem(cfg, "Absorption II", EffectType.EFFECT_ABSORPTION2, mapping, master);
        initEffectItem(cfg, "Absorption III", EffectType.EFFECT_ABSORPTION3, mapping, master);
        initEffectItem(cfg, "Saturation", EffectType.EFFECT_SATURATION, mapping, master);
        initEffectItem(cfg, "Saturation II", EffectType.EFFECT_SATURATION2, mapping, master);
        initEffectItem(cfg, "Saturation III", EffectType.EFFECT_SATURATION3, mapping, master);
        addExtraInformation(new DimletKey(DimletType.DIMLET_EFFECT, "None"), "With this none dimlet you can disable", "all special effects");

        initTimeItem(cfg, "Normal", null, null, mapping, master);
        initTimeItem(cfg, "Noon", 0.0f, null, mapping, master);
        initTimeItem(cfg, "Midnight", 0.5f, null, mapping, master);
        initTimeItem(cfg, "Morning", 0.75f, null, mapping, master);
        initTimeItem(cfg, "Evening", 0.2f, null, mapping, master);
        initTimeItem(cfg, "Fast", null, 2.0f, mapping, master);
        initTimeItem(cfg, "Slow", null, 0.5f, mapping, master);
        DimletKey keyTimeNormal = new DimletKey(DimletType.DIMLET_TIME, "Normal");
        addExtraInformation(keyTimeNormal, "With this normal dimlet you will get", "default day/night timing");

        initBiomeItems(cfg, mapping, master);
        initLiquidItems(cfg, mapping, master);

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

        readUserDimlets(cfg, modConfigDir, mapping, master);

        DimletRandomizer.setupWeightedRandomList();
        setupChestLoot();

        mapping.save(world);
    }

    private static void initMaterialDimlets(Configuration cfg, DimletMapping mapping, boolean master) {
        initMaterialItem(cfg, Blocks.diamond_block, 0, mapping, master);
        initMaterialItem(cfg, Blocks.diamond_ore, 0, mapping, master);
        initMaterialItem(cfg, Blocks.emerald_block, 0, mapping, master);
        initMaterialItem(cfg, Blocks.emerald_ore, 0, mapping, master);
        initMaterialItem(cfg, Blocks.quartz_block, 0, mapping, master);
        initMaterialItem(cfg, Blocks.quartz_ore, 0, mapping, master);
        initMaterialItem(cfg, Blocks.gold_block, 0, mapping, master);
        initMaterialItem(cfg, Blocks.gold_ore, 0, mapping, master);
        initMaterialItem(cfg, Blocks.iron_block, 0, mapping, master);
        initMaterialItem(cfg, Blocks.iron_ore, 0, mapping, master);
        initMaterialItem(cfg, Blocks.coal_ore, 0, mapping, master);
        initMaterialItem(cfg, Blocks.lapis_block, 0, mapping, master);
        initMaterialItem(cfg, Blocks.lapis_ore, 0, mapping, master);
        initMaterialItem(cfg, Blocks.coal_block, 0, mapping, master);
        initMaterialItem(cfg, Blocks.redstone_block, 0, mapping, master);
        initMaterialItem(cfg, Blocks.redstone_ore, 0, mapping, master);
        initMaterialItem(cfg, Blocks.dirt, 0, mapping, master);
        initMaterialItem(cfg, Blocks.sandstone, 0, mapping, master);
        initMaterialItem(cfg, Blocks.end_stone, 0, mapping, master);
        initMaterialItem(cfg, Blocks.netherrack, 0, mapping, master);
        initMaterialItem(cfg, Blocks.cobblestone, 0, mapping, master);
        initMaterialItem(cfg, Blocks.obsidian, 0, mapping, master);
        initMaterialItem(cfg, Blocks.soul_sand, 0, mapping, master);
        initMaterialItem(cfg, Blocks.glass, 0, mapping, master);
        for (int i = 0 ; i < 16 ; i++) {
            initMaterialItem(cfg, Blocks.stained_glass, i, mapping, master);
            initMaterialItem(cfg, Blocks.stained_hardened_clay, i, mapping, master);
        }
        initMaterialItem(cfg, Blocks.glowstone, 0, mapping, master);
        initMaterialItem(cfg, Blocks.mossy_cobblestone, 0, mapping, master);
        initMaterialItem(cfg, Blocks.ice, 0, mapping, master);
        initMaterialItem(cfg, Blocks.packed_ice, 0, mapping, master);
        initMaterialItem(cfg, Blocks.clay, 0, mapping, master);
        initMaterialItem(cfg, Blocks.hardened_clay, 0, mapping, master);
        initMaterialItem(cfg, DimletSetup.dimensionalShardBlock, 0, mapping, master);

        initOreDictionaryDimlets(cfg, mapping, master);

        initModMaterialItem(cfg, "chisel", "marble", 0, mapping, master);
        initModMaterialItem(cfg, "chisel", "limestone", 0, mapping, master);
    }

    private static void initOreDictionaryDimlets(Configuration cfg, DimletMapping mapping, boolean master) {
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
                        if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
                            int meta = itemStack.getItemDamage();
                            if (meta != 0) {
                                unlocalizedName += meta;
                            }
                            DimletKey key = new DimletKey(DimletType.DIMLET_MATERIAL, unlocalizedName);
                            Integer id = mapping.getId(key);

                            if (id == null || !idToDimletEntry.containsKey(id)) {
                                initMaterialItem(cfg, block, meta, mapping, master);
                            }
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

    private static int initModMaterialItem(Configuration cfg, String modid, String blockname, int meta, DimletMapping mapping, boolean master) {
        Block block = GameRegistry.findBlock(modid, blockname);
        if (block != null) {
            return initMaterialItem(cfg, block, meta, mapping, master);
        } else {
            return -1;
        }
    }

    /**
     * Make sure lastId is set to a value beyond all current allocated ids.
     */
    private static void updateLastId(Configuration cfg, DimletMapping mapping, boolean master) {
        lastId = 0;
        if (master) {
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

    private static int initDigitItem(Configuration cfg, int digit, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_DIGIT, "" + digit);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            idToDisplayName.put(key, DimletType.DIMLET_DIGIT.dimletType.getName() + " " + digit + " Dimlet");
            DimletObjectMapping.idToDigit.put(key, String.valueOf(digit));
        }
        return id;
    }

    public static String getModidForBlock(Block block) {
        String nameForObject = GameData.getBlockRegistry().getNameForObject(block);
        if (nameForObject == null) {
            return "?";
        }
        String[] lst = StringUtils.split(nameForObject, ":");
        if (lst.length >= 2) {
            return lst[0];
        } else {
            return "?";
        }
    }

    private static int initMaterialItem(Configuration cfg, Block block, int meta, DimletMapping mapping, boolean master) {
        String unlocalizedName = block.getUnlocalizedName();
        if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
            if (meta != 0) {
                unlocalizedName += meta;
            }
            DimletKey key = new DimletKey(DimletType.DIMLET_MATERIAL, unlocalizedName);

            String modid = getModidForBlock(block);
            int id = registerDimlet(cfg, key, mapping, master, modid);
            if (id != -1) {
                ItemStack stack = new ItemStack(block, 1, meta);
                idToDisplayName.put(key, DimletType.DIMLET_MATERIAL.dimletType.getName() + " " + stack.getDisplayName() + " Dimlet");
                DimletObjectMapping.idToBlock.put(key, new BlockMeta(block, (byte)meta));
            }
            return id;
        } else {
            return -1;
        }
    }

    /**
     * Read user-specified dimlets.
     */
    private static void readUserDimlets(Configuration cfg, File modConfigDir, DimletMapping mapping, boolean master) {
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
                    initModMaterialItem(cfg, modid, name, meta, mapping, master);
                }
            }
        } catch (IOException e) {
            RFTools.log("Could not read 'userdimlets.json', this is not an error!");
        }
    }

    /**
     * Read the built-in blacklist and default configuration for dimlets.
     */
    private static void readDimletsJson() {
        try {
            InputStream inputstream = RFTools.class.getResourceAsStream("/assets/rftools/text/dimlets.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if ("blacklist".equals(entry.getKey())) {
                    readBlacklistFromJson(entry.getValue());
                } else if ("dimlets".equals(entry.getKey())) {
                    readDimletsFromJson(entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readBlacklistFromJson(JsonElement element) {
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

    private static void readDimletsFromJson(JsonElement element) {
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

    private static int initControllerItem(Configuration cfg, String name, ControllerType type, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_CONTROLLER, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            DimletObjectMapping.idToControllerType.put(key, type);
            idToDisplayName.put(key, DimletType.DIMLET_CONTROLLER.dimletType.getName() + " " + name + " Dimlet");
        }
        return -1;
    }

    private static void initBiomeItems(Configuration cfg, DimletMapping mapping, boolean master) {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                String name = biome.biomeName;
                if (name != null && !name.isEmpty()) {
                    DimletKey key = new DimletKey(DimletType.DIMLET_BIOME, name);
                    int id = registerDimlet(cfg, key, mapping, master, null);
                    if (id != -1) {
                        DimletObjectMapping.idToBiome.put(key, biome);
                        idToDisplayName.put(key, DimletType.DIMLET_BIOME.dimletType.getName() + " " + name + " Dimlet");
                    }
                }
            }
        }
    }

    private static void initFoliageItem(Configuration cfg, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_FOLIAGE, "Oak");
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            idToDisplayName.put(key, "Foliage Oak Dimlet");
        }
    }

    private static void initLiquidItems(Configuration cfg, DimletMapping mapping, boolean master) {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            if (me.getValue().canBePlacedInWorld()) {
                String name = me.getKey();
                if (name != null && !name.isEmpty()) {
                    try {
                        Block block = me.getValue().getBlock();
                        if (block != null) {
                            String modid = getModidForBlock(block);
                            String displayName = new FluidStack(me.getValue(), 1).getLocalizedName();
                            DimletKey key = new DimletKey(DimletType.DIMLET_LIQUID, name);
                            int id = registerDimlet(cfg, key, mapping, master, modid);
                            if (id != -1) {
                                DimletObjectMapping.idToFluid.put(key, me.getValue().getBlock());
                                idToDisplayName.put(key, DimletType.DIMLET_LIQUID.dimletType.getName() + " " + displayName + " Dimlet");
                            }
                        }
                    } catch (Exception e) {
                        RFTools.logError("Something went wrong getting the name of a fluid:");
                        RFTools.logError("Fluid: " + name + ", unlocalizedName: " + me.getValue().getUnlocalizedName());
                    }
                }
            }
        }
    }

    private static int initSpecialItem(Configuration cfg, String name, SpecialType specialType, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_SPECIAL, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            idToDisplayName.put(key, DimletType.DIMLET_SPECIAL.dimletType.getName() + " " + name + " Dimlet");
            DimletObjectMapping.idToSpecialType.put(key, specialType);
        }
        return id;
    }

    private static int initMobItem(Configuration cfg, String name,
                                   DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_MOBS, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            idToDisplayName.put(key, DimletType.DIMLET_MOBS.dimletType.getName() + " " + name + " Dimlet");
            DimletObjectMapping.idtoMob.put(key, MobConfiguration.mobClasses.get(name));
        }
        return id;
    }

    private static int initSkyItem(Configuration cfg, String name, SkyDescriptor skyDescriptor, boolean isbody, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_SKY, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            DimletObjectMapping.idToSkyDescriptor.put(key, skyDescriptor);
            idToDisplayName.put(key, DimletType.DIMLET_SKY.dimletType.getName() + " " + name + " Dimlet");
            if (isbody) {
                DimletObjectMapping.celestialBodies.add(key);
            }
        }
        return id;
    }

    private static int initWeatherItem(Configuration cfg, String name, WeatherDescriptor weatherDescriptor, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_WEATHER, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            DimletObjectMapping.idToWeatherDescriptor.put(key, weatherDescriptor);
            idToDisplayName.put(key, DimletType.DIMLET_WEATHER.dimletType.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static void initRecurrentComplexStructures(Configuration cfg, DimletMapping mapping, boolean master) {
        ConfigCategory category = cfg.getCategory(CATEGORY_RECURRENTCOMPLEX);
        for (Map.Entry<String, Property> entry : category.entrySet()) {
            if (entry.getKey().startsWith("recurrentcomplex.")) {
                String[] strings = StringUtils.split(entry.getKey(), ".");
                int id = initStructureItem(cfg, strings[1], StructureType.STRUCTURE_RECURRENTCOMPLEX, mapping, master);
                if (id != -1) {
                    DimletObjectMapping.idToRecurrentComplexType.put(new DimletKey(DimletType.DIMLET_STRUCTURE, strings[1]), entry.getValue().getString());
                }
            }
        }
    }

    private static int initStructureItem(Configuration cfg, String name, StructureType structureType, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_STRUCTURE, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            DimletObjectMapping.idToStructureType.put(key, structureType);
            idToDisplayName.put(key, DimletType.DIMLET_STRUCTURE.dimletType.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTerrainItem(Configuration cfg, String name, TerrainType terrainType, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_TERRAIN, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            DimletObjectMapping.idToTerrainType.put(key, terrainType);
            idToDisplayName.put(key, DimletType.DIMLET_TERRAIN.dimletType.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initEffectItem(Configuration cfg, String name, EffectType effectType, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_EFFECT, "" + name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            idToDisplayName.put(key, DimletType.DIMLET_EFFECT.dimletType.getName() + " " + name + " Dimlet");
            DimletObjectMapping.idToEffectType.put(key, effectType);
        }
        return id;
    }

    private static int initFeatureItem(Configuration cfg, String name, FeatureType featureType, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_FEATURE, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            DimletObjectMapping.idToFeatureType.put(key, featureType);
            idToDisplayName.put(key, DimletType.DIMLET_FEATURE.dimletType.getName() + " " + name + " Dimlet");
        }
        return id;
    }

    private static int initTimeItem(Configuration cfg, String name, Float angle, Float speed, DimletMapping mapping, boolean master) {
        DimletKey key = new DimletKey(DimletType.DIMLET_TIME, name);
        int id = registerDimlet(cfg, key, mapping, master, null);
        if (id != -1) {
            DimletObjectMapping.idToCelestialAngle.put(key, angle);
            DimletObjectMapping.idToSpeed.put(key, speed);
            idToDisplayName.put(key, DimletType.DIMLET_TIME.dimletType.getName() + " " + name + " Dimlet");
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
        tagCompound.setString("ktype", key.getType().dimletType.getOpcode());
        tagCompound.setString("dkey", key.getName());
        itemStack.setTagCompound(tagCompound);

        DimletMapping mapping = DimletMapping.getInstance();
        if (mapping != null) {
            Integer id = mapping.getId(key);
            if (id != null) {
                itemStack.setItemDamage(id);
            }
        }
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

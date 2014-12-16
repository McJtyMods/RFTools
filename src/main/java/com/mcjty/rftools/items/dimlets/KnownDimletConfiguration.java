package com.mcjty.rftools.items.dimlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.varia.WeightedRandomSelector;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
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
import org.apache.commons.lang3.tuple.Pair;

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
    public static final String CATEGORY_GENERAL = "general";

    // All dimlet ids in a weighted random selector based on rarity.
    public static WeightedRandomSelector<Integer,Integer> randomDimlets;
    public static final int RARITY_0 = 0;
    public static final int RARITY_1 = 1;
    public static final int RARITY_2 = 2;
    public static final int RARITY_3 = 3;
    public static final int RARITY_4 = 4;
    public static final int RARITY_5 = 5;
    public static WeightedRandomSelector<Integer,Integer> randomMaterialDimlets;
    public static WeightedRandomSelector<Integer,Integer> randomLiquidDimlets;

    // This map keeps track of all known dimlets by id. Also the reverse map.
    public static final Map<Integer,DimletEntry> idToDimlet = new HashMap<Integer, DimletEntry>();
    public static final Map<DimletEntry,Integer> dimletToID = new HashMap<DimletEntry, Integer>();

    // Map the id of a dimlet to a display name.
    public static final Map<Integer,String> idToDisplayName = new HashMap<Integer, String>();

    // Used for randomly generating dimlets.
    public static final List<Integer> dimletIds = new ArrayList<Integer>();

    // All craftable dimlets.
    public static final Set<Integer> craftableDimlets = new HashSet<Integer>();

    public static final Map<DimletType,Integer> typeRarity = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeRfCreateCost = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeRfMaintainCost = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeTickCost = new HashMap<DimletType, Integer>();

    // First element in the pair is the modifier type. Second element is the type that is being modified.
    public static final Map<Pair<DimletType,DimletType>,Integer> rfCreateModifierMultiplier = new HashMap<Pair<DimletType, DimletType>, Integer>();
    public static final Map<Pair<DimletType,DimletType>,Integer> rfMaintainModifierMultiplier = new HashMap<Pair<DimletType, DimletType>, Integer>();
    public static final Map<Pair<DimletType,DimletType>,Integer> tickCostModifierMultiplier = new HashMap<Pair<DimletType, DimletType>, Integer>();

    public static final Map<Integer,TerrainType> idToTerrainType = new HashMap<Integer, TerrainType>();
    public static final Map<Integer,FeatureType> idToFeatureType = new HashMap<Integer, FeatureType>();
    public static final Map<Integer,StructureType> idToStructureType = new HashMap<Integer, StructureType>();
    public static final Map<Integer,BiomeGenBase> idToBiome = new HashMap<Integer, BiomeGenBase>();
    public static final Map<Integer,String> idToDigit = new HashMap<Integer, String>();
    public static final Map<Integer,Block> idToBlock = new HashMap<Integer, Block>();
    public static final Map<Integer,Block> idToFluid = new HashMap<Integer, Block>();

    private static final Set<DimletKey> dimletBlackList = new HashSet<DimletKey>();
    private static final Map<DimletKey,Integer> dimletBuiltinRfCreate = new HashMap<DimletKey, Integer>();
    private static final Map<DimletKey,Integer> dimletBuiltinRfMaintain = new HashMap<DimletKey, Integer>();
    private static final Map<DimletKey,Integer> dimletBuiltinTickCost = new HashMap<DimletKey, Integer>();
    private static final Map<DimletKey,Integer> dimletBuiltinRarity = new HashMap<DimletKey, Integer>();

    public static int baseDimensionCreationCost = 1000;
    public static int baseDimensionMaintenanceCost = 10;
    public static int baseDimensionTickCost = 100;

    private static int lastId = 0;

    public static void initGeneralConfig(Configuration cfg) {
        baseDimensionCreationCost = cfg.get(CATEGORY_GENERAL, "baseDimensionCreationCost", baseDimensionCreationCost,
                "The base cost (in RF/tick) for creating a dimension").getInt();
        baseDimensionMaintenanceCost = cfg.get(CATEGORY_GENERAL, "baseDimensionMaintenanceCost", baseDimensionMaintenanceCost,
                "The base cost (in RF/tick) for maintaining a dimension").getInt();
        baseDimensionTickCost = cfg.get(CATEGORY_GENERAL, "baseDimensionTickCost", baseDimensionTickCost,
                "The base time (in ticks) for creating a dimension").getInt();
    }

    public static void initTypeRarity(Configuration cfg) {
        typeRarity.clear();
        initRarity(cfg, DimletType.DIMLET_BIOME, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_TIME, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_FOLIAGE, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_LIQUID, RARITY_1);
        initRarity(cfg, DimletType.DIMLET_MATERIAL, RARITY_1);
        initRarity(cfg, DimletType.DIMLET_MOBS, RARITY_2);
        initRarity(cfg, DimletType.DIMLET_SKY, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_STRUCTURE, RARITY_3);
        initRarity(cfg, DimletType.DIMLET_TERRAIN, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_FEATURE, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_DIGIT, RARITY_0);
    }

    private static void initRarity(Configuration cfg, DimletType type, int rarity) {
        typeRarity.put(type, cfg.get(CATEGORY_TYPERARIRTY, "rarity." + type.getName(), rarity).getInt());
    }

    public static void initTypeRfCreateCost(Configuration cfg) {
        typeRfCreateCost.clear();
        initTypeRfCreateCost(cfg, DimletType.DIMLET_BIOME, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_TIME, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_FOLIAGE, 200);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_LIQUID, 150);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_MATERIAL, 300);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_MOBS, 300);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_SKY, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_STRUCTURE, 600);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_TERRAIN, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_FEATURE, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_DIGIT, 0);

        rfCreateModifierMultiplier.clear();
        initRfCreateModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_TERRAIN, 10);
        initRfCreateModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_FEATURE, 1);
        initRfCreateModifierMultiplier(cfg, DimletType.DIMLET_LIQUID, DimletType.DIMLET_TERRAIN, 10);
    }

    private static void initRfCreateModifierMultiplier(Configuration cfg, DimletType type1, DimletType type2, int value) {
        rfCreateModifierMultiplier.put(Pair.of(type1, type2), cfg.get(CATEGORY_TYPERFCREATECOST, "multiplier." + type1.getName() + "." + type2.getName(), value).getInt());
    }

    private static void initTypeRfCreateCost(Configuration cfg, DimletType type, int cost) {
        typeRfCreateCost.put(type, cfg.get(CATEGORY_TYPERFCREATECOST, "rfcreate." + type.getName(), cost).getInt());
    }

    public static void initTypeRfMaintainCost(Configuration cfg) {
        typeRfMaintainCost.clear();
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_BIOME, 0);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_TIME, 10);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_FOLIAGE, 10);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_LIQUID, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_MATERIAL, 10);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_MOBS, 100);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_SKY, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_STRUCTURE, 100);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_TERRAIN, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_FEATURE, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_DIGIT, 0);

        rfMaintainModifierMultiplier.clear();
        initRfMaintainModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_TERRAIN, 20);
        initRfMaintainModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_FEATURE, 1);
        initRfMaintainModifierMultiplier(cfg, DimletType.DIMLET_LIQUID, DimletType.DIMLET_TERRAIN, 20);
    }

    private static void initRfMaintainModifierMultiplier(Configuration cfg, DimletType type1, DimletType type2, int value) {
        rfMaintainModifierMultiplier.put(Pair.of(type1, type2), cfg.get(CATEGORY_TYPERFMAINTAINCOST, "multiplier." + type1.getName() + "." + type2.getName(), value).getInt());
    }

    private static void initTypeRfMaintainCost(Configuration cfg, DimletType type, int cost) {
        typeRfMaintainCost.put(type, cfg.get(CATEGORY_TYPERFMAINTAINCOST, "rfmaintain." + type.getName(), cost).getInt());
    }

    public static void initTypeTickCost(Configuration cfg) {
        typeTickCost.clear();
        initTypeTickCost(cfg, DimletType.DIMLET_BIOME, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_TIME, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_FOLIAGE, 10);
        initTypeTickCost(cfg, DimletType.DIMLET_LIQUID, 10);
        initTypeTickCost(cfg, DimletType.DIMLET_MATERIAL, 100);
        initTypeTickCost(cfg, DimletType.DIMLET_MOBS, 200);
        initTypeTickCost(cfg, DimletType.DIMLET_SKY, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_STRUCTURE, 900);
        initTypeTickCost(cfg, DimletType.DIMLET_TERRAIN, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_FEATURE, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_DIGIT, 0);

        tickCostModifierMultiplier.clear();
        initTickCostModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_TERRAIN, 2);
        initTickCostModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_FEATURE, 1);
        initTickCostModifierMultiplier(cfg, DimletType.DIMLET_LIQUID, DimletType.DIMLET_TERRAIN, 2);
    }

    private static void initTickCostModifierMultiplier(Configuration cfg, DimletType type1, DimletType type2, int value) {
        tickCostModifierMultiplier.put(Pair.of(type1, type2), cfg.get(CATEGORY_TYPETICKCOST, "multiplier." + type1.getName() + "." + type2.getName(), value).getInt());
    }

    private static void initTypeTickCost(Configuration cfg, DimletType type, int cost) {
        typeTickCost.put(type, cfg.get(CATEGORY_TYPETICKCOST, "ticks." + type.getName(), cost).getInt());
    }

    private static void registerDimletEntry(int id, DimletEntry dimletEntry) {
        idToDimlet.put(id, dimletEntry);
        dimletToID.put(dimletEntry, id);
        dimletIds.add(id);
    }

    private static int registerDimlet(Configuration cfg, Map<DimletKey,Integer> idsInConfig, DimletKey key) {
        String k = "dimlet." + key.getType().getName() + "." + key.getName();
        int id = -1;
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

        int rfCreateCost = checkCostConfig(cfg, "rfcreate.", key, dimletBuiltinRfCreate, typeRfCreateCost);
        int rfMaintainCost = checkCostConfig(cfg, "rfmaintain.", key, dimletBuiltinRfMaintain, typeRfMaintainCost);
        int tickCost = checkCostConfig(cfg, "ticks.", key, dimletBuiltinTickCost, typeTickCost);
        int rarity = checkCostConfig(cfg, "rarity.", key, dimletBuiltinRarity, typeRarity);

        DimletEntry entry = new DimletEntry(key, rfCreateCost, rfMaintainCost, tickCost, rarity);
        registerDimletEntry(id, entry);

        return id;
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
        idToBlock.put(idMaterialNone, null);

        initMaterialItem(cfg, idsInConfig, Blocks.diamond_block);
        initMaterialItem(cfg, idsInConfig, Blocks.diamond_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.gold_block);
        initMaterialItem(cfg, idsInConfig, Blocks.gold_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.iron_block);
        initMaterialItem(cfg, idsInConfig, Blocks.iron_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.lapis_block);
        initMaterialItem(cfg, idsInConfig, Blocks.lapis_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.redstone_block);
        initMaterialItem(cfg, idsInConfig, Blocks.redstone_ore);
        initMaterialItem(cfg, idsInConfig, Blocks.dirt);
        initMaterialItem(cfg, idsInConfig, Blocks.sandstone);
        initMaterialItem(cfg, idsInConfig, Blocks.end_stone);
        initMaterialItem(cfg, idsInConfig, Blocks.netherrack);
        initMaterialItem(cfg, idsInConfig, ModBlocks.dimensionalShardBlock);

        initFoliageItem(cfg, idsInConfig);

        int idLiquidNone = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_LIQUID, "None"));
        idToFluid.put(idLiquidNone, null);
        idToDisplayName.put(idLiquidNone, DimletType.DIMLET_LIQUID.getName() + " None Dimlet");

        initLiquidItems(cfg, idsInConfig);

        initMobItem(cfg, idsInConfig, EntityZombie.class, "Zombie");
        initMobItem(cfg, idsInConfig, EntitySkeleton.class, "Skeleton");

        initSkyItem(cfg, idsInConfig, "Clear");
        initSkyItem(cfg, idsInConfig, "Bright");

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

        initTimeItem(cfg, idsInConfig, "Day");
        initTimeItem(cfg, idsInConfig, "Night");
        initTimeItem(cfg, idsInConfig, "Day/Night");

        ModItems.knownDimlet = new KnownDimlet();
        ModItems.knownDimlet.setUnlocalizedName("KnownDimlet");
        ModItems.knownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        GameRegistry.registerItem(ModItems.knownDimlet, "knownDimlet");

        GameRegistry.addRecipe(new ItemStack(ModItems.dimletTemplate), "sss", "sps", "sss", 's', ModItems.dimensionalShard, 'p', Items.paper);

        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idFeatureNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.string, 'p', Items.paper);
        craftableDimlets.add(idFeatureNone);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idStructureNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bone, 'p', Items.paper);
        craftableDimlets.add(idStructureNone);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idTerrainVoid), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', Items.paper);
        craftableDimlets.add(idTerrainVoid);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idTerrainFlat), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', ModItems.dimletTemplate);
        craftableDimlets.add(idTerrainFlat);

        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idMaterialNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Blocks.dirt, 'p', ModItems.dimletTemplate);
        craftableDimlets.add(idMaterialNone);
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idLiquidNone), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bucket, 'p', ModItems.dimletTemplate);
        craftableDimlets.add(idLiquidNone);

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

        setupWeightedRandomList(cfg);
        setupChestLoot();
    }

    private static void setupWeightedRandomList(Configuration cfg) {
        float rarity0 = (float) cfg.get(CATEGORY_RARITY, "level0", 250.0f).getDouble();
        float rarity1 = (float) cfg.get(CATEGORY_RARITY, "level1", 150.0f).getDouble();
        float rarity2 = (float) cfg.get(CATEGORY_RARITY, "level2", 90.0f).getDouble();
        float rarity3 = (float) cfg.get(CATEGORY_RARITY, "level3", 40.0f).getDouble();
        float rarity4 = (float) cfg.get(CATEGORY_RARITY, "level4", 20.0f).getDouble();
        float rarity5 = (float) cfg.get(CATEGORY_RARITY, "level5", 1.0f).getDouble();

        randomDimlets = new WeightedRandomSelector<Integer, Integer>(new Random());
        setupRarity(randomDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5);
        randomMaterialDimlets = new WeightedRandomSelector<Integer, Integer>(new Random());
        setupRarity(randomMaterialDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5);
        randomLiquidDimlets = new WeightedRandomSelector<Integer, Integer>(new Random());
        setupRarity(randomLiquidDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5);

        for (Map.Entry<Integer, DimletEntry> entry : idToDimlet.entrySet()) {
            randomDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
            if (entry.getValue().getKey().getType() == DimletType.DIMLET_MATERIAL) {
                // Don't add the 'null' material.
                if (idToBlock.get(entry.getKey()) != null) {
                    randomMaterialDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (entry.getValue().getKey().getType() == DimletType.DIMLET_LIQUID) {
                // Don't add the 'null' fluid.
                if (idToFluid.get(entry.getKey()) != null) {
                    randomLiquidDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            }
        }
    }


    public static Block getRandomFluidBlock() {
        return idToFluid.get(randomLiquidDimlets.select());
    }

    public static Block getRandomMaterialBlock() {
        return idToBlock.get(randomMaterialDimlets.select());
    }

    private static void setupRarity(WeightedRandomSelector<Integer,Integer> randomDimlets, float rarity0, float rarity1, float rarity2, float rarity3, float rarity4, float rarity5) {
        randomDimlets.addRarity(RARITY_0, rarity0);
        randomDimlets.addRarity(RARITY_1, rarity1);
        randomDimlets.addRarity(RARITY_2, rarity2);
        randomDimlets.addRarity(RARITY_3, rarity3);
        randomDimlets.addRarity(RARITY_4, rarity4);
        randomDimlets.addRarity(RARITY_5, rarity5);
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
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_DIGIT, ""+digit));
        idToDisplayName.put(id, DimletType.DIMLET_DIGIT.getName() + " " + digit + " Dimlet");
        idToDigit.put(id, ""+digit);
        return id;
    }

    private static void initMaterialItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, Block block) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_MATERIAL, block.getUnlocalizedName()));
        ItemStack stack = new ItemStack(block);
        idToDisplayName.put(id, DimletType.DIMLET_MATERIAL.getName() + " " + stack.getDisplayName() + " Dimlet");
        idToBlock.put(id, block);
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
            DimletType type = DimletType.getTypeByName(typeName);
            if (type == null) {
                RFTools.logError("Error in dimlets.json! Unknown type '" + typeName + "'!");
                return;
            }
            DimletKey key = new DimletKey(type, name);
            dimletBuiltinRfCreate.put(key, rfcreate);
            dimletBuiltinRfMaintain.put(key, rfmaintain);
            dimletBuiltinTickCost.put(key, tickCost);
            dimletBuiltinRarity.put(key, rarity);
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
                idToBiome.put(id, biome);
                idToDisplayName.put(id, DimletType.DIMLET_BIOME.getName() + " " + name + " Dimlet");
            }
        }
    }

    private static void initFoliageItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_FOLIAGE, "Oak"));
        idToDisplayName.put(id, "Foliage Oak Dimlet");
    }

    private static void initLiquidItems(Configuration cfg, Map<DimletKey,Integer> idsInConfig) {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            if (me.getValue().canBePlacedInWorld()) {
                int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_LIQUID, me.getKey()));
                String displayName = new FluidStack(me.getValue(), 1).getLocalizedName();
                idToFluid.put(id, me.getValue().getBlock());
                idToDisplayName.put(id, DimletType.DIMLET_LIQUID.getName() + " " + displayName + " Dimlet");
            }
        }
    }

    private static int initMobItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, Class <? extends EntityLiving> entity, String name) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_MOBS, name));
        idToDisplayName.put(id, DimletType.DIMLET_MOBS.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initSkyItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_SKY, name));
        idToDisplayName.put(id, DimletType.DIMLET_SKY.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initStructureItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, StructureType structureType) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_STRUCTURE, name));
        idToStructureType.put(id, structureType);
        idToDisplayName.put(id, DimletType.DIMLET_STRUCTURE.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initTerrainItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, TerrainType terrainType) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_TERRAIN, name));
        idToTerrainType.put(id, terrainType);
        idToDisplayName.put(id, DimletType.DIMLET_TERRAIN.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initFeatureItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name, FeatureType featureType) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_FEATURE, name));
        idToFeatureType.put(id, featureType);
        idToDisplayName.put(id, DimletType.DIMLET_FEATURE.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initTimeItem(Configuration cfg, Map<DimletKey,Integer> idsInConfig, String name) {
        int id = registerDimlet(cfg, idsInConfig, new DimletKey(DimletType.DIMLET_TIME, name));
        idToDisplayName.put(id, DimletType.DIMLET_TIME.getName() + " " + name + " Dimlet");
        return id;
    }

    // Get a random dimlet. A bonus of 0.01 will already give a good increase in getting rare items. 0.0 is default.
    public static int getRandomDimlet(float bonus) {
        return randomDimlets.select(randomDimlets.createDistribution(bonus));
    }

    // Get a random dimlet with no bonus.
    public static int getRandomDimlet() {
        return randomDimlets.select();
    }

    // Get a random dimlet with the given distribution.
    public static int getRandomDimlet(WeightedRandomSelector.Distribution<Integer> distribution) {
        return randomDimlets.select(distribution);
    }

    public static void dumpRarityDistribution(float bonus) {
        Map<Integer,Integer> counter = new HashMap<Integer, Integer>();
        WeightedRandomSelector.Distribution<Integer> distribution = randomDimlets.createDistribution(bonus);

        for (Integer id : dimletIds) {
            counter.put(id, 0);
        }

        final int total = 10000000;
        for (int i = 0 ; i < total ; i++) {
            int id = randomDimlets.select(distribution);
            counter.put(id, counter.get(id)+1);
        }

        RFTools.log("#### Dumping with bonus=" + bonus);
        List<Pair<Integer,Integer>> sortedCounters = new ArrayList<Pair<Integer, Integer>>();
        for (Map.Entry<Integer, Integer> entry : counter.entrySet()) {
            sortedCounters.add(Pair.of(entry.getValue(), entry.getKey()));
        }
        Collections.sort(sortedCounters);

        for (Pair<Integer, Integer> entry : sortedCounters) {
            int count = entry.getKey();
            int id = entry.getValue();
            float percentage = count * 100.0f / total;
            RFTools.log("Id:"+id + ",    key:\"" + idToDimlet.get(id).getKey().getName() + "\",    name:\""+idToDisplayName.get(id)+"\",    count:"+ count + ", "+percentage+"%");
        }
    }
}

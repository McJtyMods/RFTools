package com.mcjty.rftools.items.dimlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class KnownDimletConfiguration {
    public static final String CATEGORY_KNOWNDIMLETS = "KnownDimlets";
    public static final String CATEGORY_TYPERARIRTY = "TypeRarity";
    public static final String CATEGORY_TYPERFCREATECOST = "TypeRfCreateCost";
    public static final String CATEGORY_TYPERFMAINTAINCOST = "TypeRfMaintainCost";
    public static final String CATEGORY_TYPETICKCOST = "TypeTickCost";
    public static final String CATEGORY_GENERAL = "General";

    // This map keeps track of all known dimlets by id. Also the reverse map.
    public static final Map<Integer,DimletEntry> idToDimlet = new HashMap<Integer, DimletEntry>();
    public static final Map<DimletEntry,Integer> dimletToID = new HashMap<DimletEntry, Integer>();

    // Map the id of a dimlet to a display name.
    public static final Map<Integer,String> idToDisplayName = new HashMap<Integer, String>();

    public static final List<Integer> dimletIds = new ArrayList<Integer>();

    public static final Map<DimletType,Integer> typeRarity = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeRfCreateCost = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeRfMaintainCost = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeTickCost = new HashMap<DimletType, Integer>();

    public static final Map<Integer,TerrainType> idToTerrainType = new HashMap<Integer, TerrainType>();
    public static final Map<Integer,FeatureType> idToFeatureType = new HashMap<Integer, FeatureType>();
    public static final Map<Integer,StructureType> idToStructureType = new HashMap<Integer, StructureType>();
    public static final Map<Integer,String> idToBiome = new HashMap<Integer, String>();

    private static final ResourceLocation builtinConfigLocation = new ResourceLocation(RFTools.MODID, "text/dimlets.json");
    private static final Set<DimletKey> dimletBlackList = new HashSet<DimletKey>();
    private static final Map<DimletKey,Integer> dimletBuiltinRfCreate = new HashMap<DimletKey, Integer>();
    private static final Map<DimletKey,Integer> dimletBuiltinRfMaintain = new HashMap<DimletKey, Integer>();
    private static final Map<DimletKey,Integer> dimletBuiltinTickCost = new HashMap<DimletKey, Integer>();

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
        initRarity(cfg, DimletType.DIMLET_BIOME, 3);
        initRarity(cfg, DimletType.DIMLET_TIME, 1);
        initRarity(cfg, DimletType.DIMLET_FOLIAGE, 2);
        initRarity(cfg, DimletType.DIMLET_LIQUID, 1);
        initRarity(cfg, DimletType.DIMLET_MATERIAL, 2);
        initRarity(cfg, DimletType.DIMLET_MOBS, 1);
        initRarity(cfg, DimletType.DIMLET_SKY, 1);
        initRarity(cfg, DimletType.DIMLET_STRUCTURE, 1);
        initRarity(cfg, DimletType.DIMLET_TERRAIN, 1);
        initRarity(cfg, DimletType.DIMLET_FEATURE, 1);
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
    }

    private static void initTypeTickCost(Configuration cfg, DimletType type, int cost) {
        typeTickCost.put(type, cfg.get(CATEGORY_TYPETICKCOST, "ticks." + type.getName(), cost).getInt());
    }

    private static void registerDimletEntry(int id, DimletEntry dimletEntry) {
        idToDimlet.put(id, dimletEntry);
        dimletToID.put(dimletEntry, id);
        dimletIds.add(id);
    }

    private static int registerDimlet(Configuration cfg, DimletKey key) {
        String k = "dimlet." + key.getType().getName() + "." + key.getName();
        int id = -1;
        if (dimletBlackList.contains(key)) {
            // Blacklisted! But it is possibly overridden by the user in the config.
            id = cfg.get(CATEGORY_KNOWNDIMLETS, k, -1).getInt();
        } else {
            id = cfg.get(CATEGORY_KNOWNDIMLETS, k, lastId + 1).getInt();
            if (id > lastId) {
                lastId = id;
            }
        }

        if (id == -1) {
            return -1;
        }

        int rfCreateCost = checkCostConfig(cfg, "rfcreate.", key, dimletBuiltinRfCreate, typeRfCreateCost);
        int rfMaintainCost = checkCostConfig(cfg, "rfmaintain.", key, dimletBuiltinRfMaintain, typeRfMaintainCost);
        int tickCost = checkCostConfig(cfg, "ticks.", key, dimletBuiltinTickCost, typeTickCost);

        DimletEntry entry = new DimletEntry(key, rfCreateCost, rfMaintainCost, tickCost);
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

        initBiomeItems(cfg);

        initMaterialItem(cfg, Blocks.diamond_block);
        initMaterialItem(cfg, Blocks.diamond_ore);
        initMaterialItem(cfg, Blocks.gold_block);
        initMaterialItem(cfg, Blocks.gold_ore);

        initFoliageItem(cfg);

        initLiquidItems(cfg);

        initMobItem(cfg, EntityZombie.class, "Zombie");
        initMobItem(cfg, EntitySkeleton.class, "Skeleton");

        initSkyItem(cfg, "Clear");
        initSkyItem(cfg, "Bright");

        int idStructureNone = initStructureItem(cfg, "None", StructureType.STRUCTURE_NONE);
        initStructureItem(cfg, "Village", StructureType.STRUCTURE_VILLAGE);
        initStructureItem(cfg, "Stronghold", StructureType.STRUCTURE_STRONGHOLD);
        initStructureItem(cfg, "Dungeon", StructureType.STRUCTURE_DUNGEON);
        initStructureItem(cfg, "Fortress", StructureType.STRUCTURE_FORTRESS);
        initStructureItem(cfg, "Mineshaft", StructureType.STRUCTURE_MINESHAFT);
        initStructureItem(cfg, "Scattered", StructureType.STRUCTURE_SCATTERED);

        int idTerrainVoid = initTerrainItem(cfg, "Void", TerrainType.TERRAIN_VOID);
        initTerrainItem(cfg, "Flat", TerrainType.TERRAIN_FLAT);
        initTerrainItem(cfg, "Amplified", TerrainType.TERRAIN_AMPLIFIED);
        initTerrainItem(cfg, "Normal", TerrainType.TERRAIN_NORMAL);
        initTerrainItem(cfg, "Cave World", TerrainType.TERRAIN_CAVES);
        initTerrainItem(cfg, "Island", TerrainType.TERRAIN_ISLAND);
        initTerrainItem(cfg, "Spheres", TerrainType.TERRAIN_SPHERES);

        int idFeatureNone = initFeatureItem(cfg, "None", FeatureType.FEATURE_NONE);
        initFeatureItem(cfg, "Caves", FeatureType.FEATURE_CAVES);
        initFeatureItem(cfg, "Ravines", FeatureType.FEATURE_RAVINES);

        initTimeItem(cfg, "Day");
        initTimeItem(cfg, "Night");
        initTimeItem(cfg, "Day/Night");

        ModItems.knownDimlet = new KnownDimlet();
        ModItems.knownDimlet.setUnlocalizedName("KnownDimlet");
        ModItems.knownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        GameRegistry.registerItem(ModItems.knownDimlet, "knownDimlet");

        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idFeatureNone), new Object[] { " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.string, 'p', Items.paper } );
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idStructureNone), new Object[] { " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bone, 'p', Items.paper } );
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idTerrainVoid), new Object[] { " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', Items.paper } );

        setupChestLoot();
    }

    private static void initMaterialItem(Configuration cfg, Block block) {
        int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_MATERIAL, block.getUnlocalizedName()));
        ItemStack stack = new ItemStack(block);
        idToDisplayName.put(id, DimletType.DIMLET_MATERIAL.getName() + " " + stack.getDisplayName() + " Dimlet");
    }

    /**
     * Read the built-in blacklist and default configuration for dimlets.
     */
    private static void readBuiltinConfig() {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        try {
            IResource iresource = resourceManager.getResource(builtinConfigLocation);
            InputStream inputstream = iresource.getInputStream();
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
            DimletType type = DimletType.getTypeByName(typeName);
            if (type == null) {
                RFTools.logError("Error in dimlets.json! Unknown type '" + typeName + "'!");
                return;
            }
            DimletKey key = new DimletKey(type, name);
            dimletBuiltinRfCreate.put(key, rfcreate);
            dimletBuiltinRfMaintain.put(key, rfmaintain);
            dimletBuiltinTickCost.put(key, tickCost);
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

    private static void initBiomeItems(Configuration cfg) {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                String name = biome.biomeName;
                int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_BIOME, name));
                idToBiome.put(id, name);
                idToDisplayName.put(id, DimletType.DIMLET_BIOME.getName() + " " + name + " Dimlet");
            }
        }
    }

    private static void initFoliageItem(Configuration cfg) {
        int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_FOLIAGE, "Oak"));
        idToDisplayName.put(id, "Foliage Oak Dimlet");
    }

    private static void initLiquidItems(Configuration cfg) {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_LIQUID, me.getKey()));
            String displayName = new FluidStack(me.getValue(), 1).getLocalizedName();
            idToDisplayName.put(id, DimletType.DIMLET_LIQUID.getName() + " " + displayName + " Dimlet");
        }
    }

    private static int initMobItem(Configuration cfg, Class <? extends EntityLiving> entity, String name) {
        int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_MOBS, name));
        idToDisplayName.put(id, DimletType.DIMLET_MOBS.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initSkyItem(Configuration cfg, String name) {
        int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_SKY, name));
        idToDisplayName.put(id, DimletType.DIMLET_SKY.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initStructureItem(Configuration cfg, String name, StructureType structureType) {
        int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_STRUCTURE, name));
        idToStructureType.put(id, structureType);
        idToDisplayName.put(id, DimletType.DIMLET_STRUCTURE.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initTerrainItem(Configuration cfg, String name, TerrainType terrainType) {
        int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_TERRAIN, name));
        idToTerrainType.put(id, terrainType);
        idToDisplayName.put(id, DimletType.DIMLET_TERRAIN.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initFeatureItem(Configuration cfg, String name, FeatureType featureType) {
        int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_FEATURE, name));
        idToFeatureType.put(id, featureType);
        idToDisplayName.put(id, DimletType.DIMLET_FEATURE.getName() + " " + name + " Dimlet");
        return id;
    }

    private static int initTimeItem(Configuration cfg, String name) {
        int id = registerDimlet(cfg, new DimletKey(DimletType.DIMLET_TIME, name));
        idToDisplayName.put(id, DimletType.DIMLET_TIME.getName() + " " + name + " Dimlet");
        return id;
    }

}

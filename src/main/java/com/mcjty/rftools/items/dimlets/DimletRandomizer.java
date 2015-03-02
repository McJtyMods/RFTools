package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.description.MobDescriptor;
import com.mcjty.rftools.dimension.world.types.EffectType;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.varia.WeightedRandomSelector;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class DimletRandomizer {

    public static final int RARITY_0 = 0;
    public static final int RARITY_1 = 1;
    public static final int RARITY_2 = 2;
    public static final int RARITY_3 = 3;
    public static final int RARITY_4 = 4;
    public static final int RARITY_5 = 5;
    public static final int RARITY_6 = 6;

    public static final Map<DimletType,Integer> typeRarity = new HashMap<DimletType, Integer>();
    // Used for randomly generating dimlets.
    public static final List<Integer> dimletIds = new ArrayList<Integer>();
    static final Map<DimletKey,Integer> dimletBuiltinRarity = new HashMap<DimletKey, Integer>();

    // All dimlet ids in a weighted random selector based on rarity.
    public static WeightedRandomSelector<Integer,Integer> randomDimlets;
    public static WeightedRandomSelector<Integer,Integer> randomMaterialDimlets;
    public static WeightedRandomSelector<Integer,Integer> randomLiquidDimlets;
    public static WeightedRandomSelector<Integer,Integer> randomMobDimlets;
    public static WeightedRandomSelector<Integer,Integer> randomStructureDimlets;
    public static WeightedRandomSelector<Integer,Integer> randomEffectDimlets;
    public static WeightedRandomSelector<Integer,Integer> randomFeatureDimlets;

    public static void initTypeRarity(Configuration cfg) {
        typeRarity.clear();
        initRarity(cfg, DimletType.DIMLET_BIOME, RARITY_1);
        initRarity(cfg, DimletType.DIMLET_TIME, RARITY_2);
        initRarity(cfg, DimletType.DIMLET_FOLIAGE, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_LIQUID, RARITY_2);
        initRarity(cfg, DimletType.DIMLET_MATERIAL, RARITY_1);
        initRarity(cfg, DimletType.DIMLET_MOBS, RARITY_2);
        initRarity(cfg, DimletType.DIMLET_SKY, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_STRUCTURE, RARITY_3);
        initRarity(cfg, DimletType.DIMLET_TERRAIN, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_FEATURE, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_DIGIT, RARITY_0);
        initRarity(cfg, DimletType.DIMLET_EFFECT, RARITY_3);
        initRarity(cfg, DimletType.DIMLET_SPECIAL, RARITY_5);
        initRarity(cfg, DimletType.DIMLET_CONTROLLER, RARITY_1);
    }

    private static void initRarity(Configuration cfg, DimletType type, int rarity) {
        typeRarity.put(type, cfg.get(KnownDimletConfiguration.CATEGORY_TYPERARIRTY, "rarity." + type.getName(), rarity).getInt());
    }

    public static void clean() {
        randomDimlets = null;
        randomMaterialDimlets = null;
        randomLiquidDimlets = null;
        randomMobDimlets = null;
        randomStructureDimlets = null;
        randomEffectDimlets = null;
        randomFeatureDimlets = null;
    }

    static void setupWeightedRandomList(Configuration cfg, DimletMapping mapping) {
        float rarity0 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level0", 500.0f).getDouble();
        float rarity1 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level1", 250.0f).getDouble();
        float rarity2 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level2", 150.0f).getDouble();
        float rarity3 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level3", 90.0f).getDouble();
        float rarity4 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level4", 40.0f).getDouble();
        float rarity5 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level5", 20.0f).getDouble();
        float rarity6 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level6", 1.0f).getDouble();

        randomDimlets = new WeightedRandomSelector<Integer, Integer>();
        setupRarity(randomDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomMaterialDimlets = new WeightedRandomSelector<Integer, Integer>();
        setupRarity(randomMaterialDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomLiquidDimlets = new WeightedRandomSelector<Integer, Integer>();
        setupRarity(randomLiquidDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomMobDimlets = new WeightedRandomSelector<Integer, Integer>();
        setupRarity(randomMobDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomStructureDimlets = new WeightedRandomSelector<Integer, Integer>();
        setupRarity(randomStructureDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomEffectDimlets = new WeightedRandomSelector<Integer, Integer>();
        setupRarity(randomEffectDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomFeatureDimlets = new WeightedRandomSelector<Integer, Integer>();
        setupRarity(randomFeatureDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);

        for (Map.Entry<Integer, DimletEntry> entry : KnownDimletConfiguration.idToDimletEntry.entrySet()) {
            randomDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
            if (entry.getValue().getKey().getType() == DimletType.DIMLET_MATERIAL) {
                // Don't add the 'null' material.
                if (DimletObjectMapping.idToBlock.get(entry.getKey()) != null) {
                    randomMaterialDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (entry.getValue().getKey().getType() == DimletType.DIMLET_LIQUID) {
                // Don't add the 'null' fluid.
                if (DimletObjectMapping.idToFluid.get(entry.getKey()) != null) {
                    randomLiquidDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (entry.getValue().getKey().getType() == DimletType.DIMLET_MOBS) {
                // Don't add the 'null' mob.
                MobDescriptor descriptor = DimletObjectMapping.idtoMob.get(entry.getKey());
                if (descriptor != null && descriptor.getEntityClass() != null) {
                    randomMobDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (entry.getValue().getKey().getType() == DimletType.DIMLET_EFFECT) {
                // Don't add the 'null' effect.
                if (DimletObjectMapping.idToEffectType.get(entry.getKey()) != EffectType.EFFECT_NONE) {
                    randomEffectDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (entry.getValue().getKey().getType() == DimletType.DIMLET_FEATURE) {
                // Don't add the 'null' feature.
                if (DimletObjectMapping.idToFeatureType.get(entry.getKey()) != FeatureType.FEATURE_NONE) {
                    randomFeatureDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (entry.getValue().getKey().getType() == DimletType.DIMLET_STRUCTURE) {
                // Don't add the 'null' structure.
                if (DimletObjectMapping.idToStructureType.get(entry.getKey()) != StructureType.STRUCTURE_NONE) {
                    randomStructureDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            }
        }
    }

    public static int getRandomMob(Random random, boolean allowRandom) {
        Integer id = randomMobDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(id).isRandomNotAllowed()) {
            id = randomMobDimlets.select(random);
        }
        return id;
    }

    public static int getRandomEffect(Random random, boolean allowRandom) {
        Integer id = randomEffectDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(id).isRandomNotAllowed()) {
            id = randomEffectDimlets.select(random);
        }
        return id;
    }

    public static int getRandomFeature(Random random, boolean allowRandom) {
        Integer id = randomFeatureDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(id).isRandomNotAllowed()) {
            id = randomFeatureDimlets.select(random);
        }
        return id;
    }

    public static int getRandomStructure(Random random, boolean allowRandom) {
        Integer id = randomStructureDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(id).isRandomNotAllowed()) {
            id = randomStructureDimlets.select(random);
        }
        return id;
    }

    public static int getRandomFluidBlock(Random random) {
        return randomLiquidDimlets.select(random);
    }

    public static int getRandomMaterialBlock(Random random, boolean allowRandom) {
        Integer id = randomMaterialDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(id).isRandomNotAllowed()) {
            id = randomMaterialDimlets.select(random);
        }
        return id;
    }

    private static void setupRarity(WeightedRandomSelector<Integer,Integer> randomDimlets, float rarity0, float rarity1, float rarity2, float rarity3, float rarity4, float rarity5, float rarity6) {
        randomDimlets.addRarity(RARITY_0, rarity0);
        randomDimlets.addRarity(RARITY_1, rarity1);
        randomDimlets.addRarity(RARITY_2, rarity2);
        randomDimlets.addRarity(RARITY_3, rarity3);
        randomDimlets.addRarity(RARITY_4, rarity4);
        randomDimlets.addRarity(RARITY_5, rarity5);
        randomDimlets.addRarity(RARITY_6, rarity6);
    }

    // Get a random dimlet. A bonus of 0.01 will already give a good increase in getting rare items. 0.0 is default.
    public static int getRandomDimlet(float bonus, Random random) {
        return randomDimlets.select(randomDimlets.createDistribution(bonus), random);
    }

    // Get a random dimlet with no bonus.
    public static int getRandomDimlet(Random random) {
        return randomDimlets.select(random);
    }

    // Get a random dimlet with the given distribution.
    public static int getRandomDimlet(WeightedRandomSelector.Distribution<Integer> distribution, Random random) {
        return randomDimlets.select(distribution, random);
    }

    public static void dumpRarityDistribution(float bonus, World world) {
        Random random = new Random();
        Map<Integer,Integer> counter = new HashMap<Integer, Integer>();
        WeightedRandomSelector.Distribution<Integer> distribution = randomDimlets.createDistribution(bonus);

        for (Integer id : dimletIds) {
            counter.put(id, 0);
        }

        final int total = 10000000;
        for (int i = 0 ; i < total ; i++) {
            int id = randomDimlets.select(distribution, random);
            counter.put(id, counter.get(id)+1);
        }

        RFTools.log("#### Dumping with bonus=" + bonus);
        List<Pair<Integer,Integer>> sortedCounters = new ArrayList<Pair<Integer, Integer>>();
        for (Map.Entry<Integer, Integer> entry : counter.entrySet()) {
            sortedCounters.add(Pair.of(entry.getValue(), entry.getKey()));
        }
        Collections.sort(sortedCounters);

        DimletMapping mapping = DimletMapping.getDimletMapping(world);
        for (Pair<Integer, Integer> entry : sortedCounters) {
            int count = entry.getKey();
            int id = entry.getValue();
            float percentage = count * 100.0f / total;
            RFTools.log("Id:"+id + ",    key:\"" + mapping.getKey(id).getName() + "\",    name:\""+ KnownDimletConfiguration.idToDisplayName.get(id)+"\",    count:"+ count + ", "+percentage+"%");
        }
    }

    public static void dumpMaterialRarityDistribution(World world) {
        Random random = new Random();
        Map<Integer,Integer> counter = new HashMap<Integer, Integer>();

        for (Integer id : DimletObjectMapping.idToBlock.keySet()) {
            counter.put(id, 0);
        }

        final int total = 10000000;
        for (int i = 0 ; i < total ; i++) {
            int id = randomMaterialDimlets.select(random);
            counter.put(id, counter.get(id)+1);
        }

        RFTools.log("#### Dumping material distribution");
        List<Pair<Integer,Integer>> sortedCounters = new ArrayList<Pair<Integer, Integer>>();
        for (Map.Entry<Integer, Integer> entry : counter.entrySet()) {
            sortedCounters.add(Pair.of(entry.getValue(), entry.getKey()));
        }
        Collections.sort(sortedCounters);

        DimletMapping mapping = DimletMapping.getDimletMapping(world);
        for (Pair<Integer, Integer> entry : sortedCounters) {
            int count = entry.getKey();
            int id = entry.getValue();
            float percentage = count * 100.0f / total;
            RFTools.log("Id:"+id + ",    key:\"" + mapping.getKey(id).getName() + "\",    name:\""+ KnownDimletConfiguration.idToDisplayName.get(id)+"\",    count:"+ count + ", "+percentage+"%");
        }
    }
}

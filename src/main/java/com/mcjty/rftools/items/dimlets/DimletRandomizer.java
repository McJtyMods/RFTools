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

    public static float rarity0;
    public static float rarity1;
    public static float rarity2;
    public static float rarity3;
    public static float rarity4;
    public static float rarity5;
    public static float rarity6;

    public static final Map<DimletType,Integer> typeRarity = new HashMap<DimletType, Integer>();
    // Used for randomly generating dimlets.
    public static final List<DimletKey> dimletIds = new ArrayList<DimletKey>();
    static final Map<DimletKey,Integer> dimletBuiltinRarity = new HashMap<DimletKey, Integer>();

    // All dimlet ids in a weighted random selector based on rarity.
    public static WeightedRandomSelector<Integer,DimletKey> randomDimlets;
    public static WeightedRandomSelector<Integer,DimletKey> randomMaterialDimlets;
    public static WeightedRandomSelector<Integer,DimletKey> randomLiquidDimlets;
    public static WeightedRandomSelector<Integer,DimletKey> randomMobDimlets;
    public static WeightedRandomSelector<Integer,DimletKey> randomStructureDimlets;
    public static WeightedRandomSelector<Integer,DimletKey> randomEffectDimlets;
    public static WeightedRandomSelector<Integer,DimletKey> randomFeatureDimlets;

    public static void clean() {
        randomDimlets = null;
        randomMaterialDimlets = null;
        randomLiquidDimlets = null;
        randomMobDimlets = null;
        randomStructureDimlets = null;
        randomEffectDimlets = null;
        randomFeatureDimlets = null;
    }

    public static void readRandomConfig(Configuration cfg) {
        rarity0 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level0", 500.0f).getDouble();
        rarity1 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level1", 250.0f).getDouble();
        rarity2 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level2", 150.0f).getDouble();
        rarity3 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level3", 90.0f).getDouble();
        rarity4 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level4", 40.0f).getDouble();
        rarity5 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level5", 20.0f).getDouble();
        rarity6 = (float) cfg.get(KnownDimletConfiguration.CATEGORY_RARITY, "level6", 1.0f).getDouble();
        initTypeRarity(cfg);
    }

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
        initRarity(cfg, DimletType.DIMLET_WEATHER, RARITY_1);
    }

    private static void initRarity(Configuration cfg, DimletType type, int rarity) {
        typeRarity.put(type, cfg.get(KnownDimletConfiguration.CATEGORY_TYPERARIRTY, "rarity." + type.getName(), rarity).getInt());
    }

    static void setupWeightedRandomList() {
        randomDimlets = new WeightedRandomSelector<Integer, DimletKey>();
        setupRarity(randomDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomMaterialDimlets = new WeightedRandomSelector<Integer, DimletKey>();
        setupRarity(randomMaterialDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomLiquidDimlets = new WeightedRandomSelector<Integer, DimletKey>();
        setupRarity(randomLiquidDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomMobDimlets = new WeightedRandomSelector<Integer, DimletKey>();
        setupRarity(randomMobDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomStructureDimlets = new WeightedRandomSelector<Integer, DimletKey>();
        setupRarity(randomStructureDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomEffectDimlets = new WeightedRandomSelector<Integer, DimletKey>();
        setupRarity(randomEffectDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);
        randomFeatureDimlets = new WeightedRandomSelector<Integer, DimletKey>();
        setupRarity(randomFeatureDimlets, rarity0, rarity1, rarity2, rarity3, rarity4, rarity5, rarity6);

        for (Map.Entry<DimletKey, DimletEntry> entry : KnownDimletConfiguration.idToDimletEntry.entrySet()) {
            randomDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
            DimletKey key = entry.getValue().getKey();
            if (key.getType() == DimletType.DIMLET_MATERIAL) {
                // Don't add the 'null' material.
                if (DimletObjectMapping.idToBlock.get(key) != null) {
                    randomMaterialDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (key.getType() == DimletType.DIMLET_LIQUID) {
                // Don't add the 'null' fluid.
                if (DimletObjectMapping.idToFluid.get(key) != null) {
                    randomLiquidDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (key.getType() == DimletType.DIMLET_MOBS) {
                // Don't add the 'null' mob.
                MobDescriptor descriptor = DimletObjectMapping.idtoMob.get(key);
                if (descriptor != null && descriptor.getEntityClass() != null) {
                    randomMobDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (key.getType() == DimletType.DIMLET_EFFECT) {
                // Don't add the 'null' effect.
                if (DimletObjectMapping.idToEffectType.get(key) != EffectType.EFFECT_NONE) {
                    randomEffectDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (key.getType() == DimletType.DIMLET_FEATURE) {
                // Don't add the 'null' feature.
                if (DimletObjectMapping.idToFeatureType.get(key) != FeatureType.FEATURE_NONE) {
                    randomFeatureDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            } else if (key.getType() == DimletType.DIMLET_STRUCTURE) {
                // Don't add the 'null' structure.
                if (DimletObjectMapping.idToStructureType.get(key) != StructureType.STRUCTURE_NONE) {
                    randomStructureDimlets.addItem(entry.getValue().getRarity(), entry.getKey());
                }
            }
        }
    }

    public static DimletKey getRandomMob(Random random, boolean allowRandom) {
        DimletKey key = randomMobDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(key).isRandomNotAllowed()) {
            key = randomMobDimlets.select(random);
        }
        return key;
    }

    public static DimletKey getRandomEffect(Random random, boolean allowRandom) {
        DimletKey key = randomEffectDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(key).isRandomNotAllowed()) {
            key = randomEffectDimlets.select(random);
        }
        return key;
    }

    public static DimletKey getRandomFeature(Random random, boolean allowRandom) {
        DimletKey key = randomFeatureDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(key).isRandomNotAllowed()) {
            key = randomFeatureDimlets.select(random);
        }
        return key;
    }

    public static DimletKey getRandomStructure(Random random, boolean allowRandom) {
        DimletKey key = randomStructureDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(key).isRandomNotAllowed()) {
            key = randomStructureDimlets.select(random);
        }
        return key;
    }

    public static DimletKey getRandomFluidBlock(Random random) {
        return randomLiquidDimlets.select(random);
    }

    public static DimletKey getRandomMaterialBlock(Random random, boolean allowRandom) {
        DimletKey key = randomMaterialDimlets.select(random);
        while ((!allowRandom) && KnownDimletConfiguration.getEntry(key).isRandomNotAllowed()) {
            key = randomMaterialDimlets.select(random);
        }
        return key;
    }

    private static void setupRarity(WeightedRandomSelector<Integer,DimletKey> randomDimlets, float rarity0, float rarity1, float rarity2, float rarity3, float rarity4, float rarity5, float rarity6) {
        randomDimlets.addRarity(RARITY_0, rarity0);
        randomDimlets.addRarity(RARITY_1, rarity1);
        randomDimlets.addRarity(RARITY_2, rarity2);
        randomDimlets.addRarity(RARITY_3, rarity3);
        randomDimlets.addRarity(RARITY_4, rarity4);
        randomDimlets.addRarity(RARITY_5, rarity5);
        randomDimlets.addRarity(RARITY_6, rarity6);
    }


    // Get a random dimlet. A bonus of 0.01 will already give a good increase in getting rare items. 0.0 is default.
    public static DimletKey getRandomDimlet(float bonus, Random random) {
        return randomDimlets.select(randomDimlets.createDistribution(bonus), random);
    }

    // Get a random dimlet with no bonus.
    public static DimletKey getRandomDimlet(Random random) {
        return randomDimlets.select(random);
    }

    // Get a random dimlet with the given distribution.
    public static DimletKey getRandomDimlet(WeightedRandomSelector.Distribution<Integer> distribution, Random random) {
        return randomDimlets.select(distribution, random);
    }

    public static void dumpRarityDistribution(float bonus) {
        Random random = new Random();
        Map<DimletKey,Integer> counter = new HashMap<DimletKey, Integer>();
        WeightedRandomSelector.Distribution<Integer> distribution = randomDimlets.createDistribution(bonus);

        DimletMapping mapping = DimletMapping.getInstance();
        for (DimletKey key : dimletIds) {
            counter.put(key, 0);
        }

        final int total = 10000000;
        for (int i = 0 ; i < total ; i++) {
            DimletKey id = randomDimlets.select(distribution, random);
            counter.put(id, counter.get(id)+1);
        }

        RFTools.log("#### Dumping with bonus=" + bonus);
        List<Pair<Integer,DimletKey>> sortedCounters = new ArrayList<Pair<Integer, DimletKey>>();
        for (Map.Entry<DimletKey, Integer> entry : counter.entrySet()) {
            sortedCounters.add(Pair.of(entry.getValue(), entry.getKey()));
        }
        Collections.sort(sortedCounters, new Comparator<Pair<Integer, DimletKey>>() {
            @Override
            public int compare(Pair<Integer, DimletKey> o1, Pair<Integer, DimletKey> o2) {
                return o1.getLeft().compareTo(o2.getLeft());
            }
        });

        for (Pair<Integer, DimletKey> entry : sortedCounters) {
            int count = entry.getKey();
            DimletKey key = entry.getValue();
            int id = mapping.getId(key);
            float percentage = count * 100.0f / total;

            formatDimletOutput(count, key, id, percentage);
        }
    }

    public static void dumpMaterialRarityDistribution(World world) {
        Random random = new Random();
        Map<DimletKey,Integer> counter = new HashMap<DimletKey, Integer>();

        for (DimletKey id : DimletObjectMapping.idToBlock.keySet()) {
            counter.put(id, 0);
        }

        DimletMapping mapping = DimletMapping.getDimletMapping(world);

        final int total = 10000000;
        for (int i = 0 ; i < total ; i++) {
            DimletKey key = randomMaterialDimlets.select(random);
            counter.put(key, counter.get(key)+1);
        }

        RFTools.log("#### Dumping material distribution");
        List<Pair<Integer,DimletKey>> sortedCounters = new ArrayList<Pair<Integer,DimletKey>>();
        for (Map.Entry<DimletKey, Integer> entry : counter.entrySet()) {
            sortedCounters.add(Pair.of(entry.getValue(), entry.getKey()));
        }
        Collections.sort(sortedCounters, new Comparator<Pair<Integer, DimletKey>>() {
            @Override
            public int compare(Pair<Integer, DimletKey> o1, Pair<Integer, DimletKey> o2) {
                return o1.getLeft().compareTo(o2.getLeft());
            }
        });

        for (Pair<Integer, DimletKey> entry : sortedCounters) {
            int count = entry.getKey();
            DimletKey key = entry.getValue();
            int id = mapping.getId(key);
            float percentage = count * 100.0f / total;

            formatDimletOutput(count, key, id, percentage);
        }
    }

    private static void formatDimletOutput(int count, DimletKey key, int id, float percentage) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        String name = KnownDimletConfiguration.idToDisplayName.get(key);
        DimletEntry de = KnownDimletConfiguration.getEntry(key);
        int rarity = -1;
        if (de != null) {
            rarity = de.getRarity();
        }
        formatter.format("Id:%1$-5d  Key:%2$-40.40s Name:%3$-40.40s [Count:%4$-8d %5$g%% R:%6$d]", id, key.toString(), name, count, percentage, rarity);
        RFTools.log(sb.toString());
    }
}

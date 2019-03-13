package mcjty.rftools.config;

import mcjty.lib.thirteen.ConfigSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    // Craftability of dimensional shards.
    public static final int CRAFT_NONE = 0;
    public static final int CRAFT_EASY = 1;
    public static final int CRAFT_HARD = 2;
    public static ConfigSpec.IntValue dimensionalShardRecipeWithDimensions;
    public static ConfigSpec.IntValue dimensionalShardRecipeWithoutDimensions;

    // Dimensions where dimensional shard ore can generate.
    private static ConfigSpec.ConfigValue<List<? extends Integer>> dimensionalShardOregenWithDimensions;
    private static ConfigSpec.ConfigValue<List<? extends Integer>> dimensionalShardOregenWithoutDimensions;
    public static Set<Integer> oregenDimensionsWithDimensions = new HashSet<>();
    public static Set<Integer> oregenDimensionsWithoutDimensions = new HashSet<>();

    // Ore settings
    public static ConfigSpec.IntValue oreMinimumVeinSize;
    public static ConfigSpec.IntValue oreMaximumVeinSize;
    public static ConfigSpec.IntValue oreMaximumVeinCount;
    public static ConfigSpec.IntValue oreMinimumHeight;
    public static ConfigSpec.IntValue oreMaximumHeight;
    public static ConfigSpec.BooleanValue retrogen;

    // For the syringe
    public static ConfigSpec.IntValue maxMobInjections;        // Maximum amount of injections we need to do a full mob extraction.


    public static ConfigSpec.IntValue villagerId;               // -1 means disable, 0 means auto-id, other means fixed id

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        CLIENT_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

        oreMinimumVeinSize = SERVER_BUILDER
                .comment("Minimum vein size of dimensional shard ores")
                .defineInRange("oreMinimumVeinSize", 5, 0, 10000);
        oreMaximumVeinSize = SERVER_BUILDER
                .comment("Maximum vein size of dimensional shard ores")
                .defineInRange("oreMaximumVeinSize", 8, 0, 10000);
        oreMaximumVeinCount = SERVER_BUILDER
                .comment("Maximum number of veins for dimensional shard ores")
                .defineInRange("oreMaximumVeinCount", 3, 0, 10000);
        oreMinimumHeight = SERVER_BUILDER
                .comment("Minimum y level for dimensional shard ores")
                .defineInRange("oreMinimumHeight", 2, 0, 255);
        oreMaximumHeight = SERVER_BUILDER
                .comment("Maximum y level for dimensional shard ores")
                .defineInRange("oreMaximumHeight", 40, 0, 255);
        retrogen = SERVER_BUILDER
                .comment("Set to true to enable retrogen")
                .define("retrogen", true);

        dimensionalShardRecipeWithDimensions = SERVER_BUILDER
                .comment("Craftability of dimensional shards if RFTools Dimension is present: 0=not, 1=easy, 2=hard")
                .defineInRange("dimensionalShardRecipeWithDimensions", CRAFT_NONE, 0, 2);
        dimensionalShardRecipeWithoutDimensions = SERVER_BUILDER
                .comment("Craftability of dimensional shards if RFTools Dimension is not present: 0=not, 1=easy, 2=hard")
                .defineInRange("dimensionalShardRecipeWithoutDimensions", CRAFT_HARD, 0, 2);

        List<Integer> defaults = new ArrayList<>();
        defaults.add(-1);
        defaults.add(1);

        dimensionalShardOregenWithDimensions = SERVER_BUILDER
                .comment("Oregen for dimensional shards in case RFTools Dimensions is present")
                .defineIntList("dimensionalShardOregenWithDimensions", defaults, o -> o instanceof Integer);
        dimensionalShardOregenWithoutDimensions = SERVER_BUILDER
                .comment("Oregen for dimensional shards in case RFTools Dimensions is not present")
                .defineIntList("dimensionalShardOregenWithoutDimensions", defaults, o -> o instanceof Integer);

        maxMobInjections = SERVER_BUILDER
                .comment("Amount of injections needed to get a fully absorbed mob essence")
                .defineInRange("maxMobInjections", 10, 0, 10000);

        villagerId = SERVER_BUILDER
                .comment("The ID for the RFTools villager. -1 means disable, 0 means to automatically assigns an id, any other number will use that as fixed id")
                .defineInRange("villagerId", 0, -1, Integer.MAX_VALUE);
//        if (villagerId == 0) {
//            villagerId = findFreeVillagerId();
//            ConfigCategory category = cfg.getCategory(CATEGORY_GENERAL);
//            Property property = new Property("villagerId", Integer.toString(GeneralConfiguration.villagerId), INTEGER);
//            property.setComment("The ID for the RFTools villager. -1 means disable, 0 means to automatically assigns an id, any other number will use that as fixed id");
//            category.put("villagerId", property);
//        }

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }

    public static void resolve() {
        oregenDimensionsWithDimensions.addAll(dimensionalShardOregenWithDimensions.get());
        oregenDimensionsWithoutDimensions.addAll(dimensionalShardOregenWithoutDimensions.get());
    }

    private static int findFreeVillagerId() {
        int id = 10;
//        Collection<Integer> registeredVillagers = VillagerRegistry.getRegisteredVillagers();
//        while (registeredVillagers.contains(id)) {
//            id++;
//        }
        //@todo
        return id;
    }

}

package mcjty.rftools;

import mcjty.lib.varia.Logging;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static net.minecraftforge.common.config.Property.Type.INTEGER;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static boolean enableMatterTransmitterRecipe = true;
    public static boolean enableMatterReceiverRecipe = true;
    public static boolean enableDialingDeviceRecipe = true;
    public static boolean enableEndergenRecipe = true;
    public static boolean enableBuilderRecipe = true;
    public static boolean enableShieldProjectorRecipe = true;

    // Craftability of dimensional shards.
    public static final int CRAFT_NONE = 0;
    public static final int CRAFT_EASY = 1;
    public static final int CRAFT_HARD = 2;
    public static int dimensionalShardRecipeWithDimensions = CRAFT_NONE;
    public static int dimensionalShardRecipeWithoutDimensions = CRAFT_HARD;

    // Dimensions where dimensional shard ore can generate.
    private static int[] dimensionalShardOregenWithDimensions = new int[] { };
    private static int[] dimensionalShardOregenWithoutDimensions = new int[] { -1, 1 };
    public static Set<Integer> oregenDimensionsWithDimensions = new HashSet<>();
    public static Set<Integer> oregenDimensionsWithoutDimensions = new HashSet<>();

    // Ore settings
    public static int oreMinimumVeinSize = 5;
    public static int oreMaximumVeinSize = 8;
    public static int oreMaximumVeinCount = 3;
    public static int oreMinimumHeight = 2;
    public static int oreMaximumHeight = 40;
    public static boolean retrogen = true;


    public static int villagerId = 0;               // -1 means disable, 0 means auto-id, other means fixed id

    public static void init(Configuration cfg) {
        Logging.doLogging = cfg.get(CATEGORY_GENERAL, "logging", Logging.doLogging,
                "If true dump a lot of logging information about various things in RFTools. Useful for debugging.").getBoolean();

        enableMatterTransmitterRecipe = cfg.get(CATEGORY_GENERAL, "enableMatterTransmitterRecipe", enableMatterTransmitterRecipe,
                "Enable the matter transmitter recipe.").getBoolean();
        enableMatterReceiverRecipe = cfg.get(CATEGORY_GENERAL, "enableMatterReceiverRecipe", enableMatterReceiverRecipe,
                "Enable the matter receiver recipe.").getBoolean();
        enableDialingDeviceRecipe = cfg.get(CATEGORY_GENERAL, "enableDialingDeviceRecipe", enableDialingDeviceRecipe,
                "Enable the dialing device recipe.").getBoolean();
        enableEndergenRecipe = cfg.get(CATEGORY_GENERAL, "enableEndergenRecipe", enableEndergenRecipe,
                "Enable the endergenic generator recipe.").getBoolean();
        enableBuilderRecipe = cfg.get(CATEGORY_GENERAL, "enableBuilderRecipe", enableBuilderRecipe,
                "Enable the builder recipe.").getBoolean();
        enableShieldProjectorRecipe = cfg.get(CATEGORY_GENERAL, "enableShieldProjectorRecipe", enableShieldProjectorRecipe,
                                              "Enable the shield projector recipe.").getBoolean();

        oreMinimumVeinSize = cfg.get(CATEGORY_GENERAL, "oreMinimumVeinSize", oreMinimumVeinSize,
                                     "Minimum vein size of dimensional shard ores").getInt();
        oreMaximumVeinSize = cfg.get(CATEGORY_GENERAL, "oreMaximumVeinSize", oreMaximumVeinSize,
                                     "Maximum vein size of dimensional shard ores").getInt();
        oreMaximumVeinCount = cfg.get(CATEGORY_GENERAL, "oreMaximumVeinCount", oreMaximumVeinCount,
                                      "Maximum number of veins for dimensional shard ores").getInt();
        oreMinimumHeight = cfg.get(CATEGORY_GENERAL, "oreMinimumHeight", oreMinimumHeight,
                                   "Minimum y level for dimensional shard ores").getInt();
        oreMaximumHeight = cfg.get(CATEGORY_GENERAL, "oreMaximumHeight", oreMaximumHeight,
                                   "Maximum y level for dimensional shard ores").getInt();
        retrogen = cfg.get(CATEGORY_GENERAL, "retrogen", retrogen,
                                   "Set to true to enable retrogen").getBoolean();

        dimensionalShardRecipeWithDimensions = cfg.get(CATEGORY_GENERAL, "dimensionalShardRecipeWithDimensions", dimensionalShardRecipeWithDimensions,
                                       "Craftability of dimensional shards if RFTools Dimension is present: 0=not, 1=easy, 2=hard").getInt();
        dimensionalShardRecipeWithoutDimensions = cfg.get(CATEGORY_GENERAL, "dimensionalShardRecipeWithoutDimensions", dimensionalShardRecipeWithoutDimensions,
                                       "Craftability of dimensional shards if RFTools Dimension is not present: 0=not, 1=easy, 2=hard").getInt();

        dimensionalShardOregenWithDimensions = cfg.get(CATEGORY_GENERAL, "dimensionalShardOregenWithDimensions", dimensionalShardOregenWithDimensions,
                                                       "Oregen for dimensional shards in case RFTools Dimensions is present").getIntList();
        dimensionalShardOregenWithoutDimensions = cfg.get(CATEGORY_GENERAL, "dimensionalShardOregenWithoutDimensions", dimensionalShardOregenWithoutDimensions,
                                                       "Oregen for dimensional shards in case RFTools Dimensions is not present").getIntList();
        for (int i : dimensionalShardOregenWithDimensions) {
            oregenDimensionsWithDimensions.add(i);
        }
        for (int i : dimensionalShardOregenWithoutDimensions) {
            oregenDimensionsWithoutDimensions.add(i);
        }

        villagerId = cfg.get(CATEGORY_GENERAL, "villagerId", villagerId,
                "The ID for the RFTools villager. -1 means disable, 0 means to automatically assigns an id, any other number will use that as fixed id").getInt();
        if (villagerId == 0) {
            villagerId = findFreeVillagerId();
            ConfigCategory category = cfg.getCategory(CATEGORY_GENERAL);
            Property property = new Property("villagerId", Integer.toString(GeneralConfiguration.villagerId), INTEGER);
            property.comment = "The ID for the RFTools villager. -1 means disable, 0 means to automatically assigns an id, any other number will use that as fixed id";
            category.put("villagerId", property);
        }
    }

    private static int findFreeVillagerId() {
        int id = 10;
        Collection<Integer> registeredVillagers = VillagerRegistry.getRegisteredVillagers();
        while (registeredVillagers.contains(id)) {
            id++;
        }
        return id;
    }

}

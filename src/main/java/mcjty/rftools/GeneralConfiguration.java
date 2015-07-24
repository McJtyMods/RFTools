package mcjty.rftools;

import cpw.mods.fml.common.registry.VillagerRegistry;
import mcjty.base.GeneralConfig;
import mcjty.gui.GuiConfig;
import mcjty.varia.Logging;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.Collection;

import static net.minecraftforge.common.config.Property.Type.INTEGER;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static boolean enableDimensionBuilderRecipe = true;
    public static boolean enableDimensionEditorRecipe = true;
    public static boolean enableMatterTransmitterRecipe = true;
    public static boolean enableMatterReceiverRecipe = true;
    public static boolean enableDialingDeviceRecipe = true;
    public static boolean enableBuilderRecipe = true;
    public static boolean enableShieldProjectorRecipe = true;
    public static boolean enableEndergenRecipe = true;
    public static boolean enableBlockProtectorRecipe = true;

    public static int villagerId = 0;               // -1 means disable, 0 means auto-id, other means fixed id

    public static void init(Configuration cfg) {
        Logging.doLogging = cfg.get(CATEGORY_GENERAL, "logging", Logging.doLogging,
                "If true dump a lot of logging information about various things in RFTools. Useful for debugging.").getBoolean();

        enableDimensionBuilderRecipe = cfg.get(CATEGORY_GENERAL, "enableDimensionBuilderRecipe", enableDimensionBuilderRecipe,
                "Enable the dimension builder recipe.").getBoolean();
        enableDimensionEditorRecipe = cfg.get(CATEGORY_GENERAL, "enableDimensionEditorRecipe", enableDimensionEditorRecipe,
                "Enable the dimension editor recipe.").getBoolean();
        enableMatterTransmitterRecipe = cfg.get(CATEGORY_GENERAL, "enableMatterTransmitterRecipe", enableMatterTransmitterRecipe,
                "Enable the matter transmitter recipe.").getBoolean();
        enableMatterReceiverRecipe = cfg.get(CATEGORY_GENERAL, "enableMatterReceiverRecipe", enableMatterReceiverRecipe,
                "Enable the matter receiver recipe.").getBoolean();
        enableDialingDeviceRecipe = cfg.get(CATEGORY_GENERAL, "enableDialingDeviceRecipe", enableDialingDeviceRecipe,
                "Enable the dialing device recipe.").getBoolean();
        enableBuilderRecipe = cfg.get(CATEGORY_GENERAL, "enableBuilderRecipe", enableBuilderRecipe,
                "Enable the builder recipe.").getBoolean();
        enableShieldProjectorRecipe = cfg.get(CATEGORY_GENERAL, "enableShieldProjectorRecipe", enableShieldProjectorRecipe,
                "Enable the shield projector recipe.").getBoolean();
        enableEndergenRecipe = cfg.get(CATEGORY_GENERAL, "enableEndergenRecipe", enableEndergenRecipe,
                "Enable the endergenic generator recipe.").getBoolean();
        enableBlockProtectorRecipe = cfg.get(CATEGORY_GENERAL, "enableBlockProtectorRecipe", enableBlockProtectorRecipe,
                "Enable the block protector recipe.").getBoolean();

        GuiConfig.itemListBackground = cfg.get(CATEGORY_GENERAL, "itemListBackground", GuiConfig.itemListBackground,
                "Color for some list backgrounds").getInt();

        GeneralConfig.maxInfuse = cfg.get(CATEGORY_GENERAL, "maxInfuse", GeneralConfig.maxInfuse,
                "The maximum amount of dimensional shards that can be infused in a single machine").getInt();


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

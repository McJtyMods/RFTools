package mcjty.rftools;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static boolean doLogging = false;

    public static boolean enableDimensionBuilderRecipe = true;
    public static boolean enableDimensionEditorRecipe = true;
    public static boolean enableMatterTransmitterRecipe = true;
    public static boolean enableMatterReceiverRecipe = true;
    public static boolean enableDialingDeviceRecipe = true;
    public static boolean enableBuilderRecipe = true;
    public static boolean enableShieldProjectorRecipe = true;
    public static boolean enableEndergenRecipe = true;
    public static boolean enableBlockProtectorRecipe = true;

    public static int itemListBackground = 0xff909090;

    public static int realVillagerId;
    public static int villagerId = 0;               // -1 means disable, 0 means auto-id, other means fixed id

    public static void init(Configuration cfg) {
        doLogging = cfg.get(CATEGORY_GENERAL, "logging", doLogging,
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

        itemListBackground = cfg.get(CATEGORY_GENERAL, "itemListBackground", itemListBackground,
                "Color for some list backgrounds").getInt();

        villagerId = cfg.get(CATEGORY_GENERAL, "villagerId", villagerId,
                "The ID for the RFTools villager. -1 means disable, 0 means to automatically assigns an id, any other number will use that as fixed id").getInt();
    }

}

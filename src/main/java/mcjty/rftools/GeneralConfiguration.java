package mcjty.rftools;

import mcjty.lib.varia.Logging;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.Collection;

import static net.minecraftforge.common.config.Property.Type.INTEGER;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static boolean enableMatterTransmitterRecipe = true;
    public static boolean enableMatterReceiverRecipe = true;
    public static boolean enableDialingDeviceRecipe = true;
    public static boolean enableEndergenRecipe = true;

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

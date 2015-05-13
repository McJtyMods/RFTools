package mcjty.rftools.blocks.storage;

import net.minecraftforge.common.config.Configuration;

public class ModularStorageConfiguration {
    public static final String CATEGORY_STORAGE = "storage";

    public static int itemListBackground = 0xff8090a0;
    public static int groupBackground = 0xffeedd33;
    public static int groupForeground = 0xff000000;

    public static void init(Configuration cfg) {
        itemListBackground = cfg.get(CATEGORY_STORAGE, "itemListBackground", itemListBackground,
                "Color for the item background").getInt();
        groupBackground = cfg.get(CATEGORY_STORAGE, "groupBackground", groupBackground,
                "Background color for group lines").getInt();
        groupForeground = cfg.get(CATEGORY_STORAGE, "groupForeground", groupForeground,
                "Foreground color for group lines").getInt();
    }
}

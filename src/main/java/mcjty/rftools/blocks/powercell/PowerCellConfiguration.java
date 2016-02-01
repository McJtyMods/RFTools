package mcjty.rftools.blocks.powercell;

import net.minecraftforge.common.config.Configuration;

public class PowerCellConfiguration {

    public static final String CATEGORY_POWERCELL = "powercell";
    public static int rfPerCell = 200000;
    public static int rfPerTick = 5000;

    public static void init(Configuration cfg) {
        rfPerTick = cfg.get(CATEGORY_POWERCELL, "rfPerTick", rfPerTick, "Base amount of RF/tick that can be extracted/inserted in this block").getInt();
        rfPerCell = cfg.get(CATEGORY_POWERCELL, "rfPerCell", rfPerCell,
                "Maximum RF storage that a single cell can hold").getInt();
    }
}

package mcjty.rftools.blocks.powercell;

import net.minecraftforge.common.config.Configuration;

public class PowerCellConfiguration {

    public static final String CATEGORY_POWERCELL = "powercell";
    public static int rfPerCell = 200000;
    public static int rfPerTick = 5000;

    public static double powerCellCostFactor = 1.05;

    public static void init(Configuration cfg) {
        rfPerTick = cfg.get(CATEGORY_POWERCELL, "rfPerTick", rfPerTick, "Base amount of RF/tick that can be extracted/inserted in this block").getInt();
        rfPerCell = cfg.get(CATEGORY_POWERCELL, "rfPerCell", rfPerCell, "Maximum RF storage that a single cell can hold").getInt();

        powerCellCostFactor = cfg.get(CATEGORY_POWERCELL, "powerCellCostFactor", powerCellCostFactor,
                                      "The maximum cost factor for extracting energy out of a powercell for blocks in other dimensions or farther away then 10000 blocks").getDouble();
    }
}

package mcjty.rftools.blocks.powercell;

import net.minecraftforge.common.config.Configuration;

public class PowerCellConfiguration {

    public static final String CATEGORY_POWERCELL = "powercell";
    public static int rfPerCell = 200000;
    public static int rfPerTick = 5000;

    public static double powerCellCostFactor = 1.10;
    public static double powerCellDistanceCap = 10000;
    public static double powerCellMinDistance = 100;
    public static double powerCellRFToolsDimensionAdvantage = .5;

    public static void init(Configuration cfg) {
        rfPerTick = cfg.get(CATEGORY_POWERCELL, "rfPerTick", rfPerTick, "Base amount of RF/tick that can be extracted/inserted in this block").getInt();
        rfPerCell = cfg.get(CATEGORY_POWERCELL, "rfPerCell", rfPerCell, "Maximum RF storage that a single cell can hold").getInt();

        powerCellCostFactor = cfg.get(CATEGORY_POWERCELL, "powerCellCostFactor", powerCellCostFactor,
                                      "The maximum cost factor for extracting energy out of a powercell for blocks in other dimensions or farther away then 10000 blocks").getDouble();
        powerCellDistanceCap = cfg.get(CATEGORY_POWERCELL, "powerCellDistanceCap", powerCellDistanceCap,
                                       "At this distance the cost factor will be maximum. This value is also used when power is extracted from cells in different dimensions").getDouble();
        powerCellMinDistance = cfg.get(CATEGORY_POWERCELL, "powerCellMinDistance", powerCellMinDistance,
                                       "As soon as powercells are not connected this value will be taken as the minimum distance to base the cost factor from").getDouble();
        powerCellRFToolsDimensionAdvantage = cfg.get(CATEGORY_POWERCELL, "powerCellRFToolsDimensionAdvantage", powerCellRFToolsDimensionAdvantage,
                                       "A multiplier for the distance if RFTools dimensions are involved. If both sides are RFTools dimensions then this multiplier is done twice").getDouble();
    }
}

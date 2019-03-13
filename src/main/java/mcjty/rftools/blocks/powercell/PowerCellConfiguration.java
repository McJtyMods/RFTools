package mcjty.rftools.blocks.powercell;

import net.minecraftforge.common.config.Configuration;

public class PowerCellConfiguration {

    public static final String CATEGORY_POWERCELL = "powercell";
    public static int rfPerNormalCell = 1000000;
    public static int advancedFactor = 4;
    public static int simpleFactor = 4;
    public static int rfPerTick = 5000;

    public static double powerCellCostFactor = 1.10;
    public static double powerCellDistanceCap = 10000;
    public static double powerCellMinDistance = 100;
    public static double powerCellRFToolsDimensionAdvantage = .5;

    public static int CHARGEITEMPERTICK = 30000;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(PowerCellConfiguration.CATEGORY_POWERCELL, "Settings for the powercell");
        rfPerTick = cfg.get(CATEGORY_POWERCELL, "rfPerTick", rfPerTick, "Base amount of RF/tick that can be extracted/inserted in this block").getInt();
        rfPerNormalCell = cfg.get(CATEGORY_POWERCELL, "rfPerNormalCell", rfPerNormalCell, "Maximum RF storage that a single cell can hold").getInt();

        advancedFactor = cfg.get(CATEGORY_POWERCELL, "advancedFactor", advancedFactor, "How much better is the advanced cell with RF and RF/t").getInt();
        simpleFactor = cfg.get(CATEGORY_POWERCELL, "simpleFactor", simpleFactor, "How much worse is the simple cell with RF and RF/t").getInt();

        powerCellCostFactor = cfg.get(CATEGORY_POWERCELL, "powerCellCostFactor", powerCellCostFactor,
                                      "The maximum cost factor for extracting energy out of a powercell for blocks in other dimensions or farther away then 10000 blocks").getDouble();
        powerCellDistanceCap = cfg.get(CATEGORY_POWERCELL, "powerCellDistanceCap", powerCellDistanceCap,
                                       "At this distance the cost factor will be maximum. This value is also used when power is extracted from cells in different dimensions").getDouble();
        powerCellMinDistance = cfg.get(CATEGORY_POWERCELL, "powerCellMinDistance", powerCellMinDistance,
                                       "As soon as powercells are not connected this value will be taken as the minimum distance to base the cost factor from").getDouble();
        powerCellRFToolsDimensionAdvantage = cfg.get(CATEGORY_POWERCELL, "powerCellRFToolsDimensionAdvantage", powerCellRFToolsDimensionAdvantage,
                                       "A multiplier for the distance if RFTools dimensions are involved. If both sides are RFTools dimensions then this multiplier is done twice").getDouble();

        CHARGEITEMPERTICK = cfg.get(CATEGORY_POWERCELL, "powercellChargePerTick", CHARGEITEMPERTICK,
                                    "RF per tick that the powrcell can charge items with").getInt();
    }
}

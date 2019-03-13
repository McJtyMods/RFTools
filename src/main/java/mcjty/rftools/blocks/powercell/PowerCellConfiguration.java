package mcjty.rftools.blocks.powercell;

import mcjty.lib.thirteen.ConfigSpec;

public class PowerCellConfiguration {

    public static final String CATEGORY_POWERCELL = "powercell";
    public static ConfigSpec.IntValue rfPerNormalCell;
    public static ConfigSpec.IntValue advancedFactor;
    public static ConfigSpec.IntValue simpleFactor;
    public static ConfigSpec.IntValue rfPerTick;

    public static ConfigSpec.DoubleValue powerCellCostFactor;
    public static ConfigSpec.DoubleValue powerCellDistanceCap;
    public static ConfigSpec.DoubleValue powerCellMinDistance;
    public static ConfigSpec.DoubleValue powerCellRFToolsDimensionAdvantage;

    public static ConfigSpec.IntValue CHARGEITEMPERTICK;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the powercell").push(CATEGORY_POWERCELL);
        CLIENT_BUILDER.comment("Settings for the powercell").push(CATEGORY_POWERCELL);

        rfPerTick = SERVER_BUILDER
                .comment("Base amount of RF/tick that can be extracted/inserted in this block")
                .defineInRange("rfPerTick", 5000, 0, Integer.MAX_VALUE);
        rfPerNormalCell = SERVER_BUILDER
                .comment("Maximum RF storage that a single cell can hold")
                .defineInRange("rfPerNormalCell", 1000000, 0, Integer.MAX_VALUE);

        advancedFactor = SERVER_BUILDER
                .comment("How much better is the advanced cell with RF and RF/t")
                .defineInRange("advancedFactor", 4, 0, Integer.MAX_VALUE);
        simpleFactor = SERVER_BUILDER
                .comment("How much worse is the simple cell with RF and RF/t")
                .defineInRange("simpleFactor", 4, 0, Integer.MAX_VALUE);

        powerCellCostFactor = SERVER_BUILDER
                .comment("The maximum cost factor for extracting energy out of a powercell for blocks in other dimensions or farther away then 10000 blocks")
                .defineInRange("powerCellCostFactor", 1.10, 0, 1000000000.0);
        powerCellDistanceCap = SERVER_BUILDER
                .comment("At this distance the cost factor will be maximum. This value is also used when power is extracted from cells in different dimensions")
                .defineInRange("powerCellDistanceCap", 10000.0, 0, 1000000000.0);
        powerCellMinDistance = SERVER_BUILDER
                .comment("As soon as powercells are not connected this value will be taken as the minimum distance to base the cost factor from")
                .defineInRange("powerCellMinDistance", 100.0, 0, 1000000000.0);
        powerCellRFToolsDimensionAdvantage = SERVER_BUILDER
                .comment("A multiplier for the distance if RFTools dimensions are involved. If both sides are RFTools dimensions then this multiplier is done twice")
                .defineInRange("powerCellRFToolsDimensionAdvantage", 0.5, 0, 1000000000.0);

        CHARGEITEMPERTICK = SERVER_BUILDER
                .comment("RF per tick that the powrcell can charge items with")
                .defineInRange("powercellChargePerTick", 30000, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}

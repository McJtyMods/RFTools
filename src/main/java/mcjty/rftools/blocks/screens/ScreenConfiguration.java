package mcjty.rftools.blocks.screens;

import net.minecraftforge.common.config.Configuration;

public class ScreenConfiguration {
    public static final String CATEGORY_SCREEN = "screen";
    public static int CONTROLLER_MAXENERGY = 60000;
    public static int CONTROLLER_RECEIVEPERTICK = 1000;

    public static int BUTTON_RFPERTICK = 0;
    public static int ELEVATOR_BUTTON_RFPERTICK = 0;
    public static int CLOCK_RFPERTICK = 1;
    public static int COMPUTER_RFPERTICK = 4;
    public static int COUNTERPLUS_RFPERTICK = 30;
    public static int COUNTER_RFPERTICK = 4;
    public static int DIMENSION_RFPERTICK = 6;
    public static int ENERGY_RFPERTICK = 4;
    public static int ENERGYPLUS_RFPERTICK = 30;
    public static int FLUID_RFPERTICK = 4;
    public static int FLUIDPLUS_RFPERTICK = 30;
    public static int ITEMSTACKPLUS_RFPERTICK = 30;
    public static int ITEMSTACK_RFPERTICK = 4;
    public static int MACHINEINFO_RFPERTICK = 4;
    public static int REDSTONE_RFPERTICK = 4;
    public static int TEXT_RFPERTICK = 0;

    public static void init(Configuration cfg) {
        CONTROLLER_MAXENERGY = cfg.get(CATEGORY_SCREEN, "screenControllerMaxRF", CONTROLLER_MAXENERGY,
                "Maximum RF storage that the screen controller can hold").getInt();
        CONTROLLER_RECEIVEPERTICK = cfg.get(CATEGORY_SCREEN, "dimletResearcherRFPerTick", CONTROLLER_RECEIVEPERTICK,
                "RF per tick that the the screen controller can receive").getInt();

        BUTTON_RFPERTICK = cfg.get(CATEGORY_SCREEN, "buttonRFPerTick", BUTTON_RFPERTICK,
                                   "RF per tick/per block for the button module").getInt();
        ELEVATOR_BUTTON_RFPERTICK = cfg.get(CATEGORY_SCREEN, "elevatorButtonRFPerTick", ELEVATOR_BUTTON_RFPERTICK,
                                   "RF per tick/per block for the elevator button module").getInt();
        CLOCK_RFPERTICK = cfg.get(CATEGORY_SCREEN, "clockRFPerTick", CLOCK_RFPERTICK,
                "RF per tick/per block for the clock module").getInt();
        COMPUTER_RFPERTICK = cfg.get(CATEGORY_SCREEN, "computerRFPerTick", COMPUTER_RFPERTICK,
                "RF per tick/per block for the computer module").getInt();
        COUNTERPLUS_RFPERTICK = cfg.get(CATEGORY_SCREEN, "counterPlusRFPerTick", COUNTERPLUS_RFPERTICK,
                "RF per tick/per block for the counter plus module").getInt();
        COUNTER_RFPERTICK = cfg.get(CATEGORY_SCREEN, "counterRFPerTick", COUNTER_RFPERTICK,
                "RF per tick/per block for the counter module").getInt();
        DIMENSION_RFPERTICK = cfg.get(CATEGORY_SCREEN, "dimensionRFPerTick", DIMENSION_RFPERTICK,
                "RF per tick/per block for the dimension module").getInt();
        ENERGY_RFPERTICK = cfg.get(CATEGORY_SCREEN, "energyRFPerTick", ENERGY_RFPERTICK,
                "RF per tick/per block for the energy module").getInt();
        ENERGYPLUS_RFPERTICK = cfg.get(CATEGORY_SCREEN, "energyPlusRFPerTick", ENERGYPLUS_RFPERTICK,
                "RF per tick/per block for the energy plus module").getInt();
        FLUID_RFPERTICK = cfg.get(CATEGORY_SCREEN, "fluidRFPerTick", FLUID_RFPERTICK,
                "RF per tick/per block for the fluid module").getInt();
        FLUIDPLUS_RFPERTICK = cfg.get(CATEGORY_SCREEN, "fluidPlusRFPerTick", FLUIDPLUS_RFPERTICK,
                "RF per tick/per block for the fluid plus module").getInt();
        ITEMSTACKPLUS_RFPERTICK = cfg.get(CATEGORY_SCREEN, "itemstackPlusRFPerTick", ITEMSTACKPLUS_RFPERTICK,
                "RF per tick/per block for the itemstack plus module").getInt();
        ITEMSTACK_RFPERTICK = cfg.get(CATEGORY_SCREEN, "itemstackRFPerTick", ITEMSTACK_RFPERTICK,
                "RF per tick/per block for the itemstack module").getInt();
        MACHINEINFO_RFPERTICK = cfg.get(CATEGORY_SCREEN, "machineInfoRFPerTick", MACHINEINFO_RFPERTICK,
                "RF per tick/per block for the machine information module").getInt();
        REDSTONE_RFPERTICK = cfg.get(CATEGORY_SCREEN, "redstoneRFPerTick", REDSTONE_RFPERTICK,
                "RF per tick/per block for the redstone module").getInt();
        TEXT_RFPERTICK = cfg.get(CATEGORY_SCREEN, "textRFPerTick", TEXT_RFPERTICK,
                "RF per tick/per block for the text module").getInt();
    }

}

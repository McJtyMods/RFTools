package mcjty.rftools.blocks.screens;


import net.minecraftforge.common.ForgeConfigSpec;

public class ScreenConfiguration {

    public static final String CATEGORY_SCREEN = "screen";

    public static ForgeConfigSpec.IntValue CONTROLLER_MAXENERGY; //60000;
    public static ForgeConfigSpec.IntValue CONTROLLER_RECEIVEPERTICK; //1000;
    public static ForgeConfigSpec.IntValue BUTTON_RFPERTICK; //0;
    public static ForgeConfigSpec.IntValue DUMP_RFPERTICK; //0;
    public static ForgeConfigSpec.IntValue ELEVATOR_BUTTON_RFPERTICK; //0;
    public static ForgeConfigSpec.IntValue CLOCK_RFPERTICK; //1;
    public static ForgeConfigSpec.IntValue COMPUTER_RFPERTICK; //4;
    public static ForgeConfigSpec.IntValue COUNTERPLUS_RFPERTICK; //30;
    public static ForgeConfigSpec.IntValue COUNTER_RFPERTICK; //4;
    public static ForgeConfigSpec.IntValue DIMENSION_RFPERTICK; //6;
    public static ForgeConfigSpec.IntValue ENERGY_RFPERTICK; //4;
    public static ForgeConfigSpec.IntValue ENERGYPLUS_RFPERTICK; //30;
    public static ForgeConfigSpec.IntValue FLUID_RFPERTICK; //4;
    public static ForgeConfigSpec.IntValue FLUIDPLUS_RFPERTICK; //30;
    public static ForgeConfigSpec.IntValue ITEMSTACKPLUS_RFPERTICK; //30;
    public static ForgeConfigSpec.IntValue ITEMSTACK_RFPERTICK; //4;
    public static ForgeConfigSpec.IntValue MACHINEINFO_RFPERTICK; //4;
    public static ForgeConfigSpec.IntValue REDSTONE_RFPERTICK; //4;
    public static ForgeConfigSpec.IntValue TEXT_RFPERTICK; //0;
    public static ForgeConfigSpec.IntValue STORAGE_CONTROL_RFPERTICK; //6;
    public static ForgeConfigSpec.IntValue SCREEN_REFRESH_TIMING; //500;

    public static ForgeConfigSpec.BooleanValue useTruetype;
    public static ForgeConfigSpec.ConfigValue<String> font;
    public static ForgeConfigSpec.DoubleValue fontSize;
    public static ForgeConfigSpec.ConfigValue<String> additionalCharacters;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the screen system").push(CATEGORY_SCREEN);
        CLIENT_BUILDER.comment("Settings for the screen system").push(CATEGORY_SCREEN);

        CONTROLLER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the screen controller can hold")
                .defineInRange("screenControllerMaxRF", 60000, 0, Integer.MAX_VALUE);
        CONTROLLER_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the the screen controller can receive")
                .defineInRange("screenControllerRFPerTick", 1000, 0, Integer.MAX_VALUE);

        BUTTON_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the button module")
                .defineInRange("buttonRFPerTick", 9, 0, Integer.MAX_VALUE);
        DUMP_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the dump module")
                .defineInRange("dumpRFPerTick", 0, 0, Integer.MAX_VALUE);
        ELEVATOR_BUTTON_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the elevator button module")
                .defineInRange("elevatorButtonRFPerTick", 0, 0, Integer.MAX_VALUE);
        CLOCK_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the clock module")
                .defineInRange("clockRFPerTick", 1, 0, Integer.MAX_VALUE);
        COMPUTER_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the computer module")
                .defineInRange("computerRFPerTick", 4, 0, Integer.MAX_VALUE);
        COUNTERPLUS_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the counter plus module")
                .defineInRange("counterPlusRFPerTick", 30, 0, Integer.MAX_VALUE);
        COUNTER_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the counter module")
                .defineInRange("counterRFPerTick", 4, 0, Integer.MAX_VALUE);
        DIMENSION_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the dimension module")
                .defineInRange("dimensionRFPerTick", 6, 0, Integer.MAX_VALUE);
        ENERGY_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the energy module")
                .defineInRange("energyRFPerTick", 4, 0, Integer.MAX_VALUE);
        ENERGYPLUS_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the energy plus module")
                .defineInRange("energyPlusRFPerTick", 30, 0, Integer.MAX_VALUE);
        FLUID_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the fluid module")
                .defineInRange("fluidRFPerTick", 4, 0, Integer.MAX_VALUE);
        FLUIDPLUS_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the fluid plus module")
                .defineInRange("fluidPlusRFPerTick", 30, 0, Integer.MAX_VALUE);
        ITEMSTACKPLUS_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the itemstack plus module")
                .defineInRange("itemstackPlusRFPerTick", 30, 0, Integer.MAX_VALUE);
        ITEMSTACK_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the itemstack module")
                .defineInRange("itemstackRFPerTick", 4, 0, Integer.MAX_VALUE);
        MACHINEINFO_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the machine information module")
                .defineInRange("machineInfoRFPerTick", 4, 0, Integer.MAX_VALUE);
        REDSTONE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the redstone module")
                .defineInRange("redstoneRFPerTick", 4, 0, Integer.MAX_VALUE);
        TEXT_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the text module")
                .defineInRange("textRFPerTick", 0, 0, Integer.MAX_VALUE);
        STORAGE_CONTROL_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the storage control module")
                .defineInRange("storageControlRFPerTick", 0, 0, Integer.MAX_VALUE);

        useTruetype = CLIENT_BUILDER
                .comment("Set to true for TrueType font, set to false for vanilla font")
                .define("useTruetype", true);
        font = CLIENT_BUILDER
                .comment("The default truetype font to use")
                .define("fontName", "rftools:fonts/ubuntu.ttf");
        fontSize = CLIENT_BUILDER
                .comment("The size of the font")
                .defineInRange("fontSize", 40.0, 0, 1000000);
        additionalCharacters = CLIENT_BUILDER
                .comment("Additional characters that should be supported by the truetype system")
                .define("additionalCharacters", "");

        SCREEN_REFRESH_TIMING = SERVER_BUILDER
                .comment("How many times the screen will update. Higher numbers make the screens less accurate but better for network bandwidth")
                .defineInRange("screenRefreshTiming", 500, 0, Integer.MAX_VALUE);

        CLIENT_BUILDER.pop();
        SERVER_BUILDER.pop();
    }

}

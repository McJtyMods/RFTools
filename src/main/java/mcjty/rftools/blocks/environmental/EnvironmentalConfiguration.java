package mcjty.rftools.blocks.environmental;

import net.minecraftforge.common.config.Configuration;

public class EnvironmentalConfiguration {
    public static final String CATEGORY_ENVIRONMENTAL = "environmental";
    public static int ENVIRONMENTAL_MAXENERGY = 500000;
    public static int ENVIRONMENTAL_RECEIVEPERTICK = 20000;
    public static int MIN_USAGE = 5;

    public static float FEATHERFALLING_RFPERTICK = 0.001f;
    public static float FEATHERFALLINGPLUS_RFPERTICK = 0.003f;
    public static float FLIGHT_RFPERTICK = 0.004f;
    public static float HASTE_RFPERTICK = 0.001f;
    public static float HASTEPLUS_RFPERTICK = 0.003f;
    public static float NIGHTVISION_RFPERTICK = 0.001f;
    public static float PEACEFUL_RFPERTICK = 0.001f;
    public static float REGENERATION_RFPERTICK = 0.0015f;
    public static float REGENERATIONPLUS_RFPERTICK = 0.0045f;
    public static float SATURATION_RFPERTICK = 0.001f;
    public static float SATURATIONPLUS_RFPERTICK = 0.003f;
    public static float SPEED_RFPERTICK = 0.001f;
    public static float SPEEDPLUS_RFPERTICK = 0.003f;
    public static float WATERBREATHING_RFPERTICK = 0.001f;

    public static void init(Configuration cfg) {
        ENVIRONMENTAL_MAXENERGY = cfg.get(CATEGORY_ENVIRONMENTAL, "environmentalMaxRF", ENVIRONMENTAL_MAXENERGY,
                "Maximum RF storage that the environmental controller can hold").getInt();
        ENVIRONMENTAL_RECEIVEPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "environmentalRFPerTick", ENVIRONMENTAL_RECEIVEPERTICK,
                "RF per tick that the the environmental controller can receive").getInt();
        MIN_USAGE = cfg.get(CATEGORY_ENVIRONMENTAL, "environmentalMinRFUsage", MIN_USAGE,
                "The minimum RF/tick usage that an active controller consumes").getInt();

        FEATHERFALLING_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "featherfallingRFPerTick", FEATHERFALLING_RFPERTICK,
                "RF per tick/per block for feather falling module").getInt();
        FEATHERFALLINGPLUS_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "featherfallingPlusRFPerTick", FEATHERFALLINGPLUS_RFPERTICK,
                "RF per tick/per block for feather falling plus module").getInt();
        FLIGHT_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "flightRFPerTick", FLIGHT_RFPERTICK,
                "RF per tick/per block for flight module").getInt();
        HASTE_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "hasteRFPerTick", HASTE_RFPERTICK,
                "RF per tick/per block for haste module").getInt();
        HASTEPLUS_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "hastePlusRFPerTick", HASTEPLUS_RFPERTICK,
                "RF per tick/per block for haste plus module").getInt();
        NIGHTVISION_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "nightvisionRFPerTick", NIGHTVISION_RFPERTICK,
                "RF per tick/per block for night vision module").getInt();
        PEACEFUL_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "peacefulRFPerTick", PEACEFUL_RFPERTICK,
                "RF per tick/per block for peaceful module").getInt();
        REGENERATION_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "regenerationRFPerTick", REGENERATION_RFPERTICK,
                "RF per tick/per block for regeneration module").getInt();
        REGENERATIONPLUS_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "regenerationPlusRFPerTick", REGENERATIONPLUS_RFPERTICK,
                "RF per tick/per block for regeneration plus module").getInt();
        SATURATION_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "saturationRFPerTick", SATURATION_RFPERTICK,
                "RF per tick/per block for saturation module").getInt();
        SATURATIONPLUS_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "saturationPlusRFPerTick", SATURATIONPLUS_RFPERTICK,
                "RF per tick/per block for saturation plus module").getInt();
        SPEED_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "speedRFPerTick", SPEED_RFPERTICK,
                "RF per tick/per block for speed module").getInt();
        SPEEDPLUS_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "speedPlusRFPerTick", SPEEDPLUS_RFPERTICK,
                "RF per tick/per block for speed plus module").getInt();
        WATERBREATHING_RFPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "watherBreathingRFPerTick", WATERBREATHING_RFPERTICK,
                "RF per tick/per block for wather breathing module").getInt();
    }
}

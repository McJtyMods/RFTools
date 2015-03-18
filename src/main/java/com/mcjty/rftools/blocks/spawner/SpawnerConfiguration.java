package com.mcjty.rftools.blocks.spawner;

import net.minecraftforge.common.config.Configuration;

public class SpawnerConfiguration {
    public static final String CATEGORY_SPAWNER = "spawner";

    public static int SPAWNER_MAXENERGY = 200000;
    public static int SPAWNER_RECEIVEPERTICK = 1000;

    public static int BEAMER_MAXENERGY = 200000;
    public static int BEAMER_RECEIVEPERTICK = 1000;

    public static int matterAmount = 64 * 4;
    public static int maxMatterStorage = 64 * 10;

    public static void init(Configuration cfg) {
        SPAWNER_MAXENERGY = cfg.get(CATEGORY_SPAWNER, "spawnerMaxRF", SPAWNER_MAXENERGY,
                "Maximum RF storage that the spawner can hold").getInt();
        SPAWNER_RECEIVEPERTICK = cfg.get(CATEGORY_SPAWNER, "spawnerRFPerTick", SPAWNER_RECEIVEPERTICK,
                "RF per tick that the spawner can receive").getInt();

        BEAMER_MAXENERGY = cfg.get(CATEGORY_SPAWNER, "beamerMaxRF", BEAMER_MAXENERGY,
                "Maximum RF storage that the matter beamer can hold").getInt();
        BEAMER_RECEIVEPERTICK = cfg.get(CATEGORY_SPAWNER, "beamerRFPerTick", BEAMER_RECEIVEPERTICK,
                "RF per tick that the matter beamer can receive").getInt();

        matterAmount = cfg.get(CATEGORY_SPAWNER, "spawnerMatterAmount", matterAmount,
                "Amount of energized matter that is needed to spawn a single creature").getInt();
        maxMatterStorage = cfg.get(CATEGORY_SPAWNER, "spawnerMaxMatterStorage", maxMatterStorage,
                "The maximum amount of energized matter that this spawner can store").getInt();
    }
}

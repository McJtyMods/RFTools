package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class SpeedEModule extends PotionEffectModule {

    public SpeedEModule() {
        super("speed", 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.SPEED_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SPEED;
    }
}

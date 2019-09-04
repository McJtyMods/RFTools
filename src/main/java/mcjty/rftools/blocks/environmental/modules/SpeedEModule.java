package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class SpeedEModule extends PotionEffectModule {

    public SpeedEModule() {
        super("speed", 0);
    }

    @Override
    public float getRfPerTick() {
        return (float) (double) EnvironmentalConfiguration.SPEED_RFPERTICK.get();
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SPEED;
    }
}

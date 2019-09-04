package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class SpeedPlusEModule extends PotionEffectModule {

    public SpeedPlusEModule() {
        super("speed", 2);
    }

    @Override
    public float getRfPerTick() {
        return (float) (double) EnvironmentalConfiguration.SPEEDPLUS_RFPERTICK.get();
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SPEEDPLUS;
    }
}

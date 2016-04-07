package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class SpeedPlusEModule extends PotionEffectModule {

    public SpeedPlusEModule() {
        super("speed", 2);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.SPEEDPLUS_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SPEEDPLUS;
    }
}

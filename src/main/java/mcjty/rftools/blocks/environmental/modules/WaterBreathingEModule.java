package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class WaterBreathingEModule extends PotionEffectModule {

    public WaterBreathingEModule() {
        super("water_breathing", 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.WATERBREATHING_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_WATERBREATHING;
    }
}

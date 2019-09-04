package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class SaturationEModule extends PotionEffectModule {

    public SaturationEModule() {
        super("saturation", 0);
    }

    @Override
    public float getRfPerTick() {
        return (float) (double) EnvironmentalConfiguration.SATURATION_RFPERTICK.get();
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SATURATION;
    }
}

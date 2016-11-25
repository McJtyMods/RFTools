package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class NightVisionEModule extends PotionEffectModule {

    public NightVisionEModule() {
        super("night_vision", 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.NIGHTVISION_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_NIGHTVISION;
    }
}

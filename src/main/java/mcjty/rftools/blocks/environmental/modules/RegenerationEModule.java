package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class RegenerationEModule extends PotionEffectModule {

    public RegenerationEModule() {
        super("regeneration", 0);
    }

    @Override
    public float getRfPerTick() {
        return (float) (double) EnvironmentalConfiguration.REGENERATION_RFPERTICK.get();
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_REGENERATION;
    }
}

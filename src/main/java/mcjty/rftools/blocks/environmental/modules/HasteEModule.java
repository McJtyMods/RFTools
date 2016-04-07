package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class HasteEModule extends PotionEffectModule {

    public HasteEModule() {
        super("haste", 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.HASTE_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_HASTE;
    }
}

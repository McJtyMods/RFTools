package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class PoisonEModule extends PotionEffectModule {

    public PoisonEModule() {
        super("poison", 1);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.POISON_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_POISON;
    }
}

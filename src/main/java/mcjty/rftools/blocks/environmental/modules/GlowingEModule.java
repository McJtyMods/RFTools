package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class GlowingEModule extends PotionEffectModule {

    public GlowingEModule() {
        super("glowing", 0);
    }

    @Override
    public float getRfPerTick() {
        return (float) EnvironmentalConfiguration.HASTE_RFPERTICK.get();
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_GLOWING;
    }
}

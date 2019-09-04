package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class SlownessEModule extends PotionEffectModule {

    public SlownessEModule() {
        super("slowness", 3);
    }

    @Override
    public float getRfPerTick() {
        return (float) (double) EnvironmentalConfiguration.SLOWNESS_RFPERTICK.get();
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SLOWNESS;
    }

    @Override
    protected boolean allowedForPlayers() {
        return EnvironmentalConfiguration.slownessAvailable.get();
    }

}

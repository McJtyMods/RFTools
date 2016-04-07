package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class RegenerationPlusEModule extends PotionEffectModule {

    public RegenerationPlusEModule() {
        super("regeneration", 2);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.REGENERATIONPLUS_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_REGENERATIONPLUS;
    }
}

package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class LuckEModule extends PotionEffectModule {

    public LuckEModule() {
        super("luck", 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.LUCK_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_LUCK;
    }
}

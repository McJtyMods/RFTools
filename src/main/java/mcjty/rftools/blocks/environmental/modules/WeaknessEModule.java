package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class WeaknessEModule extends PotionEffectModule {

    public WeaknessEModule() {
        super("weakness", 1);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.WEAKNESS_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_WEAKNESS;
    }
}

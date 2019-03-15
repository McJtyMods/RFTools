package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class WeaknessEModule extends PotionEffectModule {

    public WeaknessEModule() {
        super("weakness", 1);
    }

    @Override
    public float getRfPerTick() {
        return (float) EnvironmentalConfiguration.WEAKNESS_RFPERTICK.get();
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_WEAKNESS;
    }

    @Override
    protected boolean allowedForPlayers() {
        return EnvironmentalConfiguration.weaknessAvailable.get();
    }

}

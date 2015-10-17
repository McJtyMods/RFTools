package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class BlindnessEModule extends PotionEffectModule {

    public BlindnessEModule() {
        super(Potion.blindness.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.BLINDNESS_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_BLINDNESS;
    }
}

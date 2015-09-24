package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class SlownessEModule extends PotionEffectModule {

    public SlownessEModule() {
        super(Potion.moveSlowdown.getId(), 3);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.SLOWNESS_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SLOWNESS;
    }
}

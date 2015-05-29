package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class HastePlusEModule extends PotionEffectModule {

    public HastePlusEModule() {
        super(Potion.digSpeed.getId(), 2);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.HASTEPLUS_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_HASTEPLUS;
    }
}

package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class SaturationPlusEModule extends PotionEffectModule {

    public SaturationPlusEModule() {
        super("saturation", 2);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.SATURATIONPLUS_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SATURATIONPLUS;
    }
}

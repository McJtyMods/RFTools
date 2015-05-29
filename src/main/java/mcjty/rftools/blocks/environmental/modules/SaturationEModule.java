package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class SaturationEModule extends PotionEffectModule {

    public SaturationEModule() {
        super(Potion.field_76443_y.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.SATURATION_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SATURATION;
    }
}

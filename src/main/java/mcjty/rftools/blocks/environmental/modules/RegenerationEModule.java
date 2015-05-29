package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class RegenerationEModule extends PotionEffectModule {

    public RegenerationEModule() {
        super(Potion.regeneration.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.REGENERATION_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_REGENERATION;
    }
}

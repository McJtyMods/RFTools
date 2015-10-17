package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class WeaknessEModule extends PotionEffectModule {

    public WeaknessEModule() {
        super(Potion.weakness.getId(), 1);
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

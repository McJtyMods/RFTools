package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class PoisonEModule extends PotionEffectModule {

    public PoisonEModule() {
        super(Potion.poison.getId(), 1);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.POISON_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_POISON;
    }
}

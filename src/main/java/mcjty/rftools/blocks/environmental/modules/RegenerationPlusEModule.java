package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import net.minecraft.potion.Potion;

public class RegenerationPlusEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.0045f;

    public RegenerationPlusEModule() {
        super(Potion.regeneration.getId(), 2);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_REGENERATIONPLUS;
    }
}

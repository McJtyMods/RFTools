package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import net.minecraft.potion.Potion;

public class RegenerationEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.0015f;

    public RegenerationEModule() {
        super(Potion.regeneration.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_REGENERATION;
    }
}

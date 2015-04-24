package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import net.minecraft.potion.Potion;

public class NightVisionEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.001f;

    public NightVisionEModule() {
        super(Potion.nightVision.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_NIGHTVISION;
    }
}

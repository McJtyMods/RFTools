package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import net.minecraft.potion.Potion;

public class WaterBreathingEModule extends PotionEffectModule {

    public WaterBreathingEModule() {
        super(Potion.waterBreathing.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.WATERBREATHING_RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_WATERBREATHING;
    }
}

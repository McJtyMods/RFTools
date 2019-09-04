package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class FeatherFallingPlusEModule extends BuffEModule {

    public FeatherFallingPlusEModule() {
        super(PlayerBuff.BUFF_FEATHERFALLINGPLUS);
    }

    @Override
    public float getRfPerTick() {
        return (float) (double) EnvironmentalConfiguration.FEATHERFALLINGPLUS_RFPERTICK.get();
    }
}

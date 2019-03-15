package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class FeatherFallingEModule extends BuffEModule {

    public FeatherFallingEModule() {
        super(PlayerBuff.BUFF_FEATHERFALLING);
    }

    @Override
    public float getRfPerTick() {
        return (float) EnvironmentalConfiguration.FEATHERFALLING_RFPERTICK.get();
    }
}

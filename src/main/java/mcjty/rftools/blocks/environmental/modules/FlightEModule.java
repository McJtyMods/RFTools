package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;

public class FlightEModule extends BuffEModule {
    public static final float RFPERTICK = 0.004f;

    public FlightEModule() {
        super(PlayerBuff.BUFF_FLIGHT);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}

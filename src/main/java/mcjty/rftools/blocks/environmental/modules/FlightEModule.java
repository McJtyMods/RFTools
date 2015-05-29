package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;

public class FlightEModule extends BuffEModule {

    public FlightEModule() {
        super(PlayerBuff.BUFF_FLIGHT);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.FLIGHT_RFPERTICK;
    }
}

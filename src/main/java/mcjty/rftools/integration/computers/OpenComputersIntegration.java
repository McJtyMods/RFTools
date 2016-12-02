package mcjty.rftools.integration.computers;

import li.cil.oc.api.Driver;
import net.minecraftforge.fml.common.Optional;

public class OpenComputersIntegration {
    @Optional.Method(modid="OpenComputers")
    public static void init() {
        Driver.add(new MachineInfuserDriver.OCDriver());
        Driver.add(new DialingDeviceDriver.OCDriver());
        Driver.add(new MatterReceiverDriver.OCDriver());
        Driver.add(new MatterTransmitterDriver.OCDriver());
        Driver.add(new CoalGeneratorDriver.OCDriver());
        Driver.add(new RFMonitorDriver.OCDriver());
        Driver.add(new LiquidMonitorDriver.OCDriver());
        Driver.add(new PowercellDriver.OCDriver());
        Driver.add(new PearlInjectorDriver.OCDriver());
    }
}

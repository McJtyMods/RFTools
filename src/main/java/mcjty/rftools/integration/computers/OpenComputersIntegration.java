package mcjty.rftools.integration.computers;

import li.cil.oc.api.Driver;
import mcjty.rftools.blocks.endergen.EndergenicConfiguration;
import mcjty.rftools.blocks.generator.CoalGeneratorConfiguration;
import net.minecraftforge.fml.common.Optional;

public class OpenComputersIntegration {
    @Optional.Method(modid="opencomputers")
    public static void init() {
        Driver.add(new MachineInfuserDriver.OCDriver());
        Driver.add(new DialingDeviceDriver.OCDriver());
        Driver.add(new MatterReceiverDriver.OCDriver());
        Driver.add(new MatterTransmitterDriver.OCDriver());
        if(CoalGeneratorConfiguration.enabled)
            Driver.add(new CoalGeneratorDriver.OCDriver());
        Driver.add(new RFMonitorDriver.OCDriver());
        Driver.add(new LiquidMonitorDriver.OCDriver());
        Driver.add(new PowercellDriver.OCDriver());
        if(EndergenicConfiguration.enabled) {
            Driver.add(new PearlInjectorDriver.OCDriver());
            Driver.add(new EndergenicDriver.OCDriver());
        }
    }
}

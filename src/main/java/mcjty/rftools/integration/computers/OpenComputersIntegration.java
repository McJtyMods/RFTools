package mcjty.rftools.integration.computers;

import li.cil.oc.api.Driver;
import mcjty.lib.integration.computers.OcCompatTools;
import net.minecraftforge.fml.common.Optional;

public class OpenComputersIntegration {
    @Optional.Method(modid="opencomputers")
    public static void init() {
        OcCompatTools.driverAdd(new MachineInfuserDriver.OCDriver());
        OcCompatTools.driverAdd(new DialingDeviceDriver.OCDriver());
        OcCompatTools.driverAdd(new MatterReceiverDriver.OCDriver());
        OcCompatTools.driverAdd(new MatterTransmitterDriver.OCDriver());
        OcCompatTools.driverAdd(new CoalGeneratorDriver.OCDriver());
        OcCompatTools.driverAdd(new RFMonitorDriver.OCDriver());
        OcCompatTools.driverAdd(new LiquidMonitorDriver.OCDriver());
        OcCompatTools.driverAdd(new PowercellDriver.OCDriver());
        OcCompatTools.driverAdd(new PearlInjectorDriver.OCDriver());
        OcCompatTools.driverAdd(new EndergenicDriver.OCDriver());
    }
}

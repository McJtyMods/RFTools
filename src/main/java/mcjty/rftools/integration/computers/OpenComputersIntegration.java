package mcjty.rftools.integration.computers;

import li.cil.oc.api.Driver;
import net.minecraftforge.fml.common.Optional;

public class OpenComputersIntegration {
    @Optional.Method(modid="OpenComputers")
    public static void init() {
        Driver.add(new MachineInfuserDriver.OCDriver());
    }
}

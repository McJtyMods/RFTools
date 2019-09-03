package mcjty.rftools.compat.wheelsupport;

import net.minecraftforge.fml.ModList;

public class WheelSupport {

    public static void registerWheel() {
        if (ModList.get().isLoaded("intwheel")) {
            WheelCompatibility.register();
        }
    }
}

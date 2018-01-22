package mcjty.rftools.wheelsupport;

import net.minecraftforge.fml.common.Loader;

public class WheelSupport {

    public static void registerWheel() {
        if (Loader.isModLoaded("intwheel")) {
            WheelCompatibility.register();
        }
    }
}

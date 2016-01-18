package mcjty.rftools.world;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModWorldgen {

    public static void init() {
        GameRegistry.registerWorldGenerator(RFToolsWorldGenerator.instance, 10);
        MinecraftForge.EVENT_BUS.register(RFToolsWorldGenerator.instance);
    }
}

package mcjty.rftools.village;

import cpw.mods.fml.common.registry.VillagerRegistry;
import mcjty.lib.varia.Logging;
import mcjty.rftools.GeneralConfiguration;
import net.minecraft.world.gen.structure.MapGenStructureIO;

public class VillageSetup {
    public static void villagerSetup() {
        if (GeneralConfiguration.villagerId != -1) {
            Logging.log("RFTools villager registered with id: " + GeneralConfiguration.villagerId);
            VillagerRegistry.instance().registerVillagerId(GeneralConfiguration.villagerId);
            RFToolsTradeHandler.INSTANCE.load();
            MapGenStructureIO.func_143031_a(VillagePiece.class, "Vrftools");
            VillagerRegistry.instance().registerVillageCreationHandler(new VillageCreationHandler());
        }
    }
}

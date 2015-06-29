package mcjty.rftools.village;

import cpw.mods.fml.common.registry.VillagerRegistry;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import net.minecraft.world.gen.structure.MapGenStructureIO;

import java.util.Collection;

public class VillageHandler {
    public static void villagerSetup() {
        GeneralConfiguration.realVillagerId = GeneralConfiguration.villagerId;
        if (GeneralConfiguration.realVillagerId != -1) {
            if (GeneralConfiguration.realVillagerId == 0) {
                int id = 10;
                Collection<Integer> registeredVillagers = VillagerRegistry.getRegisteredVillagers();
                while (registeredVillagers.contains(id)) {
                    id++;
                }
                GeneralConfiguration.realVillagerId = id;
            }
            RFTools.log("RFTools villager registered with id: " + GeneralConfiguration.realVillagerId);
            VillagerRegistry.instance().registerVillagerId(GeneralConfiguration.realVillagerId);
            RFToolsTradeHandler.INSTANCE.load();
            MapGenStructureIO.func_143031_a(VillagePiece.class, "Vrftools");
            VillagerRegistry.instance().registerVillageCreationHandler(new VillageCreationHandler());
        }
    }
}

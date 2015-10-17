package mcjty.rftools;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.rftools.dimension.world.types.FeatureType;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.OreGenEvent;

public class ForgeOregenHandlers {
    @SubscribeEvent
    public void onGenerateMinable(OreGenEvent.GenerateMinable event) {
        World world = event.world;
        int id = world.provider.dimensionId;
        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
        DimensionInformation information = dimensionManager.getDimensionInformation(id);
        if (information != null && information.hasFeatureType(FeatureType.FEATURE_CLEAN)) {
            event.setResult(Event.Result.DENY);
        }
    }

}

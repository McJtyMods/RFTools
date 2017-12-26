package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class BakedModelLoader implements ICustomModelLoader {

    public static final CamoModel MIMIC_MODEL = new CamoModel();

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(RFTools.MODID)) {
            return false;
        }
        return CamoShieldBlock.CAMO.equals(modelLocation.getResourcePath());
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        if (CamoShieldBlock.CAMO.equals(modelLocation.getResourcePath())) {
            return MIMIC_MODEL;
        }
        return null;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}

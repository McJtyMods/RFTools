package mcjty.rftools.compat.jei;

import mcjty.rftools.RFTools;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class RFToolsJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(RFTools.MODID, "rftools");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
//        ModularStorageRecipeTransferHandler.register(registration);
//        ModularStorageItemRecipeTransferHandler.register(registration);
//        RemoteStorageItemRecipeTransferHandler.register(registration);
//        StorageScannerRecipeTransferHandler.register(registration);
    }
}

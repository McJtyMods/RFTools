package mcjty.rftools;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.VillagerRegistry;
import mcjty.rftools.render.ModRenderers;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    private static final ResourceLocation VILLAGER_TEXTURE = new ResourceLocation(RFTools.MODID, "textures/entities/rftoolsvillager.png");

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        ModRenderers.init();
        MinecraftForge.EVENT_BUS.register(this);

        if (GeneralConfiguration.realVillagerId != -1) {
            VillagerRegistry.instance().registerVillagerSkin(GeneralConfiguration.realVillagerId, VILLAGER_TEXTURE);
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent evt) {
        RenderWorldLastEventHandler.tick(evt);
    }

    @SubscribeEvent
    public void renderGameOverlayEvent(RenderGameOverlayEvent evt) {
        RenderGameOverlayEventHandler.onRender(evt);
    }

}

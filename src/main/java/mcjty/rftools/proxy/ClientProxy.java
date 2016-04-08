package mcjty.rftools.proxy;

import mcjty.rftools.RFTools;
import mcjty.rftools.RenderGameOverlayEventHandler;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.items.ModItems;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {
    private static final ResourceLocation VILLAGER_TEXTURE = new ResourceLocation(RFTools.MODID, "textures/entities/rftoolsvillager.png");

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(RFTools.MODID);
        ModItems.initClient();
        ModBlocks.initClient();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @SubscribeEvent
    public void renderGameOverlayEvent(RenderGameOverlayEvent evt) {
        RenderGameOverlayEventHandler.onRender(evt);
    }
}

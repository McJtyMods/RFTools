package mcjty.rftools.setup;

import mcjty.lib.font.TrueTypeFont;
import mcjty.lib.setup.DefaultClientProxy;
import mcjty.rftools.RFTools;
import mcjty.rftools.RenderGameOverlayEventHandler;
import mcjty.rftools.RenderWorldLastEventHandler;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientProxy extends DefaultClientProxy {

    private static final ResourceLocation VILLAGER_TEXTURE = new ResourceLocation(RFTools.MODID, "textures/entities/rftoolsvillager.png");

    public static TrueTypeFont font;


    // @todo 1.14
//    @Override
//    public void preInit(FMLPreInitializationEvent e) {
//        super.preInit(e);
//        MinecraftForge.EVENT_BUS.register(this);
//        OBJLoader.INSTANCE.addDomain(RFTools.MODID);
//        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
//        ClientCommandHandler.registerCommands();
//    }
//
//    @Override
//    public void init(FMLInitializationEvent e) {
//        super.init(e);
//        ModBlocks.initClientPost();
//        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
//        KeyBindings.init();
//
//        font = FontLoader.createFont(new ResourceLocation(ScreenConfiguration.font.get()), (float) ScreenConfiguration.fontSize.get(), false, Font.TRUETYPE_FONT,
//                ScreenConfiguration.additionalCharacters.get().toCharArray());
//    }

    @SubscribeEvent
    public void colorHandlerEventBlock(ColorHandlerEvent.Block event) {
        ModBlocks.initColorHandlers(event.getBlockColors());
    }

    @SubscribeEvent
    public void renderGameOverlayEvent(RenderGameOverlayEvent evt) {
        RenderGameOverlayEventHandler.onRender(evt);
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent evt) {
        RenderWorldLastEventHandler.tick(evt);
    }

    @SubscribeEvent
    public void onRenderBlockOutline(DrawBlockHighlightEvent evt) {
        // @todo 1.14
//        RayTraceResult target = evt.getTarget();
//        if (target != null && target.typeOfHit == RayTraceResult.Type.BLOCK) {
//            BlockPos pos = target.getBlockPos();
//            if (pos == null || evt.getPlayer() == null) {
//                return;
//            }
//            Block block = evt.getPlayer().getEntityWorld().getBlockState(pos).getBlock();
//            if (block == ScreenSetup.screenBlock || block == ScreenSetup.creativeScreenBlock || block == ScreenSetup.screenHitBlock) {
//                evt.setCanceled(true);
//            }
//        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        // If a shape card GUI was opened by a tile entity GUI, restore the tile entity GUI when it's closed
        if(event.getGui() == null) {
            // @todo 1.14
//            net.minecraft.client.gui.GuiScreen old = Minecraft.getInstance().currentScreen;
//            if(old instanceof GuiShapeCard &&((GuiShapeCard)old).fromTE) {
//                event.setGui(GuiShapeCard.returnGui);
//                GuiShapeCard.returnGui = null;
//            }
        }
    }
}

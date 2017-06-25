package mcjty.rftools.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import mcjty.lib.McJtyLibClient;
import mcjty.lib.font.FontLoader;
import mcjty.lib.font.TrueTypeFont;
import mcjty.rftools.RFTools;
import mcjty.rftools.RenderGameOverlayEventHandler;
import mcjty.rftools.RenderWorldLastEventHandler;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.elevator.ElevatorSounds;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.keys.KeyBindings;
import mcjty.rftools.keys.KeyInputHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.concurrent.Callable;

public class ClientProxy extends CommonProxy {

    private static final ResourceLocation VILLAGER_TEXTURE = new ResourceLocation(RFTools.MODID, "textures/entities/rftoolsvillager.png");

    public static TrueTypeFont font;

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(this);
        OBJLoader.INSTANCE.addDomain(RFTools.MODID);
        McJtyLibClient.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        ModBlocks.initClientPost();
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        KeyBindings.init();

        font = FontLoader.createFont(new ResourceLocation(ScreenConfiguration.font), ScreenConfiguration.fontSize, false, Font.TRUETYPE_FONT,
                ScreenConfiguration.additionalCharacters.toCharArray());
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        ModItems.initClient();
        ModBlocks.initClient();
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
        ElevatorSounds.init(sounds.getRegistry());
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
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
        RayTraceResult target = evt.getTarget();
        if (target != null && target.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = target.getBlockPos();
            if (pos == null || evt.getPlayer() == null) {
                return;
            }
            Block block = evt.getPlayer().getEntityWorld().getBlockState(pos).getBlock();
            if (block == ScreenSetup.screenBlock || block == ScreenSetup.creativeScreenBlock || block == ScreenSetup.screenHitBlock) {
                evt.setCanceled(true);
            }
        }
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public <V> ListenableFuture<V> addScheduledTaskClient(Callable<V> callableToSchedule) {
        return Minecraft.getMinecraft().addScheduledTask(callableToSchedule);
    }

    @Override
    public ListenableFuture<Object> addScheduledTaskClient(Runnable runnableToSchedule) {
        return Minecraft.getMinecraft().addScheduledTask(runnableToSchedule);
    }
}

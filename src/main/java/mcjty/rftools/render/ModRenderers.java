package mcjty.rftools.render;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import mcjty.rftools.blocks.endergen.EndergenicRenderer;
import mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerRenderer;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import mcjty.rftools.blocks.logic.LogicSlabBlock;
import mcjty.rftools.blocks.logic.LogicSlabRenderer;
import mcjty.rftools.blocks.screens.ScreenRenderer;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import mcjty.rftools.blocks.shield.SolidShieldBlock;
import mcjty.rftools.blocks.shield.SolidShieldBlockRenderer;
import mcjty.rftools.blocks.spaceprojector.ProxyBlock;
import mcjty.rftools.blocks.spaceprojector.ProxyBlockRenderer;
import mcjty.rftools.blocks.spaceprojector.ProxyBlockTERenderer;
import mcjty.rftools.blocks.spaceprojector.ProxyBlockTileEntity;
import mcjty.rftools.blocks.spawner.MatterBeamerRenderer;
import mcjty.rftools.blocks.spawner.MatterBeamerTileEntity;
import mcjty.rftools.blocks.storage.ModularStorageBlock;
import mcjty.rftools.blocks.storage.ModularStorageRenderer;
import mcjty.rftools.blocks.storage.RemoteStorageBlock;
import mcjty.rftools.blocks.storage.RemoteStorageRenderer;
import mcjty.rftools.blocks.teleporter.BeamRenderer;
import mcjty.rftools.blocks.teleporter.MatterTransmitterTileEntity;

public final class ModRenderers {

    public static void init() {
        LogicSlabBlock.RENDERID_LOGICSLAB = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(LogicSlabBlock.RENDERID_LOGICSLAB, new LogicSlabRenderer());

        SolidShieldBlock.RENDERID_SHIELDBLOCK = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(SolidShieldBlock.RENDERID_SHIELDBLOCK, new SolidShieldBlockRenderer());

        ProxyBlock.RENDERID_PROXYBLOCK = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(ProxyBlock.RENDERID_PROXYBLOCK, new ProxyBlockRenderer());

        ModularStorageBlock.RENDERID_MODULARSTORAGE = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(ModularStorageBlock.RENDERID_MODULARSTORAGE, new ModularStorageRenderer());

        RemoteStorageBlock.RENDERID_REMOTESTORAGE = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(RemoteStorageBlock.RENDERID_REMOTESTORAGE, new RemoteStorageRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(EndergenicTileEntity.class, new EndergenicRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(MatterBeamerTileEntity.class, new MatterBeamerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(ScreenTileEntity.class, new ScreenRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(EnvironmentalControllerTileEntity.class, new EnvironmentalControllerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(MatterTransmitterTileEntity.class, new BeamRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(ProxyBlockTileEntity.class, new ProxyBlockTERenderer());
    }
}

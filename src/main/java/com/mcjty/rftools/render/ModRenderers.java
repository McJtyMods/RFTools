package com.mcjty.rftools.render;

import com.mcjty.rftools.blocks.endergen.EndergenicRenderer;
import com.mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import com.mcjty.rftools.blocks.environmental.EnvironmentalControllerRenderer;
import com.mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import com.mcjty.rftools.blocks.logic.LogicSlabBlock;
import com.mcjty.rftools.blocks.logic.LogicSlabRenderer;
import com.mcjty.rftools.blocks.screens.ScreenRenderer;
import com.mcjty.rftools.blocks.screens.ScreenTileEntity;
import com.mcjty.rftools.blocks.shield.SolidShieldBlock;
import com.mcjty.rftools.blocks.shield.SolidShieldBlockRenderer;
import com.mcjty.rftools.blocks.teleporter.BeamRenderer2;
import com.mcjty.rftools.blocks.teleporter.MatterTransmitterTileEntity;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public final class ModRenderers {

    public static void init() {
        LogicSlabBlock.RENDERID_LOGICSLAB = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(LogicSlabBlock.RENDERID_LOGICSLAB, new LogicSlabRenderer());

        SolidShieldBlock.RENDERID_SHIELDBLOCK = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(SolidShieldBlock.RENDERID_SHIELDBLOCK, new SolidShieldBlockRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(EndergenicTileEntity.class, new EndergenicRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(ScreenTileEntity.class, new ScreenRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(EnvironmentalControllerTileEntity.class, new EnvironmentalControllerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(MatterTransmitterTileEntity.class, new BeamRenderer2());
    }
}

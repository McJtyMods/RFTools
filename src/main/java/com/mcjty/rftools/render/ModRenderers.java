package com.mcjty.rftools.render;

import com.mcjty.rftools.blocks.endergen.EndergenicRenderer;
import com.mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import com.mcjty.rftools.blocks.logic.LogicSlabRenderer;
import com.mcjty.rftools.blocks.teleporter.BeamRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public final class ModRenderers {
    public static int RENDERID_BEAM;
    public static int RENDERID_LOGICSLAB;

    public static final void init() {
        RENDERID_BEAM = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(RENDERID_BEAM, new BeamRenderer());
        RENDERID_LOGICSLAB = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(RENDERID_LOGICSLAB, new LogicSlabRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(EndergenicTileEntity.class, new EndergenicRenderer());
    }
}

package com.mcjty.rftools.render;

import com.mcjty.rftools.blocks.endergen.EndergenicRenderer;
import com.mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import com.mcjty.rftools.blocks.logic.LogicSlabBlock;
import com.mcjty.rftools.blocks.logic.LogicSlabRenderer;
import com.mcjty.rftools.blocks.teleporter.BeamRenderer;
import com.mcjty.rftools.blocks.teleporter.TeleportBeamBlock;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public final class ModRenderers {

    public static void init() {
        TeleportBeamBlock.RENDERID_BEAM = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(TeleportBeamBlock.RENDERID_BEAM, new BeamRenderer());

        LogicSlabBlock.RENDERID_LOGICSLAB = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(LogicSlabBlock.RENDERID_LOGICSLAB, new LogicSlabRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(EndergenicTileEntity.class, new EndergenicRenderer());
    }
}

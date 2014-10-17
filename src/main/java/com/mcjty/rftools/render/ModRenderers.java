package com.mcjty.rftools.render;

import com.mcjty.rftools.blocks.teleporter.BeamRenderer;
import cpw.mods.fml.client.registry.RenderingRegistry;

public final class ModRenderers {
    public static int RENDERID_BEAM;

    public static final void init() {
        RENDERID_BEAM = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(RENDERID_BEAM, new BeamRenderer());
    }
}

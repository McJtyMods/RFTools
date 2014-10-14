package com.mcjty.rftools.render;

import com.mcjty.rftools.blocks.teleporter.MatterTransmitterRenderer;
import cpw.mods.fml.client.registry.RenderingRegistry;

public final class ModRenderers {
    public static int RENDERID_TRANSMITTER;

    public static final void init() {
        RENDERID_TRANSMITTER = RenderingRegistry.getNextAvailableRenderId();
//        RenderingRegistry.registerBlockHandler(RENDERID_TRANSMITTER, new MatterTransmitterRenderer());
    }
}

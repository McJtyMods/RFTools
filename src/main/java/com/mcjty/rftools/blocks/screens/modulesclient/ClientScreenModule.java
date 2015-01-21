package com.mcjty.rftools.blocks.screens.modulesclient;

import net.minecraft.client.gui.FontRenderer;

public interface ClientScreenModule {
    public enum TransformMode {
        NONE,
        TEXT,
        ITEM
    }

    TransformMode getTransformMode();

    int getHeight();

    void render(FontRenderer fontRenderer, int currenty);
}

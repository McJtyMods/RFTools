package com.mcjty.rftools.blocks.screens.modules;

import net.minecraft.client.gui.FontRenderer;

public interface ScreenModule {
    public enum TransformMode {
        NONE,
        TEXT,
        ITEM
    }

    TransformMode getTransformMode();

    int getHeight();

    void render(FontRenderer fontRenderer, int currenty);
}

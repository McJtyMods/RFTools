package com.mcjty.rftools.blocks.screens.modules;

public interface ScreenModule {
    public enum TransformMode {
        TEXT,
        ITEM
    }

    TransformMode getTransformMode();

//    int getHeight();
}

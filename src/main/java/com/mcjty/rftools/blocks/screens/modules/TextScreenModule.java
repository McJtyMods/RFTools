package com.mcjty.rftools.blocks.screens.modules;

public class TextScreenModule implements ScreenModule {
    private String line;
    private int color = 0xffffff;

    public TextScreenModule(String line) {
        this.line = line;
    }

    public TextScreenModule color(int color) {
        this.color = color;
        return this;
    }

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }
}

package com.mcjty.rftools.blocks.screens.modules;

import net.minecraft.client.gui.FontRenderer;

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

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty) {
        fontRenderer.drawString(line, 7, currenty, color);
    }
}

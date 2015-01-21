package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.RenderHelper;
import net.minecraft.client.gui.FontRenderer;

public class EnergyBarClientScreenModule implements ClientScreenModule {
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
        RenderHelper.drawHorizontalGradientRect(7 + 10, currenty+1, 7 + 60, currenty + 8, 0xffff0000, 0xff333300);
    }
}

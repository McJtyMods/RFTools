package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class TextClientScreenModule implements ClientScreenModule {
    private String line;
    private int color = 0xffffff;

    public TextClientScreenModule() {
        line = "";
    }

    public TextClientScreenModule(String line) {
        this.line = line;
    }

    public TextClientScreenModule color(int color) {
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

    @Override
    public Panel createGui(Minecraft mc, Gui gui) {
        Panel panel = new Panel(mc, gui).setLayout(new VerticalLayout());
        panel.addChild(new TextField(mc, gui));
        return panel;
    }
}

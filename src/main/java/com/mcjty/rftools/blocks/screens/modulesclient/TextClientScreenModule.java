package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.ColorChoiceLabel;
import com.mcjty.gui.widgets.Label;
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
        panel.addChild(new TextField(mc, gui).setDesiredHeight(20));
        panel.addChild(new Panel(mc, gui).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, gui).setText("Color:")).
                addChild(new ColorChoiceLabel(mc, gui).addColors(0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0xff00ff, 0x00ffff).setDesiredWidth(60).setDesiredHeight(18)).
                setDesiredHeight(20));
        return panel;
    }
}

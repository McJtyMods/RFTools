package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiDimensionEnscriber extends GenericGuiContainer<DimensionEnscriberTileEntity> {
    public static final int ENSCRIBER_WIDTH = 256;
    public static final int ENSCRIBER_HEIGHT = 224;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimensionenscriber.png");

    public GuiDimensionEnscriber(DimensionEnscriberTileEntity dimensionEnscriberTileEntity, DimensionEnscriberContainer container) {
        super(dimensionEnscriberTileEntity, container);

        xSize = ENSCRIBER_WIDTH;
        ySize = ENSCRIBER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Button extractButton = new Button(mc, this).setText("Extract").setLayoutHint(new PositionalLayout.PositionalHint(13, 164, 60, 18));
        Button storeButton = new Button(mc, this).setText("Store").setLayoutHint(new PositionalLayout.PositionalHint(13, 184, 60, 18));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(extractButton).addChild(storeButton);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

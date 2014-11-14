package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiPearlInjector  extends GenericGuiContainer<PearlInjectorTileEntity> {
    public static final int PEARLINJECTOR_WIDTH = 180;
    public static final int PEARLINJECTOR_HEIGHT = 152;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/pearlinjector.png");

    public GuiPearlInjector(PearlInjectorTileEntity pearlInjectorTileEntity, PearlInjectorContainer container) {
        super(pearlInjectorTileEntity, container);

        xSize = PEARLINJECTOR_WIDTH;
        ySize = PEARLINJECTOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

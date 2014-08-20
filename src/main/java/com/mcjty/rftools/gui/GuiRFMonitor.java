package com.mcjty.rftools.gui;

import com.mcjty.gui.*;
import com.mcjty.gui.Panel;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.RFMonitorBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiRFMonitor extends GuiScreen {
    private RFMonitorBlock monitorBlock;
    private Widget toplevel;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack.png");

    /** The X size of the window in pixels. */
    protected int xSize = 256;
    /** The Y size of the window in pixels. */
    protected int ySize = 180;


    public GuiRFMonitor(RFMonitorBlock monitorBlock) {
        this.monitorBlock = monitorBlock;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new HorizontalLayout());
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        toplevel.draw(0, 0);
    }
}

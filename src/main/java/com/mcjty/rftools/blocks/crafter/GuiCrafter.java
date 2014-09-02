package com.mcjty.rftools.blocks.crafter;

import com.mcjty.gui.*;
import com.mcjty.gui.Panel;
import com.mcjty.gui.Window;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.crafter.CrafterContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class GuiCrafter extends GuiContainer {
    public static final int CRAFTER_WIDTH = 256;
    public static final int CRAFTER_HEIGHT = 184;

    private Window window;

    private final CrafterBlockTileEntity crafterBlockTileEntity;
    private final IInventory inventory;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/crafter.png");

    @Override
    public void initGui() {
        super.initGui();

        EnergyBar energyBar = new EnergyBar(mc, this).setVertical().setHandler(crafterBlockTileEntity).setLayoutHint(new PositionalLayout.PositionalHint(20, 80, 16, 40));
        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    public GuiCrafter(CrafterBlockTileEntity crafterBlockTileEntity, CrafterContainer container) {
        super(container);
        this.crafterBlockTileEntity = crafterBlockTileEntity;
        xSize = CRAFTER_WIDTH;
        ySize = CRAFTER_HEIGHT;
        inventory = container.getContainerInventory();
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        String s = this.inventory.hasCustomInventoryName() ? this.inventory.getInventoryName() : inventory.getInventoryName();
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 4210752);
//        this.fontRendererObj.drawString("container.inventory", 26, this.ySize - 96 + 4, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

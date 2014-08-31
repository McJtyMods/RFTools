package com.mcjty.rftools.gui;

import com.mcjty.gui.Panel;
import com.mcjty.gui.Widget;
import com.mcjty.gui.Window;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.CrafterContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class GuiCrafter extends GuiContainer {
    private float xSize_lo;
    private float ySize_lo;

    public static final int CRAFTER_WIDTH = 256;
    public static final int CRAFTER_HEIGHT = 184;

    private Window window;

    private final IInventory inventory;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/crafter.png");

    @Override
    public void initGui() {
        super.initGui();
        Widget toplevel = new Panel(mc, this).setBackground(iconLocation);//setFilledRectThickness(2);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
    }

    public GuiCrafter(CrafterContainer container) {
        super(container);
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
        this.xSize_lo = par1;
        this.ySize_lo = par2;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        String s = this.inventory.hasCustomInventoryName() ? this.inventory.getInventoryName() : inventory.getInventoryName();
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 0, 4210752);
        this.fontRendererObj.drawString("container.inventory", 26, this.ySize - 96 + 4, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
//        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//        this.mc.getTextureManager().bindTexture(iconLocation);
//        int k = (this.width - this.xSize) / 2;
//        int l = (this.height - this.ySize) / 2;
//        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
//        GuiHelper.drawPlayerModel(k + 51, l + 75, 30, (k + 51) - this.xSize_lo, (l + 75 - 50) - this.ySize_lo, this.mc.thePlayer);
        window.draw();
    }
}

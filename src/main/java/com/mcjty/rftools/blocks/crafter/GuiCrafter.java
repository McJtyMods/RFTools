package com.mcjty.rftools.blocks.crafter;

import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.EnergyBar;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.Window;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiCrafter extends GuiContainer {
    public static final int CRAFTER_WIDTH = 256;
    public static final int CRAFTER_HEIGHT = 216;

    private Window window;
    private EnergyBar energyBar;

    private final CrafterBlockTileEntity crafterBlockTileEntity;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/crafter.png");

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = crafterBlockTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 10, 16, 40));
        energyBar.setValue(crafterBlockTileEntity.getCurrentRF());
        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    public GuiCrafter(CrafterBlockTileEntity crafterBlockTileEntity, CrafterContainer container) {
        super(container);
        this.crafterBlockTileEntity = crafterBlockTileEntity;
        crafterBlockTileEntity.setOldRF(-1);
        crafterBlockTileEntity.setCurrentRF(crafterBlockTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = CRAFTER_WIDTH;
        ySize = CRAFTER_HEIGHT;
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
//        String s = this.inventory.hasCustomInventoryName() ? this.inventory.getInventoryName() : inventory.getInventoryName();
//        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 4210752);
//        this.fontRendererObj.drawString("container.inventory", 26, this.ySize - 96 + 4, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        int currentRF = crafterBlockTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
    }
}

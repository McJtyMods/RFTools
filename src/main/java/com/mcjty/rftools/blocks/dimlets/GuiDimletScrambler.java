package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.EnergyBar;
import com.mcjty.gui.widgets.ImageLabel;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiDimletScrambler extends GenericGuiContainer<DimletScramblerTileEntity> {
    public static final int SCRAMBLER_WIDTH = 180;
    public static final int SCRAMBLER_HEIGHT = 152;

    private EnergyBar energyBar;
    private ImageLabel progressIcon;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimletscrambler.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiDimletScrambler(DimletScramblerTileEntity pearlInjectorTileEntity, DimletScramblerContainer container) {
        super(pearlInjectorTileEntity, container);
        pearlInjectorTileEntity.setCurrentRF(pearlInjectorTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = SCRAMBLER_WIDTH;
        ySize = SCRAMBLER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        progressIcon = new ImageLabel(mc, this).setImage(iconGuiElements, 4 * 16, 16);
        progressIcon.setLayoutHint(new PositionalLayout.PositionalHint(64, 24, 16, 16));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).addChild(progressIcon);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer();
        tileEntity.requestScramblingFromServer();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        int scrambling = tileEntity.getScrambling();
        if (scrambling == 0) {
            progressIcon.setImage(iconGuiElements, 4 * 16, 16);
        } else {
            progressIcon.setImage(iconGuiElements, (scrambling % 4) * 16, 16);
        }

        window.draw();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer();
        tileEntity.requestScramblingFromServer();
    }
}

package com.mcjty.rftools.blocks.spawner;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.EnergyBar;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiSpawner extends GenericGuiContainer<SpawnerTileEntity> {
    public static final int SPAWNER_WIDTH = 180;
    public static final int SPAWNER_HEIGHT = 152;

    private EnergyBar energyBar;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/spawner.png");

    public GuiSpawner(SpawnerTileEntity spawnerTileEntity, SpawnerContainer container) {
        super(spawnerTileEntity, container);
        spawnerTileEntity.setCurrentRF(spawnerTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = SPAWNER_WIDTH;
        ySize = SPAWNER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        tileEntity.requestRfFromServer();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        int currentRF = tileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer();
    }
}

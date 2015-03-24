package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.RedstoneMode;
import com.mcjty.rftools.network.Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiDimensionBuilder extends GenericGuiContainer<DimensionBuilderTileEntity> {
    public static final int BUILDER_WIDTH = 180;
    public static final int BUILDER_HEIGHT = 152;

    private EnergyBar energyBar;
    private ImageLabel stages;
    private Label percentage;
    private ImageChoiceLabel redstoneMode;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimensionbuilder.png");
    private static final ResourceLocation iconStages = new ResourceLocation(RFTools.MODID, "textures/gui/dimensionbuilderstages.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiDimensionBuilder(DimensionBuilderTileEntity dimensionBuilderTileEntity, DimensionBuilderContainer container) {
        super(dimensionBuilderTileEntity, container);
        dimensionBuilderTileEntity.setCurrentRF(dimensionBuilderTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = BUILDER_WIDTH;
        ySize = BUILDER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        stages = new ImageLabel(mc, this).setImage(iconStages, 0, 0);
        stages.setLayoutHint(new PositionalLayout.PositionalHint(61, 9, 48, 48));

        percentage = new Label(mc, this).setText("0%");
        percentage.setLayoutHint(new PositionalLayout.PositionalHint(115, 25, 40, 16));

        initRedstoneMode();

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(stages).addChild(percentage).addChild(redstoneMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer();
        tileEntity.requestBuildingPercentage();
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeRedstoneMode();
                    }
                }).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(150, 46, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRedstoneMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRedstoneMode(RedstoneMode.values()[redstoneMode.getCurrentChoice()]);
        sendServerCommand(DimensionBuilderTileEntity.CMD_RSMODE, new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoice()].getDescription()));
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        int pct = tileEntity.getBuildPercentage();
        int x = ((pct-1)/4) % 5;
        int y = ((pct-1)/4) / 5;
        stages.setImage(iconStages, x * 48, y * 48);
        percentage.setText(pct + "%");

        window.draw();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer();
        tileEntity.requestBuildingPercentage();
    }
}

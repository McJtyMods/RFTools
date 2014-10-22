package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.EmptyContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiEndergenic extends GuiContainer {
    public static final int ENDERGENIC_WIDTH = 180;
    public static final int ENDERGENIC_HEIGHT = 50;

    private final EndergenicTileEntity endergenicTileEntity;

    private Window window;
    private EnergyBar energyBar;
    private TextField averageRF;

    public GuiEndergenic(EndergenicTileEntity endergenicTileEntity, EmptyContainer<EndergenicTileEntity> container) {
        super(container);
        this.endergenicTileEntity = endergenicTileEntity;
        endergenicTileEntity.setOldRF(-1);
        endergenicTileEntity.setCurrentRF(endergenicTileEntity.getEnergyStored(ForgeDirection.DOWN));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - ENDERGENIC_WIDTH) / 2;
        int l = (this.height - ENDERGENIC_HEIGHT) / 2;

        int maxEnergyStored = endergenicTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setHorizontal().setDesiredHeight(12).setMaxValue(maxEnergyStored).setShowText(true);
        energyBar.setValue(endergenicTileEntity.getCurrentRF());

        Label avgLabel = new Label(mc, this).setText("Average RF:");
        averageRF = new TextField(mc, this).setText("0 RF/tick").setDesiredWidth(90).setDesiredHeight(15);
        Panel averageRFPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(avgLabel).addChild(averageRF);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(energyBar).addChild(averageRFPanel);
        toplevel.setBounds(new Rectangle(k, l, ENDERGENIC_WIDTH, ENDERGENIC_HEIGHT));
        window = new com.mcjty.gui.Window(this, toplevel);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x - guiLeft, y - guiTop, mc.fontRenderer);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        int currentRF = endergenicTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        int avgRF = endergenicTileEntity.getAverageRF();
        averageRF.setText(avgRF + " RF/tick");
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

}

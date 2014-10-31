package com.mcjty.rftools.blocks.shield;

import com.mcjty.gui.Window;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.EnergyBar;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;

public class GuiShield extends GuiContainer {
    public static final int SHIELD_WIDTH = 256;
    public static final int SHIELD_HEIGHT = 224;

    private Window window;
    private EnergyBar energyBar;

    private final ShieldTileEntity shieldTileEntity;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/shieldprojector.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiShield(ShieldTileEntity shieldTileEntity, ShieldContainer container) {
        super(container);
        this.shieldTileEntity = shieldTileEntity;
        shieldTileEntity.setOldRF(-1);
        shieldTileEntity.setCurrentRF(shieldTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = SHIELD_WIDTH;
        ySize = SHIELD_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = shieldTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(12, 141, 8, 76)).setShowText(false);
        energyBar.setValue(shieldTileEntity.getCurrentRF());

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        int currentRF = shieldTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
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
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }
}
package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.EmptyContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRequestIntegerFromServer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiEndergenic extends GuiContainer {
    public static final int ENDERGENIC_WIDTH = 190;
    public static final int ENDERGENIC_HEIGHT = 108;

    private final EndergenicTileEntity endergenicTileEntity;

    private Window window;
    private EnergyBar energyBar;
    private TextField lastRfPerTick;
    private TextField lastLostPearls;
    private TextField lastLaunchedPearls;
    private TextField lastOpportunities;

    public static int fromServer_lastRfPerTick = 0;
    public static int fromServer_lastPearlsLost = 0;
    public static int fromServer_lastPearlsLaunched = 0;
    public static int fromServer_lastPearlOpportunities = 0;


    private int timer = 10;

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

        Label descriptionLabel = new Label(mc, this).setText("Averages over last 5 seconds:").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);

        lastRfPerTick = new TextField(mc, this).setText("0 RF/tick").setDesiredWidth(90).setDesiredHeight(15);
        Panel p1 = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Gain:").setDesiredWidth(70)).addChild(lastRfPerTick);

        lastLostPearls = new TextField(mc, this).setText("0").setDesiredWidth(90).setDesiredHeight(15);
        Panel p2 = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Lost:").setDesiredWidth(70)).addChild(lastLostPearls);

        lastLaunchedPearls = new TextField(mc, this).setText("0").setDesiredWidth(90).setDesiredHeight(15);
        Panel p3 = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Launched:").setDesiredWidth(70)).addChild(lastLaunchedPearls);

        lastOpportunities = new TextField(mc, this).setText("0").setDesiredWidth(90).setDesiredHeight(15);
        Panel p4 = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Chances:").setDesiredWidth(70)).addChild(lastOpportunities);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(energyBar).
                addChild(descriptionLabel).
                addChild(p1).addChild(p2).addChild(p3).addChild(p4);
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

        checkStats();

        lastRfPerTick.setText(fromServer_lastRfPerTick + " RF/tick");
        lastLostPearls.setText(fromServer_lastPearlsLost + " pearls");
        lastLaunchedPearls.setText(fromServer_lastPearlsLaunched + " pearls");
        lastOpportunities.setText(fromServer_lastPearlOpportunities + " times");

    }

    private void checkStats() {
        timer--;
        if (timer <= 0) {
            timer = 20;
            PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(endergenicTileEntity.xCoord, endergenicTileEntity.yCoord, endergenicTileEntity.zCoord,
                    EndergenicTileEntity.CMD_GETSTAT_RF, EndergenicTileEntity.CLIENTCMD_GETSTAT_RF));
            PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(endergenicTileEntity.xCoord, endergenicTileEntity.yCoord, endergenicTileEntity.zCoord,
                    EndergenicTileEntity.CMD_GETSTAT_LOST, EndergenicTileEntity.CLIENTCMD_GETSTAT_LOST));
            PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(endergenicTileEntity.xCoord, endergenicTileEntity.yCoord, endergenicTileEntity.zCoord,
                    EndergenicTileEntity.CMD_GETSTAT_LAUNCHED, EndergenicTileEntity.CLIENTCMD_GETSTAT_LAUNCHED));
            PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(endergenicTileEntity.xCoord, endergenicTileEntity.yCoord, endergenicTileEntity.zCoord,
                    EndergenicTileEntity.CMD_GETSTAT_OPPORTUNITIES, EndergenicTileEntity.CLIENTCMD_GETSTAT_OPPORTUNITIES));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

}

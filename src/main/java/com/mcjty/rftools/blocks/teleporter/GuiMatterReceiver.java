package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiMatterReceiver extends GuiContainer {
    public static final int MATTER_WIDTH = 140;
    public static final int MATTER_HEIGHT = 50;

    private Window window;
    private EnergyBar energyBar;

    private final MatterReceiverTileEntity matterReceiverTileEntity;

    public GuiMatterReceiver(MatterReceiverTileEntity matterReceiverTileEntity, MatterReceiverContainer container) {
        super(container);
        this.matterReceiverTileEntity = matterReceiverTileEntity;
        matterReceiverTileEntity.setOldRF(-1);
        matterReceiverTileEntity.setCurrentRF(matterReceiverTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = MATTER_WIDTH;
        ySize = MATTER_HEIGHT;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - MATTER_WIDTH) / 2;
        int l = (this.height - MATTER_HEIGHT) / 2;

        int maxEnergyStored = matterReceiverTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setHorizontal().setDesiredHeight(12).setMaxValue(maxEnergyStored).setShowText(true);
        energyBar.setValue(matterReceiverTileEntity.getCurrentRF());

        TextField textField = new TextField(mc, this).setTooltips("Use this name to", "identify this receiver", "in the dialer").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                setReceiverName(newText);
            }
        });
        textField.setText(matterReceiverTileEntity.getName());
        Panel bottemPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Name:")).addChild(textField);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setHorizontalMargin(6).setVerticalMargin(6)).
                addChild(energyBar).addChild(bottemPanel);
        toplevel.setBounds(new Rectangle(k, l, MATTER_WIDTH, MATTER_HEIGHT));
        window = new com.mcjty.gui.Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);
    }

    private void setReceiverName(String text) {
        PacketHandler.INSTANCE.sendToServer(new PacketSetReceiverName(matterReceiverTileEntity.xCoord, matterReceiverTileEntity.yCoord, matterReceiverTileEntity.zCoord,
                text));
    }


    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
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
        int currentRF = matterReceiverTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!window.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }
}

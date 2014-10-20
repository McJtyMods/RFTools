package com.mcjty.rftools.blocks.relay;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.ChoiceLabel;
import com.mcjty.gui.widgets.ImageLabel;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketServerCommand;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiRelay extends GuiScreen {
    public static final int RELAY_WIDTH = 140;
    public static final int RELAY_HEIGHT = 50;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private Window window;
    private ChoiceLabel choicesOn;
    private ChoiceLabel choicesOff;

    private final RelayTileEntity relayTileEntity;

    public GuiRelay(RelayTileEntity relayTileEntity) {
        this.relayTileEntity = relayTileEntity;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - RELAY_WIDTH) / 2;
        int l = (this.height - RELAY_HEIGHT) / 2;

        ImageLabel redstoneOff = new ImageLabel(mc, this).setImage(iconGuiElements, 16, 0);
        redstoneOff.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal off");
        choicesOff = new ChoiceLabel(mc, this).addChoices("0", "100", "500", "1000", "5000", "10000", "20000").setDesiredWidth(100).
                setTooltips("Amount of RF to output", "when redstone is off").setDesiredHeight(16).addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeRfOutput();
            }
        });
        choicesOff.setChoice(Integer.toString(relayTileEntity.getRfOff()));
        Panel panelOff = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(redstoneOff).addChild(choicesOff);

        ImageLabel redstoneOn = new ImageLabel(mc, this).setImage(iconGuiElements, 32, 0);
        redstoneOn.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal on");
        choicesOn = new ChoiceLabel(mc, this).addChoices("0", "100", "500", "1000", "5000", "10000", "20000").setDesiredWidth(100).
            setTooltips("Amount of RF to output", "when redstone is on").setDesiredHeight(16).addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeRfOutput();
            }
        });
        choicesOn.setChoice(Integer.toString(relayTileEntity.getRfOn()));
        Panel panelOn = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(redstoneOn).addChild(choicesOn);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(panelOff).addChild(panelOn);
        toplevel.setBounds(new Rectangle(k, l, RELAY_WIDTH, RELAY_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void changeRfOutput() {
        int onValue = new Integer(choicesOn.getCurrentChoice());
        int offValue = new Integer(choicesOff.getCurrentChoice());
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(relayTileEntity.xCoord, relayTileEntity.yCoord, relayTileEntity.zCoord,
                RelayTileEntity.CMD_SETTINGS,
                new Argument("on", onValue), new Argument("off", offValue)));
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
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        window.draw();
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x, y, mc.fontRenderer);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }
}

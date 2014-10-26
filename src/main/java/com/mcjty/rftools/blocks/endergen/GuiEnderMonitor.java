package com.mcjty.rftools.blocks.endergen;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.ChoiceLabel;
import com.mcjty.gui.widgets.Label;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiEnderMonitor extends GuiScreen {
    public static final int MONITOR_WIDTH = 140;
    public static final int MONITOR_HEIGHT = 30;

    private static final String MODE_LOSTPEARL = "Lost Pearl";
    private static final String MODE_PEARLFIRED = "Pearl Fired";
    private static final String MODE_PEARLARRIVED = "Pearl Arrived";

    private static final Map<String,Integer> modeToMode = new HashMap<String, Integer>();
    private static final List<String> modes = new ArrayList<String>();
    static {
        modeToMode.put(MODE_LOSTPEARL, EnderMonitorTileEntity.MODE_LOSTPEARL);
        modeToMode.put(MODE_PEARLFIRED, EnderMonitorTileEntity.MODE_PEARLFIRED);
        modeToMode.put(MODE_PEARLARRIVED, EnderMonitorTileEntity.MODE_PEARLARRIVED);

        modes.add(MODE_LOSTPEARL);
        modes.add(MODE_PEARLFIRED);
        modes.add(MODE_PEARLARRIVED);
    }

    private Window window;
    private ChoiceLabel mode;

    private final EnderMonitorTileEntity enderMonitorTileEntity;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiEnderMonitor(EnderMonitorTileEntity enderMonitorTileEntity) {
        this.enderMonitorTileEntity = enderMonitorTileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - MONITOR_WIDTH) / 2;
        int l = (this.height - MONITOR_HEIGHT) / 2;

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        Label label = new Label(mc, this).setText("Mode:");
        mode = new ChoiceLabel(mc, this).addChoices(MODE_LOSTPEARL, MODE_PEARLFIRED, MODE_PEARLARRIVED).addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeMode();
            }
        }).setDesiredHeight(13).setDesiredWidth(80);
        mode.setChoiceTooltip(MODE_LOSTPEARL, "Send a redstone pulse when a", "pearl is lost");
        mode.setChoiceTooltip(MODE_PEARLFIRED, "Send a redstone pulse when a", "pearl is fired");
        mode.setChoiceTooltip(MODE_PEARLARRIVED, "Send a redstone pulse when a", "pearl arrives");
        mode.setChoice(modes.get(enderMonitorTileEntity.getMode()));

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(label).addChild(mode);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(k, l, MONITOR_WIDTH, MONITOR_HEIGHT));
        window = new Window(this, toplevel);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void changeMode() {
        Integer newMode = modeToMode.get(mode.getCurrentChoice());
        enderMonitorTileEntity.setMode(newMode);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(enderMonitorTileEntity.xCoord, enderMonitorTileEntity.yCoord, enderMonitorTileEntity.zCoord,
                EnderMonitorTileEntity.CMD_MODE,
                new Argument("mode", newMode)));
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

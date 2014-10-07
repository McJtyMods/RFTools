package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.EmptyContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.DefaultSelectionEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketServerCommand;
import com.mcjty.varia.Coordinate;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiDialingDevice extends GuiContainer {

    public static final int DIALER_WIDTH = 256;
    public static final int DIALER_HEIGHT = 224;

    private Window window;

    private EnergyBar energyBar;
    private WidgetList transmitterList;
    private WidgetList receiverList;
    private final DialingDeviceTileEntity dialingDeviceTileEntity;

    // A copy of the receivers we're currently showing.
    private List<TeleportDestination> receivers = null;

    // A copy of the transmitters we're currently showing.
    private List<TransmitterInfo> transmitters = null;


    public GuiDialingDevice(DialingDeviceTileEntity dialingDeviceTileEntity, EmptyContainer<DialingDeviceTileEntity> container) {
        super(container);
        this.dialingDeviceTileEntity = dialingDeviceTileEntity;
        dialingDeviceTileEntity.setOldRF(-1);
        dialingDeviceTileEntity.setCurrentRF(dialingDeviceTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = DIALER_WIDTH;
        ySize = DIALER_HEIGHT;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - DIALER_WIDTH) / 2;
        int l = (this.height - DIALER_HEIGHT) / 2;

        int maxEnergyStored = dialingDeviceTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setHorizontal().setDesiredHeight(12).setMaxValue(maxEnergyStored).setShowText(true);
        energyBar.setValue(dialingDeviceTileEntity.getCurrentRF());

        transmitterList = new WidgetList(mc, this).setFilledRectThickness(1).setDesiredHeight(90);
        receiverList = new WidgetList(mc, this).setFilledRectThickness(1).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void doubleClick(Widget parent, int index) {
                testTeleport(index);
            }
        });

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).
                addChild(energyBar).addChild(new Label(mc, this).setText("Transmitters:")).addChild(transmitterList).
                addChild(new Label(mc, this).setText("Receivers:")).addChild(receiverList);
        toplevel.setBounds(new Rectangle(k, l, DIALER_WIDTH, DIALER_HEIGHT));
        window = new com.mcjty.gui.Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        requestReceivers();
        requestTransmitters();
    }

    private void testTeleport(int index) {
        TeleportDestination destination = receivers.get(index);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(dialingDeviceTileEntity.xCoord, dialingDeviceTileEntity.yCoord, dialingDeviceTileEntity.zCoord,
                DialingDeviceTileEntity.CMD_TELEPORT,
                new Argument("dest", destination.getCoordinate()),
                new Argument("dim", destination.getDimension()),
                new Argument("player", mc.thePlayer.getDisplayName())));
//        PacketHandler.INSTANCE.sendToServer(new PacketStartTeleport(dialingDeviceTileEntity.xCoord, dialingDeviceTileEntity.yCoord, dialingDeviceTileEntity.zCoord,
//                destination, mc.thePlayer.getDisplayName()));
    }

    private void requestReceivers() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetReceivers(dialingDeviceTileEntity.xCoord, dialingDeviceTileEntity.yCoord, dialingDeviceTileEntity.zCoord));
    }

    private void requestTransmitters() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetTransmitters(dialingDeviceTileEntity.xCoord, dialingDeviceTileEntity.yCoord, dialingDeviceTileEntity.zCoord));
    }

    private void populateReceivers() {
        List<TeleportDestination> newReceivers = dialingDeviceTileEntity.getReceivers();
        if (newReceivers == null) {
            return;
        }
        if (newReceivers.equals(receivers)) {
            return;
        }

        receivers = new ArrayList<TeleportDestination>(newReceivers);
        receiverList.removeChildren();

        for (TeleportDestination destination : receivers) {
            Coordinate coordinate = destination.getCoordinate();

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new Label(mc, this).setText(destination.getName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(120));
            panel.addChild(new Label(mc, this).setDynamic(true).setText(coordinate.toString()));
            receiverList.addChild(panel);
        }
    }

    private void populateTransmitters() {
        List<TransmitterInfo> mewTramsmitters = dialingDeviceTileEntity.getTransmitters();
        if (mewTramsmitters == null) {
            return;
        }
        if (mewTramsmitters.equals(transmitters)) {
            return;
        }

        transmitters = new ArrayList<TransmitterInfo>(mewTramsmitters);
        transmitterList.removeChildren();

        for (TransmitterInfo transmitterInfo : transmitters) {
            Coordinate coordinate = transmitterInfo.getCoordinate();

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new Label(mc, this).setText(transmitterInfo.getName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(120));
            panel.addChild(new Label(mc, this).setDynamic(true).setText(coordinate.toString()));
            transmitterList.addChild(panel);
        }
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
        populateReceivers();
        populateTransmitters();
        window.draw();
        int currentRF = dialingDeviceTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!window.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }
}

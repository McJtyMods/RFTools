package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.EmptyContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.DefaultSelectionEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRequestIntegerFromServer;
import com.mcjty.rftools.network.PacketServerCommand;
import com.mcjty.varia.Coordinate;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiDialingDevice extends GuiContainer {

    public static final int DIALER_WIDTH = 256;
    public static final int DIALER_HEIGHT = 224;

    private static final ResourceLocation iconDialOn = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private Window window;

    private EnergyBar energyBar;
    private WidgetList transmitterList;
    private WidgetList receiverList;
    private Button dialButton;
    private Button interruptButton;
    private Button statusButton;
    private Label statusLabel;
    private final DialingDeviceTileEntity dialingDeviceTileEntity;

    private MatterTransmitterTileEntity lastDialedTransmitter = null;
    private boolean lastCheckedReceiver = false;

    // A copy of the receivers we're currently showing.
    private List<TeleportDestination> receivers = null;

    // A copy of the transmitters we're currently showing.
    private List<TransmitterInfo> transmitters = null;

    private int listDirty = 0;


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

        transmitterList = new WidgetList(mc, this).setFilledRectThickness(1).setDesiredHeight(60).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                lastDialedTransmitter = null;
                lastCheckedReceiver = false;
                selectReceiverFromTransmitter();
            }
        });
        receiverList = new WidgetList(mc, this).setFilledRectThickness(1).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                lastDialedTransmitter = null;
                lastCheckedReceiver = false;
            }
        });

        dialButton = new Button(mc, this).setText("Dial").setTooltips("Start a connection between", "the selected transmitter", "and the selected receiver").
                setDesiredHeight(14).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        dial();
                    }
                });
        interruptButton = new Button(mc, this).setText("Interrupt").setTooltips("Interrupt a connection", "for the selected transmitter").
                setDesiredHeight(14).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        interruptDial();
                    }
                });
        statusButton = new Button(mc, this).setText("Check").setTooltips("Check the status of", "the selected receiver").
                setDesiredHeight(14).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        checkStatus();
                    }
                });
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(dialButton).addChild(interruptButton).addChild(statusButton).setDesiredHeight(16);

        statusLabel = new Label(mc, this);
        statusLabel.setDesiredWidth(180).setDesiredHeight(14).setFilledRectThickness(1);
        Panel statusPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Status")).addChild(statusLabel).setDesiredHeight(16);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).
                addChild(energyBar).addChild(new Label(mc, this).setText("Transmitters:")).addChild(transmitterList).
                addChild(new Label(mc, this).setText("Receivers:")).addChild(receiverList).addChild(buttonPanel).addChild(statusPanel);
        toplevel.setBounds(new Rectangle(k, l, DIALER_WIDTH, DIALER_HEIGHT));
        window = new com.mcjty.gui.Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        listDirty = 0;
        lastDialedTransmitter = null;
        lastCheckedReceiver = false;

        requestReceivers();
        requestTransmitters();
    }

    private void setStatusError(String message) {
        statusLabel.setText(message);
        statusLabel.setColor(0xffff0000);
    }

    private void setStatusMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setColor(0xff000000);
    }

    private void checkStatus() {
        int receiverSelected = receiverList.getSelected();
        if (receiverSelected == -1) {
            return; // Shouldn't happen. Just to be sure.
        }
        TeleportDestination destination = receivers.get(receiverSelected);
        Coordinate c = destination.getCoordinate();
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(dialingDeviceTileEntity.xCoord, dialingDeviceTileEntity.yCoord, dialingDeviceTileEntity.zCoord,
                DialingDeviceTileEntity.CMD_CHECKSTATUS,
                DialingDeviceTileEntity.CLIENTCMD_STATUS, new Argument("c", c), new Argument("dim", destination.getDimension())));

        lastCheckedReceiver = true;
        listDirty = 0;
    }

    private void showStatus(int dialResult) {
        if ((dialResult & DialingDeviceTileEntity.DIAL_POWER_LOW_MASK) != 0) {
            setStatusError("Transmitter power low!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK) != 0) {
            setStatusError("Invalid destination!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK) != 0) {
            setStatusError("Receiver blocked!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_TRANSMITTER_BLOCKED_MASK) != 0) {
            setStatusError("Transmitter blocked!");
            return;
        }
        setStatusMessage("Dial ok!");
    }

    private void selectReceiverFromTransmitter() {
        receiverList.setSelected(-1);
        TeleportDestination destination = getSelectedTransmitterDestination();
        if (destination == null) {
            return;
        }
        int i = 0;
        for (TeleportDestination receiver : receivers) {
            if (receiver.getDimension() == destination.getDimension() && receiver.getCoordinate().equals(destination.getCoordinate())) {
                receiverList.setSelected(i);
                return;
            }
            i++;
        }
    }

    private void dial() {
        int transmitterSelected = transmitterList.getSelected();
        int receiverSelected = receiverList.getSelected();
        if (transmitterSelected == -1 || receiverSelected == -1) {
            return; // Shouldn't happen. Just to be sure.
        }
        TransmitterInfo transmitterInfo = transmitters.get(transmitterSelected);
        TeleportDestination destination = receivers.get(receiverSelected);
        Coordinate c = transmitterInfo.getCoordinate();
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(c.getX(), c.getY(), c.getZ(), MatterTransmitterTileEntity.CMD_DIAL,
                MatterTransmitterTileEntity.CLIENTCMD_DIAL, new Argument("c", destination.getCoordinate()), new Argument("dim", destination.getDimension())));

        try {
            lastDialedTransmitter = (MatterTransmitterTileEntity) mc.theWorld.getTileEntity(c.getX(), c.getY(), c.getZ());
        } catch (Exception e) {
            lastDialedTransmitter = null;   // Something went wrong
            e.printStackTrace();
        }

        listDirty = 0;
    }

    private void interruptDial() {
        int transmitterSelected = transmitterList.getSelected();
        if (transmitterSelected == -1) {
            return; // Shouldn't happen. Just to be sure.
        }
        TransmitterInfo transmitterInfo = transmitters.get(transmitterSelected);
        Coordinate c = transmitterInfo.getCoordinate();
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(c.getX(), c.getY(), c.getZ(), MatterTransmitterTileEntity.CMD_DIAL,
                MatterTransmitterTileEntity.CLIENTCMD_DIAL, new Argument("c", (Coordinate) null), new Argument("dim", 0)));
        listDirty = 0;

        lastCheckedReceiver = false;
        lastDialedTransmitter = null;
        setStatusMessage("Interrupted");
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

            String dimName = DimensionManager.getProvider(destination.getDimension()).getDimensionName();

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new Label(mc, this).setText(destination.getName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(90));
            panel.addChild(new Label(mc, this).setDynamic(true).setText(coordinate.toString()));
            panel.addChild(new Label(mc, this).setText(dimName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(60));
            receiverList.addChild(panel);
        }
    }

    private TeleportDestination getSelectedTransmitterDestination() {
        int transmitterSelected = transmitterList.getSelected();
        if (transmitterSelected == -1) {
            return null;
        }
        TransmitterInfo transmitterInfo = transmitters.get(transmitterSelected);
        TeleportDestination destination = transmitterInfo.getTeleportDestination();
        if (destination.isValid()) {
            return destination;
        } else {
            return null;
        }
    }

    private void populateTransmitters() {
        List<TransmitterInfo> newTransmitters = dialingDeviceTileEntity.getTransmitters();
        if (newTransmitters == null) {
            return;
        }
        if (newTransmitters.equals(transmitters)) {
            return;
        }

        transmitters = new ArrayList<TransmitterInfo>(newTransmitters);
        transmitterList.removeChildren();

        for (TransmitterInfo transmitterInfo : transmitters) {
            Coordinate coordinate = transmitterInfo.getCoordinate();
            TeleportDestination destination = transmitterInfo.getTeleportDestination();

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new Label(mc, this).setText(transmitterInfo.getName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(120));
            panel.addChild(new Label(mc, this).setDynamic(true).setText(coordinate.toString()));
            panel.addChild(new ImageLabel(mc, this).setImage(iconDialOn, destination.isValid() ? 80 : 96, 0).setDesiredWidth(16));
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
        requestListsIfNeeded();

        populateReceivers();
        populateTransmitters();

        if (lastDialedTransmitter != null) {
            int dialResult = lastDialedTransmitter.getDialResult();
            showStatus(dialResult);
        } else if (lastCheckedReceiver) {
            int dialResult = dialingDeviceTileEntity.getReceiverStatus();
            showStatus(dialResult);
        } else {
            statusLabel.setText("");
        }

        enableButtons();

        window.draw();
        int currentRF = dialingDeviceTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestReceivers();
            requestTransmitters();
            listDirty = 20;
        }
    }

    private void enableButtons() {
        int transmitterSelected = transmitterList.getSelected();
        int receiverSelected = receiverList.getSelected();
        if (transmitterSelected != -1 && receiverSelected != -1) {
            dialButton.setEnabled(true);
        } else {
            dialButton.setEnabled(false);
        }
        if (transmitterSelected != -1) {
            TeleportDestination destination = getSelectedTransmitterDestination();
            interruptButton.setEnabled(destination != null);
        } else {
            interruptButton.setEnabled(false);
        }
        if (receiverSelected != -1) {
            statusButton.setEnabled(true);
        } else {
            statusButton.setEnabled(false);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!window.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }
}

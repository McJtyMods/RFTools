package mcjty.rftools.blocks.teleporter;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.events.ChoiceEvent;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketRequestIntegerFromServer;
import mcjty.lib.varia.Coordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storagemonitor.StorageScannerConfiguration;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiDialingDevice extends GenericGuiContainer<DialingDeviceTileEntity> {

    public static final int DIALER_WIDTH = 256;
    public static final int DIALER_HEIGHT = 224;

    private static final ResourceLocation guielements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private EnergyBar energyBar;
    private WidgetList transmitterList;
    private WidgetList receiverList;
    private Button dialButton;
    private Button dialOnceButton;
    private Button interruptButton;
    private ImageChoiceLabel favoriteButton;
    private Button statusButton;
    private Label statusLabel;

    private boolean analyzerAvailable = false;
    private boolean lastDialedTransmitter = false;
    private boolean lastCheckedReceiver = false;

    public static int fromServer_receiverStatus = -1;
    public static int fromServer_dialResult = -1;
    public static List<TeleportDestinationClientInfo> fromServer_receivers = null;
    public static List<TransmitterInfo> fromServer_transmitters = null;

    // A copy of the receivers we're currently showing.
    private List<TeleportDestinationClientInfo> receivers = null;
    private boolean receiversFiltered = false;

    // A copy of the transmitters we're currently showing.
    private List<TransmitterInfo> transmitters = null;

    private int listDirty = 0;


    public GuiDialingDevice(DialingDeviceTileEntity dialingDeviceTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, dialingDeviceTileEntity, container, RFTools.GUI_MANUAL_MAIN, "tpdialer");
        dialingDeviceTileEntity.setCurrentRF(dialingDeviceTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = DIALER_WIDTH;
        ySize = DIALER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setHorizontal().setDesiredWidth(80).setDesiredHeight(12).setMaxValue(maxEnergyStored).setShowText(true);
        energyBar.setValue(tileEntity.getCurrentRF());

        Panel transmitterPanel = setupTransmitterPanel();
        Panel receiverPanel = setupReceiverPanel();

        dialButton = new Button(mc, this).setText("Dial").setTooltips("Start a connection between", "the selected transmitter", "and the selected receiver").
                setDesiredHeight(14).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        dial(false);
                    }
                });
        dialOnceButton = new Button(mc, this).setText("Dial Once").setTooltips("Dial a connection for a", "single teleport").
                setDesiredHeight(14).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        dial(true);
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
        favoriteButton = new ImageChoiceLabel(mc, this).addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeShowFavorite();
            }
        }).setDesiredWidth(10).setDesiredHeight(10);
        favoriteButton.addChoice("No", "Unfavorited receiver", guielements, 131, 19);
        favoriteButton.addChoice("Yes", "Favorited receiver", guielements, 115, 19);
        favoriteButton.setCurrentChoice(tileEntity.isShowOnlyFavorites() ? 1 : 0);

        analyzerAvailable =  DialingDeviceTileEntity.isDestinationAnalyzerAvailable(mc.theWorld, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
        statusButton = new Button(mc, this).setText("Check").
                setDesiredHeight(14).
                setEnabled(analyzerAvailable).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        checkStatus();
                    }
                });
        if (analyzerAvailable) {
            statusButton.setTooltips("Check the status of", "the selected receiver");
        } else {
            statusButton.setTooltips("Check the status of", "the selected receiver", "(needs an adjacent analyzer!)");
        }

        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(dialButton).addChild(dialOnceButton).addChild(interruptButton).
                addChild(favoriteButton).addChild(statusButton).setDesiredHeight(16);

        statusLabel = new Label(mc, this);
        statusLabel.setDesiredWidth(180).setDesiredHeight(14).setFilledRectThickness(1);
        Panel statusPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Status")).addChild(statusLabel).setDesiredHeight(16);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setVerticalMargin(3).setSpacing(1)).
                addChild(energyBar).addChild(transmitterPanel).
                addChild(receiverPanel).addChild(buttonPanel).addChild(statusPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, DIALER_WIDTH, DIALER_HEIGHT));
        window = new mcjty.lib.gui.Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        listDirty = 0;
        clearSelectedStatus();

        requestReceivers();
        requestTransmitters();
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
    }

    private Panel setupReceiverPanel() {
        receiverList = createStyledList().setRowheight(14).setDesiredHeight(100).setPropagateEventsToChildren(true).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                clearSelectedStatus();
            }

            @Override
            public void doubleClick(Widget parent, int index) {
                hilightSelectedReceiver(index);
            }
        });
        Slider receiverSlider = new Slider(mc, this).setDesiredWidth(10).setDesiredHeight(100).setVertical().setScrollable(receiverList);
        return new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChild(receiverList).addChild(receiverSlider).setDesiredHeight(106);
    }

    private Panel setupTransmitterPanel() {
        transmitterList = createStyledList().setRowheight(18).setDesiredHeight(58).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                clearSelectedStatus();
                selectReceiverFromTransmitter();
            }

            @Override
            public void doubleClick(Widget parent, int index) {
                hilightSelectedTransmitter(index);
            }
        });
        Slider transmitterSlider = new Slider(mc, this).setDesiredWidth(10).setDesiredHeight(58).setVertical().setScrollable(transmitterList);
        return new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChild(transmitterList).addChild(transmitterSlider).setDesiredHeight(64);
    }

    private void clearSelectedStatus() {
        lastDialedTransmitter = false;
        lastCheckedReceiver = false;
    }

    private void hilightSelectedTransmitter(int index) {
        TransmitterInfo transmitterInfo = getSelectedTransmitter(index);
        if (transmitterInfo == null) {
            return;
        }
        Coordinate c = transmitterInfo.getCoordinate();
        RFTools.instance.clientInfo.hilightBlock(c, System.currentTimeMillis() + 1000 * StorageScannerConfiguration.hilightTime);
        mc.getMinecraft().thePlayer.closeScreen();
    }

    private void hilightSelectedReceiver(int index) {
        TeleportDestination destination = getSelectedReceiver(index);
        if (destination == null) {
            return;
        }

        Coordinate c = destination.getCoordinate();
        double distance = Vec3.createVectorHelper(c.getX(), c.getY(), c.getZ()).distanceTo(mc.thePlayer.getPosition(1.0f));

        if (destination.getDimension() != mc.theWorld.provider.dimensionId || distance > 150) {
            Logging.warn(mc.thePlayer, "Receiver is too far to hilight!");
            mc.getMinecraft().thePlayer.closeScreen();
            return;
        }
        RFTools.instance.clientInfo.hilightBlock(c, System.currentTimeMillis()+1000* StorageScannerConfiguration.hilightTime);
        Logging.message(mc.thePlayer, "The receiver is now highlighted");
        mc.getMinecraft().thePlayer.closeScreen();
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
        TeleportDestination destination = getSelectedReceiver(receiverSelected);
        if (destination == null) {
            return;
        }
        Coordinate c = destination.getCoordinate();
        RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
                DialingDeviceTileEntity.CMD_CHECKSTATUS,
                DialingDeviceTileEntity.CLIENTCMD_STATUS, new Argument("c", c), new Argument("dim", destination.getDimension())));

        lastCheckedReceiver = true;
        listDirty = 0;
    }

    private void showStatus(int dialResult) {
        if ((dialResult & DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK) != 0) {
            setStatusError("Dialing device power low!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_RECEIVER_POWER_LOW_MASK) != 0) {
            setStatusError("Matter receiver power low!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_DIMENSION_POWER_LOW_MASK) != 0) {
            setStatusError("Destination dimension power low!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_TRANSMITTER_NOACCESS) != 0) {
            setStatusError("No access to transmitter!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_RECEIVER_NOACCESS) != 0) {
            setStatusError("No access to receiver!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK) != 0) {
            setStatusError("Invalid destination!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_INVALID_SOURCE_MASK) != 0) {
            setStatusError("Invalid source!");
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
        if ((dialResult & DialingDeviceTileEntity.DIAL_INVALID_TRANSMITTER) != 0) {
            setStatusError("Invalid transmitter!!");
            return;
        }
        if ((dialResult & DialingDeviceTileEntity.DIAL_INTERRUPTED) != 0) {
            setStatusMessage("Interrupted!");
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

    private void dial(boolean once) {
        int transmitterSelected = transmitterList.getSelected();
        TransmitterInfo transmitterInfo = getSelectedTransmitter(transmitterSelected);
        if (transmitterInfo == null) {
            return;
        }

        int receiverSelected = receiverList.getSelected();
        TeleportDestination destination = getSelectedReceiver(receiverSelected);
        if (destination == null) {
            return;
        }

        RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
                once ? DialingDeviceTileEntity.CMD_DIALONCE : DialingDeviceTileEntity.CMD_DIAL,
                DialingDeviceTileEntity.CLIENTCMD_DIAL,
                new Argument("player", mc.thePlayer.getDisplayName()),
                new Argument("trans", transmitterInfo.getCoordinate()), new Argument("transDim", mc.theWorld.provider.dimensionId),
                new Argument("c", destination.getCoordinate()), new Argument("dim", destination.getDimension())));

        lastDialedTransmitter = true;
        listDirty = 0;
    }

    private TeleportDestinationClientInfo getSelectedReceiver(int receiverSelected) {
        if (receiverSelected == -1) {
            return null;
        }
        if (receiverSelected >= receivers.size()) {
            return null;
        }
        return receivers.get(receiverSelected);
    }

    private void interruptDial() {
        int transmitterSelected = transmitterList.getSelected();
        TransmitterInfo transmitterInfo = getSelectedTransmitter(transmitterSelected);
        if (transmitterInfo == null) {
            return;
        }
        RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, DialingDeviceTileEntity.CMD_DIAL,
                DialingDeviceTileEntity.CLIENTCMD_DIAL,
                new Argument("player", mc.thePlayer.getDisplayName()),
                new Argument("trans", transmitterInfo.getCoordinate()), new Argument("transDim", mc.theWorld.provider.dimensionId),
                new Argument("c", (Coordinate) null), new Argument("dim", 0)));

        lastDialedTransmitter = true;
        listDirty = 0;
    }

    private void requestReceivers() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetReceivers(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, mc.thePlayer.getDisplayName()));
    }

    private void requestTransmitters() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetTransmitters(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
    }

    private void changeShowFavorite() {
        boolean fav = favoriteButton.getCurrentChoiceIndex() == 1;
        sendServerCommand(RFToolsMessages.INSTANCE, DialingDeviceTileEntity.CMD_SHOWFAVORITE,
                new Argument("favorite", fav));
        listDirty = 0;
        transmitterList.setSelected(-1);
        receiverList.setSelected(-1);
    }

    private void changeFavorite() {
        int receiverSelected = receiverList.getSelected();
        TeleportDestinationClientInfo destination = getSelectedReceiver(receiverSelected);
        if (destination == null) {
            return;
        }
        boolean favorite = destination.isFavorite();
        destination.setFavorite(!favorite);
        sendServerCommand(RFToolsMessages.INSTANCE, DialingDeviceTileEntity.CMD_FAVORITE,
                new Argument("player", mc.thePlayer.getDisplayName()),
                new Argument("receiver", destination.getCoordinate()),
                new Argument("dimension", destination.getDimension()),
                new Argument("favorite", !favorite));
        listDirty = 0;
    }

    private void populateReceivers() {
        List<TeleportDestinationClientInfo> newReceivers = fromServer_receivers;
        if (newReceivers == null) {
            return;
        }

        boolean newReceiversFiltered = favoriteButton.getCurrentChoiceIndex() == 1;

        if (newReceivers.equals(receivers) && newReceiversFiltered == receiversFiltered) {
            return;
        }

        receiversFiltered = newReceiversFiltered;
        if (receiversFiltered) {
            receivers = new ArrayList<TeleportDestinationClientInfo>();
            // We only show favorited receivers. Remove the rest.
            for (TeleportDestinationClientInfo receiver : newReceivers) {
                if (receiver.isFavorite()) {
                    receivers.add(receiver);
                }
            }
        } else {
            // Show all receivers.
            receivers = new ArrayList<TeleportDestinationClientInfo>(newReceivers);
        }

        receiverList.removeChildren();

        for (TeleportDestinationClientInfo destination : receivers) {
            Coordinate coordinate = destination.getCoordinate();

            String dimName = destination.getDimensionName();
            if (dimName == null || dimName.trim().isEmpty()) {
                dimName = "Id " + destination.getDimension();
            }

            boolean favorite = destination.isFavorite();
            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(5));
            panel.addChild(new Label(mc, this).setText(destination.getName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(90).
                    setTooltips("The name of the", "destination receiver:", destination.getName() + " (" + coordinate.toString() + ")"));
            panel.addChild(new Label(mc, this).setText(dimName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDynamic(true).setTooltips("The name of the", "destination dimension:", dimName));
            ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).addChoiceEvent(new ChoiceEvent() {
                @Override
                public void choiceChanged(Widget parent, String newChoice) {
                    changeFavorite();
                }
            }).setDesiredWidth(10);
            choiceLabel.addChoice("No", "Not favorited", guielements, 131, 19);
            choiceLabel.addChoice("Yes", "Favorited", guielements, 115, 19);
            choiceLabel.setCurrentChoice(favorite ? 1 : 0);
            panel.addChild(choiceLabel);
            receiverList.addChild(panel);
        }
    }

    private TeleportDestination getSelectedTransmitterDestination() {
        int transmitterSelected = transmitterList.getSelected();
        TransmitterInfo transmitterInfo = getSelectedTransmitter(transmitterSelected);
        if (transmitterInfo == null) {
            return null;
        }
        TeleportDestination destination = transmitterInfo.getTeleportDestination();
        if (destination.isValid()) {
            return destination;
        } else {
            return null;
        }
    }

    private void populateTransmitters() {
        List<TransmitterInfo> newTransmitters = fromServer_transmitters;
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
            panel.addChild(new Label(mc, this).setText(transmitterInfo.getName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(90));
            panel.addChild(new Label(mc, this).setDynamic(true).setText(coordinate.toString()));
            panel.addChild(new ImageLabel(mc, this).setImage(guielements, destination.isValid() ? 80 : 96, 0).setDesiredWidth(16));
            transmitterList.addChild(panel);
        }
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        requestListsIfNeeded();

        populateReceivers();
        populateTransmitters();

        if (lastDialedTransmitter) {
            showStatus(fromServer_dialResult);
        } else if (lastCheckedReceiver) {
            showStatus(fromServer_receiverStatus);
        } else {
            statusLabel.setText("");
        }

        enableButtons();

        drawWindow();
        int currentRF = tileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestReceivers();
            requestTransmitters();
            listDirty = 20;
        }
    }

    private String calculateDistance(int transmitterSelected, int receiverSelected) {
        TransmitterInfo transmitterInfo = getSelectedTransmitter(transmitterSelected);
        if (transmitterInfo == null) {
            return "?";
        }
        TeleportDestination teleportDestination = getSelectedReceiver(receiverSelected);
        if (teleportDestination == null) {
            return "?";
        }

        return DialingDeviceTileEntity.calculateDistance(mc.theWorld, transmitterInfo, teleportDestination);
    }

    private TransmitterInfo getSelectedTransmitter(int transmitterSelected) {
        if (transmitterSelected == -1) {
            return null;
        }
        if (transmitterSelected >= transmitters.size()) {
            return null;
        }
        return transmitters.get(transmitterSelected);
    }

    private void enableButtons() {
        int transmitterSelected = transmitterList.getSelected();
        if (transmitters == null || transmitterSelected >= transmitters.size()) {
            transmitterSelected = -1;
            transmitterList.setSelected(-1);
        }
        int receiverSelected = receiverList.getSelected();
        if (receivers == null || receiverSelected >= receivers.size()) {
            receiverSelected = -1;
            receiverList.setSelected(-1);
        }

        if (transmitterSelected != -1 && receiverSelected != -1) {
            dialButton.setEnabled(true);
            dialOnceButton.setEnabled(true);
            String distance = calculateDistance(transmitterSelected, receiverSelected);
            dialButton.setTooltips("Start a connection between", "the selected transmitter", "and the selected receiver.", "Distance: " + distance);
            dialOnceButton.setTooltips("Dial a connection for a", "single teleport.", "Distance: " + distance);
        } else {
            dialButton.setEnabled(false);
            dialOnceButton.setEnabled(false);
            dialButton.setTooltips("Start a connection between", "the selected transmitter", "and the selected receiver");
            dialOnceButton.setTooltips("Dial a connection for a", "single teleport");
        }
        if (transmitterSelected != -1) {
            TeleportDestination destination = getSelectedTransmitterDestination();
            interruptButton.setEnabled(destination != null);
        } else {
            interruptButton.setEnabled(false);
        }
        if (receiverSelected != -1) {
            statusButton.setEnabled(analyzerAvailable);
        } else {
            statusButton.setEnabled(false);
        }
    }
}

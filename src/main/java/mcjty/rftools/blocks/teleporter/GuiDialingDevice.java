package mcjty.rftools.blocks.teleporter;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static mcjty.rftools.blocks.teleporter.DialingDeviceTileEntity.*;

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

    private int listDirty = 10;


    public GuiDialingDevice(DialingDeviceTileEntity dialingDeviceTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, dialingDeviceTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "tpdialer");
        GenericEnergyStorageTileEntity.setCurrentRF(dialingDeviceTileEntity.getStoredPower());

        xSize = DIALER_WIDTH;
        ySize = DIALER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        long maxEnergyStored = tileEntity.getCapacity();
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setHorizontal().setDesiredWidth(80).setDesiredHeight(12).setMaxValue(maxEnergyStored).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        Panel transmitterPanel = setupTransmitterPanel();
        Panel receiverPanel = setupReceiverPanel();

        dialButton = new Button(mc, this).setChannel("dial").setText("Dial").setTooltips("Start a connection between", "the selected transmitter", "and the selected receiver").
                setDesiredHeight(14).setDesiredWidth(65);
        dialOnceButton = new Button(mc, this).setChannel("dialonce").setText("Dial Once").setTooltips("Dial a connection for a", "single teleport").
                setDesiredHeight(14).setDesiredWidth(65);
        interruptButton = new Button(mc, this).setChannel("interrupt").setText("Interrupt").setTooltips("Interrupt a connection", "for the selected transmitter").
                setDesiredHeight(14).setDesiredWidth(65);
        favoriteButton = new ImageChoiceLabel(mc, this).setChannel("favorite").setDesiredWidth(10).setDesiredHeight(10);
        favoriteButton.addChoice("No", "Unfavorited receiver", guielements, 131, 19);
        favoriteButton.addChoice("Yes", "Favorited receiver", guielements, 115, 19);
        favoriteButton.setCurrentChoice(tileEntity.isShowOnlyFavorites() ? 1 : 0);

        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(dialButton).addChild(dialOnceButton).addChild(interruptButton).
                addChild(favoriteButton).setDesiredHeight(16);

        analyzerAvailable =  DialingDeviceTileEntity.isDestinationAnalyzerAvailable(mc.world, tileEntity.getPos());
        statusButton = new Button(mc, this).setChannel("check").setText("Check").
                setDesiredHeight(14).setDesiredWidth(65).
                setEnabled(analyzerAvailable);
        if (analyzerAvailable) {
            statusButton.setTooltips("Check the status of", "the selected receiver");
        } else {
            statusButton.setTooltips("Check the status of", "the selected receiver", "(needs an adjacent analyzer!)");
        }

        statusLabel = new Label(mc, this);
        statusLabel.setDesiredWidth(170).setDesiredHeight(14).setFilledRectThickness(1);
        Panel statusPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChildren(statusButton, statusLabel).setDesiredHeight(16);

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setVerticalMargin(3).setSpacing(1)).
                addChildren(energyBar, transmitterPanel, receiverPanel, buttonPanel, statusPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, DIALER_WIDTH, DIALER_HEIGHT));
        window = new mcjty.lib.gui.Window(this, toplevel);

        window.event("dial", (source, params) -> dial(false));
        window.event("dialonce", (source, params) -> dial(true));
        window.event("interrupt", (source, params) -> interruptDial());
        window.event("favorite", (source, params) -> changeShowFavorite());
        window.event("check", (source, params) -> checkStatus());

        Keyboard.enableRepeatEvents(true);

        fromServer_receivers = null;
        fromServer_transmitters = null;

        listDirty = 0;
        clearSelectedStatus();

        requestReceivers();
        requestTransmitters();
        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private Panel setupReceiverPanel() {
        receiverList = new WidgetList(mc, this).setName("receivers").setRowheight(14).setDesiredHeight(100).setPropagateEventsToChildren(true).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget<?> parent, int index) {
                clearSelectedStatus();
            }

            @Override
            public void doubleClick(Widget<?> parent, int index) {
                hilightSelectedReceiver(index);
            }
        });
        Slider receiverSlider = new Slider(mc, this).setDesiredWidth(11).setDesiredHeight(100).setVertical().setScrollableName("receivers");
        return new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChildren(receiverList, receiverSlider).setDesiredHeight(106)
                .setFilledBackground(0xff9e9e9e);
    }

    private Panel setupTransmitterPanel() {
        transmitterList = new WidgetList(mc, this).setName("transmitters").setRowheight(18).setDesiredHeight(58).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget<?> parent, int index) {
                clearSelectedStatus();
                selectReceiverFromTransmitter();
            }

            @Override
            public void doubleClick(Widget<?> parent, int index) {
                hilightSelectedTransmitter(index);
            }
        });
        Slider transmitterSlider = new Slider(mc, this).setDesiredWidth(11).setDesiredHeight(58).setVertical().setScrollableName("transmitters");
        return new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChildren(transmitterList, transmitterSlider).setDesiredHeight(64)
                .setFilledBackground(0xff9e9e9e);
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
        BlockPos c = transmitterInfo.getCoordinate();
        RFTools.instance.clientInfo.hilightBlock(c, System.currentTimeMillis() + 1000 * 5);//@todo StorageScannerConfiguration.hilightTime);
        mc.player.closeScreen();
    }

    private void hilightSelectedReceiver(int index) {
        TeleportDestination destination = getSelectedReceiver(index);
        if (destination == null) {
            return;
        }

        BlockPos c = destination.getCoordinate();
        double distance = new Vec3d(c.getX(), c.getY(), c.getZ()).distanceTo(mc.player.getPositionVector());

        if (destination.getDimension() != mc.world.provider.getDimension() || distance > 150) {
            Logging.warn(mc.player, "Receiver is too far to hilight!");
            mc.player.closeScreen();
            return;
        }
        RFTools.instance.clientInfo.hilightBlock(c, System.currentTimeMillis()+1000*5);// @todo StorageScannerConfiguration.hilightTime);
        Logging.message(mc.player, "The receiver is now highlighted");
        mc.player.closeScreen();
    }

    private void setStatusError(String message) {
        statusLabel.setText(message);
        statusLabel.setColor(0xffffffff);
        statusLabel.setFilledBackground(0xffff0000, 0xff880000);
    }

    private void setStatusMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setColor(0xff000000);
        statusLabel.setFilledBackground(0xff00ff00, 0xff008800);
    }

    private void checkStatus() {
        int receiverSelected = receiverList.getSelected();
        TeleportDestination destination = getSelectedReceiver(receiverSelected);
        if (destination == null) {
            return;
        }
        BlockPos c = destination.getCoordinate();
        tileEntity.requestDataFromServer(RFTools.MODID,
                DialingDeviceTileEntity.CMD_CHECKSTATUS,
                TypedMap.builder()
                        .put(PARAM_POS, c)
                        .put(PARAM_DIMENSION, destination.getDimension())
                        .build());

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

        tileEntity.requestDataFromServer(RFTools.MODID,
                once ? DialingDeviceTileEntity.CMD_DIALONCE : DialingDeviceTileEntity.CMD_DIAL,
                TypedMap.builder()
                        .put(PARAM_PLAYER, mc.player.getName())
                        .put(PARAM_TRANSMITTER, transmitterInfo.getCoordinate())
                        .put(PARAM_TRANS_DIMENSION, mc.world.provider.getDimension())
                        .put(PARAM_POS, destination.getCoordinate())
                        .put(PARAM_DIMENSION, destination.getDimension())
                        .build());

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
        tileEntity.requestDataFromServer(RFTools.MODID, DialingDeviceTileEntity.CMD_DIAL,
                TypedMap.builder()
                        .put(PARAM_PLAYER, mc.player.getName())
                        .put(PARAM_TRANSMITTER, transmitterInfo.getCoordinate())
                        .put(PARAM_TRANS_DIMENSION, mc.world.provider.getDimension())
                        .put(PARAM_POS, null)
                        .put(PARAM_DIMENSION, 0)
                        .build());

        lastDialedTransmitter = true;
        listDirty = 0;
    }

    private void requestReceivers() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetReceivers(tileEntity.getPos(), mc.player.getName()));
    }

    private void requestTransmitters() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetTransmitters(tileEntity.getPos()));
    }

    private void changeShowFavorite() {
        boolean fav = favoriteButton.getCurrentChoiceIndex() == 1;
        sendServerCommand(RFToolsMessages.INSTANCE, DialingDeviceTileEntity.CMD_SHOWFAVORITE,
                TypedMap.builder()
                        .put(PARAM_FAVORITE, fav)
                        .build());
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
                TypedMap.builder()
                    .put(PARAM_PLAYER, mc.player.getName())
                    .put(PARAM_POS, destination.getCoordinate())
                    .put(PARAM_DIMENSION, destination.getDimension())
                    .put(PARAM_FAVORITE, !favorite)
                    .build());
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
            receivers = new ArrayList<>();
            // We only show favorited receivers. Remove the rest.
            for (TeleportDestinationClientInfo receiver : newReceivers) {
                if (receiver.isFavorite()) {
                    receivers.add(receiver);
                }
            }
        } else {
            // Show all receivers.
            receivers = new ArrayList<>(newReceivers);
        }

        receiverList.removeChildren();

        for (TeleportDestinationClientInfo destination : receivers) {
            BlockPos coordinate = destination.getCoordinate();

            String dimName = destination.getDimensionName();
            if (dimName == null || dimName.trim().isEmpty()) {
                dimName = "Id " + destination.getDimension();
            }

            boolean favorite = destination.isFavorite();
            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3));
            panel.addChild(new Label(mc, this).setColor(StyleConfig.colorTextInListNormal).setText(destination.getName()).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setDesiredWidth(96).
                    setTooltips("The name of the", "destination receiver:", destination.getName() + " (" + BlockPosTools.toString(coordinate) + ")"));
            panel.addChild(new Label(mc, this).setColor(StyleConfig.colorTextInListNormal).setText(dimName).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                    .setDynamic(true).setTooltips("The name of the", "destination dimension:", dimName)
                    .setDesiredWidth(110));
            ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).addChoiceEvent((parent, newChoice) -> changeFavorite()).setDesiredWidth(10);
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

        transmitters = new ArrayList<>(newTransmitters);
        transmitterList.removeChildren();

        for (TransmitterInfo transmitterInfo : transmitters) {
            BlockPos coordinate = transmitterInfo.getCoordinate();
            TeleportDestination destination = transmitterInfo.getTeleportDestination();

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(3));
            panel.addChild(new Label(mc, this).setColor(StyleConfig.colorTextInListNormal).setText(transmitterInfo.getName()).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setDesiredWidth(102));
            panel.addChild(new Label(mc, this).setColor(StyleConfig.colorTextInListNormal).setDynamic(true).setText(BlockPosTools.toString(coordinate)).setDesiredWidth(90));
            panel.addChild(new ImageLabel(mc, this).setImage(guielements, destination.isValid() ? 80 : 96, 0).setDesiredWidth(16));
            transmitterList.addChild(panel);
        }

        if (transmitterList.getChildCount() == 1) {
            transmitterList.setSelected(0);
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
            statusLabel.setColor(0xff000000);
            statusLabel.setFilledBackground(-1);
        }

        enableButtons();

        drawWindow();
        long currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void requestListsIfNeeded() {
        if (fromServer_receivers != null && fromServer_transmitters != null) {
            return;
        }
        listDirty--;
        if (listDirty <= 0) {
            requestReceivers();
            requestTransmitters();
            listDirty = 10;
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

        return DialingDeviceTileEntity.calculateDistance(mc.world, transmitterInfo, teleportDestination);
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

package mcjty.rftools.blocks.teleporter;

import mcjty.container.EmptyContainer;
import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.events.TextEvent;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GuiMatterReceiver extends GenericGuiContainer<MatterReceiverTileEntity> {
    public static final int MATTER_WIDTH = 180;
    public static final int MATTER_HEIGHT = 160;
    public static final String ACCESS_PRIVATE = "Private";
    public static final String ACCESS_PUBLIC = "Public";

    private EnergyBar energyBar;
    private ChoiceLabel privateSetting;
    private WidgetList allowedPlayers;
    private Button addButton;
    private Button delButton;
    private TextField nameField;

    // A copy of the players we're currently showing.
    private java.util.List<String> players = null;
    private int listDirty = 0;

    private static Set<String> fromServer_allowedPlayers = new HashSet<String>();
    public static void storeAllowedPlayersForClient(List<PlayerName> players) {
        Set<String> p = new HashSet<String>();
        for (PlayerName n : players) {
            p.add(n.getName());
        }
        fromServer_allowedPlayers = p;
    }


    public GuiMatterReceiver(MatterReceiverTileEntity matterReceiverTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, matterReceiverTileEntity, container, RFTools.GUI_MANUAL_MAIN, "tpreceiver");
        matterReceiverTileEntity.setCurrentRF(matterReceiverTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = MATTER_WIDTH;
        ySize = MATTER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setHorizontal().setDesiredHeight(12).setDesiredWidth(MATTER_WIDTH-10).setMaxValue(maxEnergyStored).setShowText(true);
        energyBar.setValue(tileEntity.getCurrentRF());

        TextField textField = new TextField(mc, this).setTooltips("Use this name to", "identify this receiver", "in the dialer").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                setReceiverName(newText);
            }
        });
        textField.setText(tileEntity.getName());
        Panel namePanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Name:")).addChild(textField).setDesiredHeight(16);

        privateSetting = new ChoiceLabel(mc, this).addChoices(ACCESS_PUBLIC, ACCESS_PRIVATE).setDesiredHeight(13).setDesiredWidth(60).
                setChoiceTooltip(ACCESS_PUBLIC, "Everyone can dial to this receiver").
                setChoiceTooltip(ACCESS_PRIVATE, "Only people in the access list below", "can dial to this receiver").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeAccessMode(newChoice);
                    }
                });
        if (tileEntity.isPrivateAccess()) {
            privateSetting.setChoice(ACCESS_PRIVATE);
        } else {
            privateSetting.setChoice(ACCESS_PUBLIC);
        }
        Panel privatePanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Access:")).addChild(privateSetting).setDesiredHeight(16);

        allowedPlayers = createStyledList();
        Slider allowedPlayerSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(allowedPlayers);
        Panel allowedPlayersPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(allowedPlayers).addChild(allowedPlayerSlider);

        nameField = new TextField(mc, this);
        addButton = new Button(mc, this).setText("Add").setDesiredHeight(13).setDesiredWidth(34).setTooltips("Add a player to the access list").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        addPlayer();
                    }
                });
        delButton = new Button(mc, this).setText("Del").setDesiredHeight(13).setDesiredWidth(34).setTooltips("Remove the selected player", "from the access list").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        delPlayer();
                    }
                });
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(nameField).addChild(addButton).addChild(delButton).setDesiredHeight(16);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setHorizontalMargin(3).setVerticalMargin(3).setSpacing(1)).
                addChild(energyBar).addChild(namePanel).addChild(privatePanel).addChild(allowedPlayersPanel).addChild(buttonPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, MATTER_WIDTH, MATTER_HEIGHT));
        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        listDirty = 0;
        requestPlayers();
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
    }

    private void setReceiverName(String text) {
        sendServerCommand(RFToolsMessages.INSTANCE, MatterReceiverTileEntity.CMD_SETNAME, new Argument("name", text));
    }

    private void changeAccessMode(String newAccess) {
        sendServerCommand(RFToolsMessages.INSTANCE, MatterReceiverTileEntity.CMD_SETPRIVATE, new Argument("private", ACCESS_PRIVATE.equals(newAccess)));
    }

    private void addPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, MatterReceiverTileEntity.CMD_ADDPLAYER, new Argument("player", nameField.getText()));
        listDirty = 0;
    }

    private void delPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, MatterReceiverTileEntity.CMD_DELPLAYER, new Argument("player", players.get(allowedPlayers.getSelected())));
        listDirty = 0;
    }


    private void requestPlayers() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetPlayers(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
    }

    private void populatePlayers() {
        List<String> newPlayers = new ArrayList<String>(fromServer_allowedPlayers);
        Collections.sort(newPlayers);
        if (newPlayers.equals(players)) {
            return;
        }

        players = new ArrayList<String>(newPlayers);
        allowedPlayers.removeChildren();
        for (String player : players) {
            allowedPlayers.addChild(new Label(mc, this).setText(player).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        }
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestPlayers();
            listDirty = 20;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        requestListsIfNeeded();
        populatePlayers();
        enableButtons();

        drawWindow();
        int currentRF = tileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
    }

    private void enableButtons() {
        boolean isPrivate = ACCESS_PRIVATE.equals(privateSetting.getCurrentChoice());
        allowedPlayers.setEnabled(isPrivate);
        nameField.setEnabled(isPrivate);

        int isPlayerSelected = allowedPlayers.getSelected();
        delButton.setEnabled(isPrivate && (isPlayerSelected != -1));
        String name = nameField.getText();
        addButton.setEnabled(isPrivate && name != null && !name.isEmpty());
    }
}

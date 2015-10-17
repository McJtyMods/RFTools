package mcjty.rftools.blocks.security;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.events.ChoiceEvent;
import mcjty.lib.gui.events.TextEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class GuiSecurityManager extends GenericGuiContainer<SecurityManagerTileEntity> {
    public static final int SECURITYMANAGER_WIDTH = 244;
    public static final int SECURITYMANAGER_HEIGHT = 206;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/securitymanager.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private WidgetList players;
    private Button addButton;
    private Button delButton;
    private TextField nameField;
    private ImageChoiceLabel blacklistMode;
    private TextField channelNameField;

    private int listDirty = 0;

    public static SecurityChannels.SecurityChannel channelFromServer = null;

    public GuiSecurityManager(SecurityManagerTileEntity securityManagerTileEntity, SecurityManagerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, securityManagerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "security");

        xSize = SECURITYMANAGER_WIDTH;
        ySize = SECURITYMANAGER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        players = createStyledList();
        Slider allowedPlayerSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(players);
        Panel allowedPlayersPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(3).setSpacing(1)).addChild(players).addChild(allowedPlayerSlider).
                setLayoutHint(new PositionalLayout.PositionalHint(72, 5, SECURITYMANAGER_WIDTH - 76, 96));

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
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(3).setSpacing(1)).addChild(nameField).addChild(addButton).addChild(delButton).setDesiredHeight(16).
                setLayoutHint(new PositionalLayout.PositionalHint(72, 100, SECURITYMANAGER_WIDTH - 76, 14));

        channelNameField = new TextField(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(8, 27, 60, 14)).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                updateChannelName();
            }
        });

        blacklistMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(10, 44, 16, 16)).setTooltips("Black or whitelist mode").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        blacklistMode.addChoice("White", "Whitelist players", guiElements, 15 * 16, 32);
        blacklistMode.addChoice("Black", "Blacklist players", guiElements, 14 * 16, 32);


        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(allowedPlayersPanel).addChild(buttonPanel).addChild(channelNameField).
                addChild(blacklistMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        channelFromServer = null;
    }

    private void requestInfoIfNeeded() {
        int id = getCardID();
        if (id == -1) {
            return;
        }
        listDirty--;
        if (listDirty <= 0) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetSecurityInfo(id));
            listDirty = 20;
        }
    }

    private void updateChannelName() {
        listDirty = 20;     // Make sure we don't request new info from server too fast
        String channelName = channelNameField.getText();
        if (channelFromServer != null) {
            channelFromServer.setName(channelName);
        }
        sendServerCommand(RFToolsMessages.INSTANCE, SecurityManagerTileEntity.CMD_SETCHANNELNAME, new Argument("name", channelName));
    }

    private void updateSettings() {
        listDirty = 20;     // Make sure we don't request new info from server too fast
        boolean whitelist = blacklistMode.getCurrentChoiceIndex() == 0;
        if (channelFromServer != null) {
            channelFromServer.setWhitelist(whitelist);
        }
        sendServerCommand(RFToolsMessages.INSTANCE, SecurityManagerTileEntity.CMD_SETMODE, new Argument("whitelist", whitelist));
    }

    private void populatePlayers() {
//        List<String> newPlayers = new ArrayList<String>(fromServer_allowedPlayers);
//        Collections.sort(newPlayers);
//        if (newPlayers.equals(players)) {
//            return;
//        }
//
//        players = new ArrayList<String>(newPlayers);
//        allowedPlayers.removeChildren();
//        for (String player : players) {
//            allowedPlayers.addChild(new Label(mc, this).setText(player).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
//        }
    }

    private void addPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, SecurityManagerTileEntity.CMD_ADDPLAYER, new Argument("player", nameField.getText()));
    }

    private void delPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, SecurityManagerTileEntity.CMD_DELPLAYER, new Argument("player", nameField.getText()));
    }

    private int getCardID() {
        Slot slot = (Slot) inventorySlots.inventorySlots.get(SecurityManagerContainer.SLOT_CARD);
        if (slot.getHasStack()) {
            NBTTagCompound tagCompound = slot.getStack().getTagCompound();
            if (tagCompound == null) {
                return -1;
            }
            if (tagCompound.hasKey("channel")) {
                return tagCompound.getInteger("channel");
            }
        }
        return -1;
    }


    private void updateGui() {
        int id = getCardID();
        blacklistMode.setEnabled(id != -1);
        players.setEnabled(id != -1);
        addButton.setEnabled(id != -1);
        delButton.setEnabled(id != -1);
        nameField.setEnabled(id != -1);
        channelNameField.setEnabled(id != -1);

        players.removeChildren();

        if (id != -1 && channelFromServer != null) {
            channelNameField.setText(channelFromServer.getName());
            blacklistMode.setCurrentChoice(channelFromServer.isWhitelist() ? 0 : 1);
            for (String player : channelFromServer.getPlayers()) {
                players.addChild(new Label(mc, this).setText(player).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
            }
        } else {
            channelNameField.setText("");
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateGui();
        drawWindow();
        requestInfoIfNeeded();
    }
}

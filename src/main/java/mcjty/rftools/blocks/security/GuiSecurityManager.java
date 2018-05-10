package mcjty.rftools.blocks.security;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.network.Arguments;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.CommandHandler;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.Rectangle;

import static mcjty.rftools.blocks.security.SecurityManagerTileEntity.*;

public class GuiSecurityManager extends GenericGuiContainer<SecurityManagerTileEntity> {
    public static final int SECURITYMANAGER_WIDTH = 244;
    public static final int SECURITYMANAGER_HEIGHT = 206;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/securitymanager.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private WidgetList players;
    private TextField nameField;
    private ImageChoiceLabel blacklistMode;
    private TextField channelNameField;

    private int listDirty = 0;

    public static SecurityChannels.SecurityChannel channelFromServer = null;

    public GuiSecurityManager(SecurityManagerTileEntity securityManagerTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, securityManagerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "security");

        xSize = SECURITYMANAGER_WIDTH;
        ySize = SECURITYMANAGER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        players = new WidgetList(mc, this)
                .setName("players")
                .setEnabledFlags("card");
        Slider allowedPlayerSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollableName("players");
        Panel allowedPlayersPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(3).setSpacing(1)).addChildren(players, allowedPlayerSlider).
                setLayoutHint(new PositionalLayout.PositionalHint(72, 5, SECURITYMANAGER_WIDTH - 76, 96));

        nameField = new TextField(mc, this).setDesiredHeight(15).setName("name")
            .setEnabledFlags("card");
        Widget addButton = new Button(mc, this).setText("Add").setDesiredHeight(14).setDesiredWidth(34).setTooltips("Add a player to the access list")
                .setEnabledFlags("card")
                .setName("addbutton").setChannel("addbutton");
        Widget delButton = new Button(mc, this).setText("Del").setDesiredHeight(14).setDesiredWidth(34).setTooltips("Remove the selected player", "from the access list")
                .setEnabledFlags("card")
                .setName("delbutton").setChannel("delbutton");
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(3).setSpacing(1)).addChildren(nameField, addButton, delButton).setDesiredHeight(16)
                .setLayoutHint(new PositionalLayout.PositionalHint(72, 100, SECURITYMANAGER_WIDTH - 76, 14));

        channelNameField = new TextField(mc, this).setLayoutHint(8, 27, 60, 14).addTextEvent((parent, newText) -> updateChannelName())
                .setName("channelname")
                .setEnabledFlags("card");

        blacklistMode = new ImageChoiceLabel(mc, this).setLayoutHint(10, 44, 16, 16).setTooltips("Black or whitelist mode").addChoiceEvent((parent, newChoice) -> updateSettings())
                .setName("blacklistmode")
                .setEnabledFlags("card");
        blacklistMode.addChoice("White", "Whitelist players", guiElements, 15 * 16, 32);
        blacklistMode.addChoice("Black", "Blacklist players", guiElements, 14 * 16, 32);


        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChildren(allowedPlayersPanel, buttonPanel, channelNameField, blacklistMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        window.event("addbutton", (source, params) -> addPlayer());
        window.event("delbutton", (source, params) -> delPlayer());


//        window = new Window(this, new ResourceLocation(RFTools.MODID, "gui/security_manager.json"));
//        window.getToplevel().setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
//
//        addButton = window.findChild("addbutton");
//        delButton = window.findChild("delbutton");
//        nameField = (TextField) window.findChild("name");
//        channelNameField = (TextField) window.findChild("channelname");
//        blacklistMode = (ImageChoiceLabel) window.findChild("blacklistmode");
//        players = (WidgetList) window.findChild("players");

        channelFromServer = null;
    }

    private void requestInfoIfNeeded() {
        int id = getCardID();
        if (id == -1) {
            return;
        }
        listDirty--;
        if (listDirty <= 0) {
            sendServerCommand(RFTools.MODID, CommandHandler.CMD_GET_SECURITY_INFO, Arguments.builder().value(id).build());
            listDirty = 20;
        }
    }

    private void updateChannelName() {
        listDirty = 20;     // Make sure we don't request new info from server too fast
        String channelName = channelNameField.getText();
        if (channelFromServer != null) {
            channelFromServer.setName(channelName);
        }
        sendServerCommand(RFToolsMessages.INSTANCE, SecurityManagerTileEntity.CMD_SETCHANNELNAME,
                TypedMap.builder()
                        .put(PARAM_NAME, channelName)
                        .build());
    }

    private void updateSettings() {
        listDirty = 20;     // Make sure we don't request new info from server too fast
        boolean whitelist = blacklistMode.getCurrentChoiceIndex() == 0;
        if (channelFromServer != null) {
            channelFromServer.setWhitelist(whitelist);
        }
        sendServerCommand(RFToolsMessages.INSTANCE, SecurityManagerTileEntity.CMD_SETMODE,
                TypedMap.builder()
                        .put(PARAM_WHITELIST, whitelist)
                        .build());
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
//            allowedPlayers.addChild(new Label(mc, this).setText(player).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
//        }
    }

    private void addPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, SecurityManagerTileEntity.CMD_ADDPLAYER,
                TypedMap.builder()
                        .put(PARAM_PLAYER, nameField.getText())
                        .build());
    }

    private void delPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, SecurityManagerTileEntity.CMD_DELPLAYER,
                TypedMap.builder()
                        .put(PARAM_PLAYER, nameField.getText())
                        .build());
    }

    private int getCardID() {
        Slot slot = inventorySlots.inventorySlots.get(SecurityManagerTileEntity.SLOT_CARD);
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
        window.setFlag(id != -1 ? "card" : "!card");

        players.removeChildren();

        if (id != -1 && channelFromServer != null) {
            channelNameField.setText(channelFromServer.getName());
            blacklistMode.setCurrentChoice(channelFromServer.isWhitelist() ? 0 : 1);
            for (String player : channelFromServer.getPlayers()) {
                players.addChild(new Label(mc, this).setText(player).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
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

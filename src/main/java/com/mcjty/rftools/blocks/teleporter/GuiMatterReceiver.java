package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.EmptyContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketServerCommand;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GuiMatterReceiver extends GuiContainer {
    public static final int MATTER_WIDTH = 200;
    public static final int MATTER_HEIGHT = 180;
    public static final String ACCESS_PRIVATE = "Private";
    public static final String ACCESS_PUBLIC = "Public";

    private Window window;
    private EnergyBar energyBar;
    private ChoiceLabel privateSetting;
    private WidgetList allowedPlayers;
    private Button addButton;
    private Button delButton;
    private TextField nameField;

    // A copy of the players we're currently showing.
    private java.util.List<String> players = null;
    private int listDirty = 0;

    private final MatterReceiverTileEntity matterReceiverTileEntity;

    private static Set<String> fromServer_allowedPlayers = new HashSet<String>();
    public static void storeAllowedPlayersForClient(List<PlayerName> players) {
        Set<String> p = new HashSet<String>();
        for (PlayerName n : players) {
            p.add(n.getName());
        }
        fromServer_allowedPlayers = p;
    }


    public GuiMatterReceiver(MatterReceiverTileEntity matterReceiverTileEntity, EmptyContainer<MatterReceiverTileEntity> container) {
        super(container);
        this.matterReceiverTileEntity = matterReceiverTileEntity;
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
        if (matterReceiverTileEntity.isPrivateAccess()) {
            privateSetting.setChoice(ACCESS_PRIVATE);
        } else {
            privateSetting.setChoice(ACCESS_PUBLIC);
        }
        Panel privatePanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Access:")).addChild(privateSetting).setDesiredHeight(16);

        allowedPlayers = new WidgetList(mc, this).
                setFilledRectThickness(1);
        Slider allowedPlayerSlider = new Slider(mc, this).setDesiredWidth(15).setVertical().setScrollable(allowedPlayers);
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

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setHorizontalMargin(6).setVerticalMargin(6)).
                addChild(energyBar).addChild(namePanel).addChild(privatePanel).addChild(allowedPlayersPanel).addChild(buttonPanel);
        toplevel.setBounds(new Rectangle(k, l, MATTER_WIDTH, MATTER_HEIGHT));
        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        listDirty = 0;
        requestPlayers();
        matterReceiverTileEntity.requestRfFromServer();
    }

    private void setReceiverName(String text) {
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(matterReceiverTileEntity.xCoord, matterReceiverTileEntity.yCoord, matterReceiverTileEntity.zCoord,
                MatterReceiverTileEntity.CMD_SETNAME,
                new Argument("name", text)));
    }

    private void changeAccessMode(String newAccess) {
        boolean isPrivate = ACCESS_PRIVATE.equals(newAccess);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(matterReceiverTileEntity.xCoord, matterReceiverTileEntity.yCoord, matterReceiverTileEntity.zCoord,
                MatterReceiverTileEntity.CMD_SETPRIVATE,
                new Argument("private", isPrivate)));
    }

    private void addPlayer() {
        String name = nameField.getText();
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(matterReceiverTileEntity.xCoord, matterReceiverTileEntity.yCoord, matterReceiverTileEntity.zCoord,
                MatterReceiverTileEntity.CMD_ADDPLAYER,
                new Argument("player", name)));
        listDirty = 0;
    }

    private void delPlayer() {
        int selected = allowedPlayers.getSelected();
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(matterReceiverTileEntity.xCoord, matterReceiverTileEntity.yCoord, matterReceiverTileEntity.zCoord,
                MatterReceiverTileEntity.CMD_DELPLAYER,
                new Argument("player", players.get(selected))));
        listDirty = 0;
    }


    private void requestPlayers() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetPlayers(matterReceiverTileEntity.xCoord, matterReceiverTileEntity.yCoord, matterReceiverTileEntity.zCoord));
    }

    private void populatePlayers() {
        List<String> newPlayers = new ArrayList<String>(fromServer_allowedPlayers);
        Collections.sort(newPlayers);
        if (newPlayers == null) {
            return;
        }
        if (newPlayers.equals(players)) {
            return;
        }

        players = new ArrayList<String>(newPlayers);
        allowedPlayers.removeChildren();
        for (String player : players) {
            allowedPlayers.addChild(new Label(mc, this).setText(player).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        }
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

        window.draw();
        int currentRF = matterReceiverTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        matterReceiverTileEntity.requestRfFromServer();
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

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!window.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }
}

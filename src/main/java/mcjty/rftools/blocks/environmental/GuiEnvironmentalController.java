package mcjty.rftools.blocks.environmental;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.events.TextEvent;
import mcjty.gui.events.ValueEvent;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.RedstoneMode;
import mcjty.rftools.blocks.teleporter.PacketGetPlayers;
import mcjty.rftools.blocks.teleporter.PlayerName;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GuiEnvironmentalController extends GenericGuiContainer<EnvironmentalControllerTileEntity> {
    public static final int ENV_WIDTH = 179;
    public static final int ENV_HEIGHT = 224;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/environmentalcontroller.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    // A copy of the players we're currently showing.
    private List<String> players = null;
    private int listDirty = 0;

    private static Set<String> fromServer_players = new HashSet<String>();
    public static void storePlayersForClient(java.util.List<PlayerName> players) {
        Set<String> p = new HashSet<String>();
        for (PlayerName n : players) {
            p.add(n.getName());
        }
        fromServer_players = p;
    }

    private Panel toplevel;
    private TextField minyTextField;
    private TextField maxyTextField;
    private TextField nameField;
    private ImageChoiceLabel redstoneMode;
    private WidgetList playersList;
    private Button addButton;
    private Button delButton;

    public GuiEnvironmentalController(EnvironmentalControllerTileEntity environmentalControllerTileEntity, EnvironmentalControllerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, environmentalControllerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "envctrl");

        xSize = ENV_WIDTH;
        ySize = ENV_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        int r = tileEntity.getRadius();
        if (r < 5) {
            r = 5;
        } else if (r > 100) {
            r = 100;
        }
        int miny = tileEntity.getMiny();
        int maxy = tileEntity.getMaxy();

        Panel radiusPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(25, 6, ENV_WIDTH - 30, 16));
        ScrollableLabel radius = new ScrollableLabel(mc, this).setRealMinimum(5).setRealMaximum(100).setRealValue(r).setDesiredWidth(24).addValueEvent(new ValueEvent() {
            @Override
            public void valueChanged(Widget parent, int newValue) {
                sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_SETRADIUS, new Argument("radius", newValue));
            }
        });
        Slider slider = new Slider(mc, this).setHorizontal().setScrollable(radius);
        radiusPanel.addChild(new Label(mc, this).setText("Radius:")).addChild(slider).addChild(radius);

        Panel minPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(25, 24, ENV_WIDTH - 30, 16));
        minyTextField = new TextField(mc, this).setText(Integer.toString(miny)).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                sendBounds(true);
            }
        });
        maxyTextField = new TextField(mc, this).setText(Integer.toString(maxy)).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                sendBounds(false);
            }
        });

        minPanel.addChild(new Label(mc, this).setText("Height:")).addChild(minyTextField).addChild(maxyTextField);

        playersList = createStyledList();
        Slider playerSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(playersList);
        Panel playersPanel = new Panel(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(25, 42, ENV_WIDTH - 30, 72))
                .setLayout(new HorizontalLayout().setHorizontalMargin(1)).addChild(playersList).addChild(playerSlider);

        Panel controlPanel = new Panel(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(25, 118, ENV_WIDTH - 30, 16))
                .setLayout(new HorizontalLayout().setHorizontalMargin(1).setVerticalMargin(0).setSpacing(1));
        ChoiceLabel blacklist = new ChoiceLabel(mc, this).addChoices("BL", "WL")
                .setChoiceTooltip("BL", "Players in the list above will not get the effects")
                .setChoiceTooltip("WL", "Players in the list above will get the effects");
        addButton = new Button(mc, this).setText("+").setTooltips("Add a player to the list").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                addPlayer();
            }
        });
        delButton = new Button(mc, this).setText("-").setTooltips("Remove selected player from the list").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                delPlayer();
            }
        });
        nameField = new TextField(mc, this);

        initRedstoneMode();
        controlPanel.addChild(blacklist).addChild(addButton).addChild(delButton).addChild(nameField).addChild(redstoneMode);

        toplevel.addChild(radiusPanel).addChild(minPanel).addChild(playersPanel).addChild(controlPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        listDirty = 0;
        requestPlayers();
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                setDesiredHeight(16).
                setDesiredWidth(16).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeRedstoneMode();
                    }
                }).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setCurrentChoice(tileEntity.getRedstoneMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRedstoneMode(RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()]);
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_RSMODE, new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()].getDescription()));
    }

    private void addPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_ADDPLAYER, new Argument("player", nameField.getText()));
        listDirty = 0;
    }

    private void delPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_DELPLAYER, new Argument("player", players.get(playersList.getSelected())));
        listDirty = 0;
    }

    private void requestPlayers() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetPlayers(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, EnvironmentalControllerTileEntity.CMD_GETPLAYERS, EnvironmentalControllerTileEntity.CLIENTCMD_GETPLAYERS));
    }

    private void populatePlayers() {
        List<String> newPlayers = new ArrayList<String>(fromServer_players);
        Collections.sort(newPlayers);
        if (newPlayers.equals(playersList)) {
            return;
        }

        players  = new ArrayList<String>(newPlayers);
        playersList.removeChildren();
        for (String player : players) {
            playersList.addChild(new Label(mc, this).setText(player).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        }
    }


    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestPlayers();
            listDirty = 20;
        }
    }

    private void sendBounds(boolean minchanged) {
        int miny;
        int maxy;
        try {
            miny = Integer.parseInt(minyTextField.getText());
        } catch (NumberFormatException e) {
            miny = 0;
        }
        try {
            maxy = Integer.parseInt(maxyTextField.getText());
        } catch (NumberFormatException e) {
            maxy = 0;
        }
        if (minchanged) {
            if (miny > maxy) {
                maxy = miny;
                maxyTextField.setText(Integer.toString(maxy));
            }
        } else {
            if (miny > maxy) {
                miny = maxy;
                minyTextField.setText(Integer.toString(miny));
            }
        }
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_SETBOUNDS, new Argument("miny", miny), new Argument("maxy", maxy));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        requestListsIfNeeded();
        populatePlayers();
        enableButtons();

        drawWindow();
    }

    private void enableButtons() {
        int isPlayerSelected = playersList.getSelected();
        delButton.setEnabled(isPlayerSelected != -1);
        String name = nameField.getText();
        addButton.setEnabled(name != null && !name.isEmpty());
    }
}

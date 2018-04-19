package mcjty.rftools.blocks.environmental;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.PacketGetPlayers;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.Rectangle;
import java.util.*;

public class GuiEnvironmentalController extends GenericGuiContainer<EnvironmentalControllerTileEntity> {
    public static final int ENV_WIDTH = 194;
    public static final int ENV_HEIGHT = 224;
    public static final String MODE_BLACKLIST = "BL";
    public static final String MODE_WHITELIST = "WL";
    public static final String MODE_HOSTILE = "Host";
    public static final String MODE_PASSIVE = "Pass";
    public static final String MODE_MOBS = "Mobs";
    public static final String MODE_ALL = "All";

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/environmentalcontroller.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    // A copy of the players we're currently showing.
    private List<String> players = null;
    private int listDirty = 0;
    private EnergyBar energyBar;

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

        int maxEnergyStored = tileEntity.getMaxEnergyStored();
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(8, 141, 10, 76).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        Panel areaPanel = initAreaPanel();
        Panel playersPanel = initPlayerPanel();
        Panel controlPanel = initControlPanel();

        toplevel.addChildren(areaPanel, playersPanel, controlPanel, energyBar);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        listDirty = 0;
        requestPlayers();
    }

    private Panel initPlayerPanel() {
        playersList = new WidgetList(mc, this);
        Slider playerSlider = new Slider(mc, this).setDesiredWidth(11).setVertical().setScrollable(playersList);
        return new Panel(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(25, 42, ENV_WIDTH - 27, 78))
                .setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChildren(playersList, playerSlider);
    }

    private Panel initAreaPanel() {
        int r = tileEntity.getRadius();
        if (r < 5) {
            r = 5;
        } else if (r > 100) {
            r = 100;
        }
        int miny = tileEntity.getMiny();
        int maxy = tileEntity.getMaxy();

        Panel areaPanel = new Panel(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(28, 6, ENV_WIDTH - 33, 37))
                .setLayout(new VerticalLayout().setVerticalMargin(2).setSpacing(0))
                .setFilledRectThickness(-2)
                .setFilledBackground(StyleConfig.colorListBackground);

        Panel radiusPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(16);
        ScrollableLabel radius = new ScrollableLabel(mc, this).setRealMinimum(5).setRealMaximum(100).setRealValue(r).setDesiredWidth(24).addValueEvent((parent, newValue) -> sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_SETRADIUS, new Argument("radius", newValue)));
        Slider slider = new Slider(mc, this).setHorizontal().setScrollable(radius).setMinimumKnobSize(12);
        radiusPanel.addChildren(new Label(mc, this).setText("Radius:"), slider, radius);

        Panel minPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(17);
        minyTextField = new TextField(mc, this).setText(Integer.toString(miny)).addTextEvent((parent, newText) -> sendBounds(true));
        maxyTextField = new TextField(mc, this).setText(Integer.toString(maxy)).addTextEvent((parent, newText) -> sendBounds(false));
        minPanel.addChildren(new Label(mc, this).setText("Height:"), minyTextField, maxyTextField);

        areaPanel.addChildren(radiusPanel, minPanel);
        return areaPanel;
    }

    private Panel initControlPanel() {
        Panel controlPanel = new Panel(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(26, 120, ENV_WIDTH - 32, 16))
                .setLayout(new HorizontalLayout().setHorizontalMargin(1).setVerticalMargin(0).setSpacing(1));
        ChoiceLabel blacklist = new ChoiceLabel(mc, this).addChoices(MODE_BLACKLIST, MODE_WHITELIST, MODE_MOBS, MODE_HOSTILE, MODE_PASSIVE, MODE_ALL)
                .setDesiredWidth(40)
                .setDesiredHeight(15)
                .setChoiceTooltip(MODE_BLACKLIST, "Players in the list above will not get the effects")
                .setChoiceTooltip(MODE_WHITELIST, "Players in the list above will get the effects")
                .setChoiceTooltip(MODE_MOBS, "Affect hostile and passive mobs", "(needs more power)")
                .setChoiceTooltip(MODE_HOSTILE, "Affect hostile mobs", "(needs more power)")
                .setChoiceTooltip(MODE_PASSIVE, "Affect passive mobs", "(needs more power)")
                .setChoiceTooltip(MODE_ALL, "Affect all mobs and players", "(needs more power)")
                .addChoiceEvent((parent, newChoice) -> changeMode(newChoice));
        EnvironmentalControllerTileEntity.EnvironmentalMode mode = tileEntity.getMode();
        switch (mode) {
            case MODE_BLACKLIST:
                blacklist.setChoice(MODE_BLACKLIST);
                break;
            case MODE_WHITELIST:
                blacklist.setChoice(MODE_WHITELIST);
                break;
            case MODE_HOSTILE:
                blacklist.setChoice(MODE_HOSTILE);
                break;
            case MODE_PASSIVE:
                blacklist.setChoice(MODE_PASSIVE);
                break;
            case MODE_MOBS:
                blacklist.setChoice(MODE_MOBS);
                break;
            case MODE_ALL:
                blacklist.setChoice(MODE_ALL);
                break;
        }
        addButton = new Button(mc, this).setText("+").setDesiredHeight(15).setTooltips("Add a player to the list").addButtonEvent(parent -> addPlayer());
        delButton = new Button(mc, this).setText("-").setDesiredHeight(15).setTooltips("Remove selected player from the list").addButtonEvent(parent -> delPlayer());
        nameField = new TextField(mc, this);

        initRedstoneMode();
        controlPanel.addChildren(blacklist, addButton, delButton, nameField, redstoneMode);
        return controlPanel;
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                setDesiredHeight(16).
                setDesiredWidth(16).
                addChoiceEvent((parent, newChoice) -> changeRedstoneMode()).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    private void changeMode(String newAccess) {
        EnvironmentalControllerTileEntity.EnvironmentalMode newmode;
        if (MODE_ALL.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_ALL;
        } else if (MODE_BLACKLIST.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_BLACKLIST;
        } else if (MODE_WHITELIST.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_WHITELIST;
        } else if (MODE_MOBS.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_MOBS;
        } else if (MODE_PASSIVE.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_PASSIVE;
        } else {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_HOSTILE;
        }
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_SETMODE, new Argument("mode", newmode.ordinal()));
    }

    private void changeRedstoneMode() {
        tileEntity.setRSMode(RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()]);
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
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetPlayers(tileEntity.getPos(), EnvironmentalControllerTileEntity.CMD_GETPLAYERS, EnvironmentalControllerTileEntity.CLIENTCMD_GETPLAYERS));
    }

    private void populatePlayers() {
        players = new ArrayList<>(tileEntity.players);
        players.sort(null);
        playersList.removeChildren();
        for (String player : players) {
            playersList.addChild(new Label(mc, this).setText(player).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
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

        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        drawWindow();
    }

    private void enableButtons() {
        int isPlayerSelected = playersList.getSelected();
        delButton.setEnabled(isPlayerSelected != -1);
        String name = nameField.getText();
        addButton.setEnabled(name != null && !name.isEmpty());
    }
}

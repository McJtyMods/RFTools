package mcjty.rftools.blocks.environmental;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.*;
import mcjty.rftools.RFTools;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.network.PacketGetPlayers;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.lib.typed.TypedMap;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static mcjty.lib.tileentity.GenericEnergyStorageTileEntity.getCurrentRF;
import static mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity.*;

public class GuiEnvironmentalController extends GenericGuiContainer<EnvironmentalControllerTileEntity> {

    public static final String BLACKLIST = "BL";
    public static final String WHITELIST = "WL";
    public static final String HOSTILE = "Host";
    public static final String PASSIVE = "Pass";
    public static final String MOBS = "Mobs";
    public static final String ALL = "All";

    // A copy of the players we're currently showing.
    private List<String> players = null;
    private int listDirty = 0;
    private EnergyBar energyBar;

    private TextField minyTextField;
    private TextField maxyTextField;
    private TextField nameField;
    private WidgetList playersList;
    private ChoiceLabel modeLabel;

    public GuiEnvironmentalController(EnvironmentalControllerTileEntity tileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, tileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "envctrl");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/environmental.gui"));
        super.initGui();

        initializeFields();
        setupEvents();

        listDirty = 0;
        requestPlayers();
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
        energyBar.setMaxValue(tileEntity.getCapacity());
        energyBar.setValue(getCurrentRF());

        ((ImageChoiceLabel)window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());

        int r = tileEntity.getRadius();
        if (r < 5) {
            r = 5;
        } else if (r > 100) {
            r = 100;
        }
        ((ScrollableLabel)window.findChild("radius")).setRealValue(r);

        playersList = window.findChild("players");

        minyTextField = window.findChild("miny");
        maxyTextField = window.findChild("maxy");
        nameField = window.findChild("name");
        modeLabel = window.findChild("mode");

        minyTextField.setText(Integer.toString(tileEntity.getMiny()));
        maxyTextField.setText(Integer.toString(tileEntity.getMaxy()));
        switch (tileEntity.getMode()) {
            case MODE_BLACKLIST:
                modeLabel.setChoice(BLACKLIST);
                break;
            case MODE_WHITELIST:
                modeLabel.setChoice(WHITELIST);
                break;
            case MODE_HOSTILE:
                modeLabel.setChoice(HOSTILE);
                break;
            case MODE_PASSIVE:
                modeLabel.setChoice(PASSIVE);
                break;
            case MODE_MOBS:
                modeLabel.setChoice(MOBS);
                break;
            case MODE_ALL:
                modeLabel.setChoice(ALL);
                break;
        }
    }

    private void setupEvents() {
        window.event("add", (source, params) -> addPlayer());
        window.event("del", (source, params) -> delPlayer());
        window.event("mode", (source, params) -> changeMode(params.get(ChoiceLabel.PARAM_CHOICE)));
        window.event("miny", (source, params) -> sendBounds(true));
        window.event("maxy", (source, params) -> sendBounds(false));
    }


    private void changeMode(String newAccess) {
        EnvironmentalControllerTileEntity.EnvironmentalMode newmode;
        if (ALL.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_ALL;
        } else if (BLACKLIST.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_BLACKLIST;
        } else if (WHITELIST.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_WHITELIST;
        } else if (MOBS.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_MOBS;
        } else if (PASSIVE.equals(newAccess)) {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_PASSIVE;
        } else {
            newmode = EnvironmentalControllerTileEntity.EnvironmentalMode.MODE_HOSTILE;
        }
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_SETMODE,
            TypedMap.builder()
                    .put(PARAM_MODE, newmode.ordinal())
                    .build());
    }

    private void addPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_ADDPLAYER,
                TypedMap.builder().put(PARAM_NAME, nameField.getText()).build());
        listDirty = 0;
    }

    private void delPlayer() {
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_DELPLAYER,
                TypedMap.builder().put(PARAM_NAME, players.get(playersList.getSelected())).build());
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
            playersList.addChild(new Label(mc, this).setText(player).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
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
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_SETBOUNDS,
                TypedMap.builder()
                        .put(PARAM_MIN, miny)
                        .put(PARAM_MAX, maxy)
                        .build());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        requestListsIfNeeded();
        populatePlayers();
        enableButtons();

        long currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        drawWindow();
    }

    private void enableButtons() {
        window.setFlag("selected", playersList.getSelected() != -1);
        String name = nameField.getText();
        window.setFlag("name", name != null && !name.isEmpty());
    }
}

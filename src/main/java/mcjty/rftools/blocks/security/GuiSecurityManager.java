package mcjty.rftools.blocks.security;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class GuiSecurityManager extends GenericGuiContainer<SecurityManagerTileEntity> {
    public static final int SECURITYMANAGER_WIDTH = 244;
    public static final int SECURITYMANAGER_HEIGHT = 206;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/securitymanager.png");

    private WidgetList players;
    private Button addButton;
    private Button delButton;
    private TextField nameField;

    public GuiSecurityManager(SecurityManagerTileEntity securityManagerTileEntity, SecurityManagerContainer container) {
        super(securityManagerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "security");

        xSize = SECURITYMANAGER_WIDTH;
        ySize = SECURITYMANAGER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        players = createStyledList();
        Slider allowedPlayerSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(players);
        Panel allowedPlayersPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(players).addChild(allowedPlayerSlider).
                setLayoutHint(new PositionalLayout.PositionalHint(60, 10, SECURITYMANAGER_WIDTH - 70, 86));

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
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(nameField).addChild(addButton).addChild(delButton).setDesiredHeight(16).
                setLayoutHint(new PositionalLayout.PositionalHint(60, 100, SECURITYMANAGER_WIDTH - 70, 14));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(allowedPlayersPanel).addChild(buttonPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);
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
//        sendServerCommand(MatterTransmitterTileEntity.CMD_ADDPLAYER, new Argument("player", nameField.getText()));
//        listDirty = 0;
        players.addChild(new Label(mc, this).setText(nameField.getText()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
    }

    private void delPlayer() {
//        sendServerCommand(MatterTransmitterTileEntity.CMD_DELPLAYER, new Argument("player", players.get(allowedPlayers.getSelected())));
//        listDirty = 0;
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}

package mcjty.rftools.blocks.environmental;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.events.TextEvent;
import mcjty.gui.events.ValueEvent;
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
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class GuiEnvironmentalController extends GenericGuiContainer<EnvironmentalControllerTileEntity> {
    public static final int ENV_WIDTH = 179;
    public static final int ENV_HEIGHT = 224;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/environmentalcontroller.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private Panel toplevel;
    private TextField minyTextField;
    private TextField maxyTextField;
    private ImageChoiceLabel redstoneMode;
    private WidgetList players;

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

        players = createStyledList();
        Slider playerSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(players);
        Panel playersPanel = new Panel(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(25, 42, ENV_WIDTH - 30, 72))
                .setLayout(new HorizontalLayout().setHorizontalMargin(1)).addChild(players).addChild(playerSlider);

        Panel controlPanel = new Panel(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(25, 118, ENV_WIDTH - 30, 16))
                .setLayout(new HorizontalLayout().setHorizontalMargin(1).setVerticalMargin(0).setSpacing(1));
        ChoiceLabel blacklist = new ChoiceLabel(mc, this).addChoices("BL", "WL")
                .setChoiceTooltip("BL", "Players in the list above will not get the effects")
                .setChoiceTooltip("WL", "Players in the list above will get the effects");
        Button addButton = new Button(mc, this).setText("+").setTooltips("Add a player to the list");
        Button delButton = new Button(mc, this).setText("-").setTooltips("Remove selected player from the list");
        TextField playerField = new TextField(mc, this);

        initRedstoneMode();
        controlPanel.addChild(blacklist).addChild(addButton).addChild(delButton).addChild(playerField).addChild(redstoneMode);

        toplevel.addChild(radiusPanel).addChild(minPanel).addChild(playersPanel).addChild(controlPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);
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
//        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(152, 118, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRedstoneMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRedstoneMode(RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()]);
        sendServerCommand(RFToolsMessages.INSTANCE, EnvironmentalControllerTileEntity.CMD_RSMODE, new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()].getDescription()));
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
        drawWindow();
    }
}

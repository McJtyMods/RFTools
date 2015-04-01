package mcjty.rftools.blocks.environmental;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.events.TextEvent;
import mcjty.gui.events.ValueEvent;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.RedstoneMode;
import mcjty.rftools.network.Argument;
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

    public GuiEnvironmentalController(EnvironmentalControllerTileEntity environmentalControllerTileEntity, EnvironmentalControllerContainer container) {
        super(environmentalControllerTileEntity, container);

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

        Panel radiusPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(25, 10, ENV_WIDTH-30, 16));
        ScrollableLabel radius = new ScrollableLabel(mc, this).setRealMinimum(5).setRealMaximum(100).setRealValue(r).setDesiredWidth(24).addValueEvent(new ValueEvent() {
            @Override
            public void valueChanged(Widget parent, int newValue) {
                sendServerCommand(EnvironmentalControllerTileEntity.CMD_SETRADIUS, new Argument("radius", newValue));
            }
        });
        Slider slider = new Slider(mc, this).setHorizontal().setScrollable(radius);
        radiusPanel.addChild(new Label(mc, this).setText("Radius:")).addChild(slider).addChild(radius);

        Panel minPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(25, 30, ENV_WIDTH-30, 16));
        minyTextField = new TextField(mc, this).setText(Integer.toString(miny)).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                sendBounds(true);
            }
        });
        minPanel.addChild(new Label(mc, this).setText("Minimum height:")).addChild(minyTextField);
        Panel maxPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(25, 50, ENV_WIDTH-30, 16));
        maxyTextField = new TextField(mc, this).setText(Integer.toString(maxy)).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                sendBounds(false);
            }
        });
        maxPanel.addChild(new Label(mc, this).setText("Maximum height:")).addChild(maxyTextField);

        initRedstoneMode();

        toplevel.addChild(radiusPanel).addChild(minPanel).addChild(maxPanel).addChild(redstoneMode);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeRedstoneMode();
                    }
                }).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(152, 118, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRedstoneMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRedstoneMode(RedstoneMode.values()[redstoneMode.getCurrentChoice()]);
        sendServerCommand(EnvironmentalControllerTileEntity.CMD_RSMODE, new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoice()].getDescription()));
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
        sendServerCommand(EnvironmentalControllerTileEntity.CMD_SETBOUNDS, new Argument("miny", miny), new Argument("maxy", maxy));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

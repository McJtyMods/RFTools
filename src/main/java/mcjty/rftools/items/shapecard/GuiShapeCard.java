package mcjty.rftools.items.shapecard;

import mcjty.gui.Window;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.events.TextEvent;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.*;
import mcjty.network.Argument;
import mcjty.network.PacketUpdateNBTItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.varia.Coordinate;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.awt.Rectangle;
import java.util.List;

public class GuiShapeCard extends GuiScreen {

    /** The X size of the window in pixels. */
    protected int xSize = 360;
    /** The Y size of the window in pixels. */
    protected int ySize = 46;

    private ChoiceLabel shapeLabel;
    private TextField dimX;
    private TextField dimY;
    private TextField dimZ;
    private TextField offsetX;
    private TextField offsetY;
    private TextField offsetZ;
    private Window window;
    private Label blocksLabel;

    public GuiShapeCard() {
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        shapeLabel = new ChoiceLabel(mc, this).setDesiredWidth(100).setDesiredHeight(16).addChoices(
                ShapeCardItem.Shape.SHAPE_BOX.getDescription(),
                ShapeCardItem.Shape.SHAPE_TOPDOME.getDescription(),
                ShapeCardItem.Shape.SHAPE_BOTTOMDOME.getDescription(),
                ShapeCardItem.Shape.SHAPE_SPHERE.getDescription(),
                ShapeCardItem.Shape.SHAPE_CYLINDER.getDescription(),
                ShapeCardItem.Shape.SHAPE_CAPPEDCYLINDER.getDescription(),
                ShapeCardItem.Shape.SHAPE_PRISM.getDescription(),
                ShapeCardItem.Shape.SHAPE_TORUS.getDescription(),
                ShapeCardItem.Shape.SHAPE_SOLIDBOX.getDescription(),
                ShapeCardItem.Shape.SHAPE_SOLIDSPHERE.getDescription(),
                ShapeCardItem.Shape.SHAPE_SOLIDCYLINDER.getDescription(),
                ShapeCardItem.Shape.SHAPE_SOLIDTORUS.getDescription()).addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateShape();
            }
        });
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem == null) {
            // Cannot happen!
            return;
        }
        ShapeCardItem.Shape shape = ShapeCardItem.getShape(heldItem);
        shapeLabel.setChoice(shape.getDescription());

        blocksLabel = new Label(mc, this).setText("# ").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        blocksLabel.setDesiredWidth(100).setDesiredHeight(16);

        Panel modePanel = new Panel(mc, this).setLayout(new VerticalLayout()).addChild(shapeLabel).addChild(blocksLabel);

        Coordinate dim = ShapeCardItem.getDimension(heldItem);
        Coordinate offset = ShapeCardItem.getOffset(heldItem);

        dimX = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                if (isTorus()) {
                    dimZ.setText(newText);
                }
                updateSettings();
            }
        }).setText(String.valueOf(dim.getX()));
        dimY = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                updateSettings();
            }
        }).setText(String.valueOf(dim.getY()));
        dimZ = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                updateSettings();
            }
        }).setText(String.valueOf(dim.getZ()));
        Panel dimPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(dimX).addChild(dimY).addChild(dimZ);
        offsetX = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                updateSettings();
            }
        }).setText(String.valueOf(offset.getX()));
        offsetY = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                updateSettings();
            }
        }).setText(String.valueOf(offset.getY()));
        offsetZ = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                updateSettings();
            }
        }).setText(String.valueOf(offset.getZ()));
        Panel offsetPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(offsetX).addChild(offsetY).addChild(offsetZ);

        Panel settingsPanel = new Panel(mc, this).setLayout(new VerticalLayout()).addChild(dimPanel).addChild(offsetPanel);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new HorizontalLayout()).addChild(modePanel).addChild(settingsPanel);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void updateShape() {
        if (isTorus()) {
            dimZ.setText(dimX.getText());
        }
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateNBTItem(
                new Argument("shape", getCurrentShape().getIndex())));

    }

    private boolean isTorus() {
        ShapeCardItem.Shape shape = getCurrentShape();
        return ShapeCardItem.Shape.SHAPE_TORUS.equals(shape) || ShapeCardItem.Shape.SHAPE_SOLIDTORUS.equals(shape);
    }

    private ShapeCardItem.Shape getCurrentShape() {
        return ShapeCardItem.Shape.getShape(shapeLabel.getCurrentChoice());
    }

    private Coordinate getCurrentDimension() {
        return new Coordinate(parseInt(dimX.getText()), parseInt(dimY.getText()), parseInt(dimZ.getText()));
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateSettings() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateNBTItem(
                new Argument("dimX", parseInt(dimX.getText())),
                new Argument("dimY", parseInt(dimY.getText())),
                new Argument("dimZ", parseInt(dimZ.getText())),
                new Argument("offsetX", parseInt(offsetX.getText())),
                new Argument("offsetY", parseInt(offsetY.getText())),
                new Argument("offsetZ", parseInt(offsetZ.getText()))
                ));
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
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        dimZ.setEnabled(!isTorus());
        blocksLabel.setText("#Blocks:" + ShapeCardItem.countBlocks(getCurrentShape(), getCurrentDimension()));

        window.draw();
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }
}
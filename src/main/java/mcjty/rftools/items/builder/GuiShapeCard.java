package mcjty.rftools.items.builder;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.gui.RenderHelper;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.events.ChoiceEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketUpdateNBTItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class GuiShapeCard extends GuiScreen {

    /** The X size of the window in pixels. */
    protected int xSize = 360;
    /** The Y size of the window in pixels. */
    protected int ySize = 46;

    private boolean isQuarryCard;

    private ChoiceLabel shapeLabel;
    private TextField dimX;
    private TextField dimY;
    private TextField dimZ;
    private TextField offsetX;
    private TextField offsetY;
    private TextField offsetZ;
    private Window window;
    private Label blocksLabel;

    private Panel voidPanel;
    private ToggleButton stone;
    private ToggleButton cobble;
    private ToggleButton dirt;
    private ToggleButton gravel;
    private ToggleButton sand;
    private ToggleButton netherrack;
    private ToggleButton oredict;

    private boolean countDirty = true;

    public GuiShapeCard() {
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();

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
                updateSettings();
            }
        });
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem == null) {
            // Cannot happen!
            return;
        }

        isQuarryCard = ShapeCardItem.isQuarry(heldItem.getItemDamage());
        if (isQuarryCard) {
            ySize = 46 + 28;
        }

        ShapeCardItem.Shape shape = ShapeCardItem.getShape(heldItem);
        shapeLabel.setChoice(shape.getDescription());

        blocksLabel = new Label(mc, this).setText("# ").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        blocksLabel.setDesiredWidth(100).setDesiredHeight(16);

        Panel modePanel = new Panel(mc, this).setLayout(new VerticalLayout()).setDesiredWidth(100).addChild(shapeLabel).addChild(blocksLabel);

        BlockPos dim = ShapeCardItem.getDimension(heldItem);
        BlockPos offset = ShapeCardItem.getOffset(heldItem);

        dimX = new TextField(mc, this).addTextEvent((parent, newText) -> {
            if (isTorus()) {
                dimZ.setText(newText);
            }
            updateSettings();
        }).setText(String.valueOf(dim.getX()));
        dimY = new TextField(mc, this).addTextEvent((parent, newText) -> updateSettings()).setText(String.valueOf(dim.getY()));
        dimZ = new TextField(mc, this).addTextEvent((parent, newText) -> updateSettings()).setText(String.valueOf(dim.getZ()));
        Panel dimPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(0)).addChild(new Label(mc, this).setText("Dim:").setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT).setDesiredWidth(70)).setDesiredHeight(18).addChild(dimX).addChild(dimY).addChild(dimZ);
        offsetX = new TextField(mc, this).addTextEvent((parent, newText) -> updateSettings()).setText(String.valueOf(offset.getX()));
        offsetY = new TextField(mc, this).addTextEvent((parent, newText) -> updateSettings()).setText(String.valueOf(offset.getY()));
        offsetZ = new TextField(mc, this).addTextEvent((parent, newText) -> updateSettings()).setText(String.valueOf(offset.getZ()));
        Panel offsetPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(0)).addChild(new Label(mc, this).setText("Offset:").setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT).setDesiredWidth(70)).setDesiredHeight(18).addChild(offsetX).addChild(offsetY).addChild(offsetZ);

        Panel settingsPanel = new Panel(mc, this).setLayout(new VerticalLayout().setSpacing(1).setVerticalMargin(1).setHorizontalMargin(0)).addChild(dimPanel).addChild(offsetPanel);

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        Panel modeSettingsPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(0)).addChild(modePanel).addChild(settingsPanel);
        Widget toplevel;
        if (isQuarryCard) {
            setupVoidPanel(heldItem);
            toplevel = new Panel(mc, this).setLayout(new VerticalLayout()).setFilledRectThickness(2).addChild(modeSettingsPanel).addChild(voidPanel);

        } else {
            toplevel = new Panel(mc, this).setLayout(new VerticalLayout()).setFilledRectThickness(2).addChild(modeSettingsPanel);
        }

        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void setupVoidPanel(ItemStack heldItem) {
        voidPanel = new Panel(mc, this).setLayout(new HorizontalLayout())
                .setDesiredHeight(26)
                .setFilledRectThickness(-2)
                .setFilledBackground(StyleConfig.colorListBackground);
        Label label = new Label(mc, this).setText("Void:");
        stone = new ToggleButton(mc, this).setDesiredWidth(20).setDesiredHeight(20).setTooltips("Void stone").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget widget) {
                updateVoidSettings();
            }
        });
        cobble = new ToggleButton(mc, this).setDesiredWidth(20).setDesiredHeight(20).setTooltips("Void cobble").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget widget) {
                updateVoidSettings();
            }
        });
        dirt = new ToggleButton(mc, this).setDesiredWidth(20).setDesiredHeight(20).setTooltips("Void dirt").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget widget) {
                updateVoidSettings();
            }
        });
        gravel = new ToggleButton(mc, this).setDesiredWidth(20).setDesiredHeight(20).setTooltips("Void gravel").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget widget) {
                updateVoidSettings();
            }
        });
        sand = new ToggleButton(mc, this).setDesiredWidth(20).setDesiredHeight(20).setTooltips("Void sand").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget widget) {
                updateVoidSettings();
            }
        });
        netherrack = new ToggleButton(mc, this).setDesiredWidth(20).setDesiredHeight(20).setTooltips("Void netherrack").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget widget) {
                updateVoidSettings();
            }
        });
        oredict = new ToggleButton(mc, this).setDesiredWidth(60).setDesiredHeight(15).setTooltips("Enable ore dictionary matching")
                .setText("Oredict")
                .setCheckMarker(true)
                .addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget widget) {
                updateVoidSettings();
            }
        });

        stone.setPressed(ShapeCardItem.isVoiding(heldItem, "stone"));
        cobble.setPressed(ShapeCardItem.isVoiding(heldItem, "cobble"));
        dirt.setPressed(ShapeCardItem.isVoiding(heldItem, "dirt"));
        gravel.setPressed(ShapeCardItem.isVoiding(heldItem, "gravel"));
        sand.setPressed(ShapeCardItem.isVoiding(heldItem, "sand"));
        netherrack.setPressed(ShapeCardItem.isVoiding(heldItem, "netherrack"));
        oredict.setPressed(ShapeCardItem.isOreDictionary(heldItem));

        voidPanel.addChild(label).addChild(stone).addChild(cobble).addChild(dirt).addChild(gravel).addChild(sand).addChild(netherrack).addChild(oredict);
    }

    private boolean isTorus() {
        ShapeCardItem.Shape shape = getCurrentShape();
        return ShapeCardItem.Shape.SHAPE_TORUS.equals(shape) || ShapeCardItem.Shape.SHAPE_SOLIDTORUS.equals(shape);
    }

    private ShapeCardItem.Shape getCurrentShape() {
        return ShapeCardItem.Shape.getShape(shapeLabel.getCurrentChoice());
    }

    private BlockPos getCurrentDimension() {
        return new BlockPos(parseInt(dimX.getText()), parseInt(dimY.getText()), parseInt(dimZ.getText()));
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateSettings() {
        countDirty = true;
        if (isTorus()) {
            dimZ.setText(dimX.getText());
        }
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateNBTItem(
                new Argument("shape", getCurrentShape().getIndex()),
                new Argument("dimX", parseInt(dimX.getText())),
                new Argument("dimY", parseInt(dimY.getText())),
                new Argument("dimZ", parseInt(dimZ.getText())),
                new Argument("offsetX", parseInt(offsetX.getText())),
                new Argument("offsetY", parseInt(offsetY.getText())),
                new Argument("offsetZ", parseInt(offsetZ.getText()))
        ));
    }

    private void updateVoidSettings() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateNBTItem(
                new Argument("voidstone", stone.isPressed()),
                new Argument("voidcobble", cobble.isPressed()),
                new Argument("voiddirt", dirt.isPressed()),
                new Argument("voidgravel", gravel.isPressed()),
                new Argument("voidsand", sand.isPressed()),
                new Argument("voidnetherrack", netherrack.isPressed()),
                new Argument("oredict", oredict.isPressed())
        ));
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        window.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

    private static int updateCounter = 20;

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        dimZ.setEnabled(!isTorus());

        updateCounter--;
        if (updateCounter <= 0) {
            updateCounter = 10;
            if (countDirty) {
                countDirty = false;
                int count = ShapeCardItem.countBlocks(getCurrentShape(), getCurrentDimension());
                if (count >= ShapeCardItem.MAXIMUM_COUNT) {
                    blocksLabel.setText("#Blocks: ++" + count);
                } else {
                    blocksLabel.setText("#Blocks: " + count);
                }
            }
        }

        window.draw();

        if (isQuarryCard) {
            // @@@ Hacky code!
            int x = (int) (window.getToplevel().getBounds().getX() + voidPanel.getBounds().getX()) + 1;
            int y = (int) (window.getToplevel().getBounds().getY() + voidPanel.getBounds().getY() + stone.getBounds().getY()) + 1;

            renderVoidBlock(x, y, stone, Blocks.stone);
            renderVoidBlock(x, y, cobble, Blocks.cobblestone);
            renderVoidBlock(x, y, dirt, Blocks.dirt);
            renderVoidBlock(x, y, gravel, Blocks.gravel);
            renderVoidBlock(x, y, sand, Blocks.sand);
            renderVoidBlock(x, y, netherrack, Blocks.netherrack);
        }

        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRendererObj);
        }
    }

    private void renderVoidBlock(int x, int y, ToggleButton button, Block block) {
        x += (int) button.getBounds().getX();
        RenderHelper.renderObject(Minecraft.getMinecraft(), x, y, new ItemStack(block), button.isPressed());
        if (button.isPressed()) {
            drawLine(x-1, y-1, x+18, y+18, 0xffff0000);
            drawLine(x+18, y-1, x-1, y+18, 0xffff0000);
        }
    }

    private static void drawLine(int x1, int y1, int x2, int y2, int color) {
        float f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GL11.glLineWidth(2.0f);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        renderer.pos(x1, y1, 0.0D).endVertex();
        renderer.pos(x2, y2, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

}
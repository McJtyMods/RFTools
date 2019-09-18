package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.*;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.GuiShapeCard;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.shapes.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiComposer extends GenericGuiContainer<ComposerTileEntity, GenericContainer> implements IShapeParentGui {
    public static final int SIDEWIDTH = 80;
    public static final int SHAPER_WIDTH = 256;
    public static final int SHAPER_HEIGHT = 238;

    private static final ResourceLocation sideBackground = new ResourceLocation(RFTools.MODID, "textures/gui/sidegui.png");
    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/composer.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ChoiceLabel operationLabels[] = new ChoiceLabel[ComposerTileEntity.SLOT_COUNT];
    private ChoiceLabel rotationLabels[] = new ChoiceLabel[ComposerTileEntity.SLOT_COUNT];
    private ToggleButton flipButtons[] = new ToggleButton[ComposerTileEntity.SLOT_COUNT];
    private Button configButton[] = new Button[ComposerTileEntity.SLOT_COUNT];
    private Button outConfigButton;

    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showScan;

    private ShapeRenderer shapeRenderer = null;

    private Window sideWindow;


    public GuiComposer(ComposerTileEntity composerTileEntity, GenericContainer container, PlayerInventory playerInventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, composerTileEntity, container, playerInventory, GuiProxy.GUI_MANUAL_SHAPE, "composer");

        xSize = SHAPER_WIDTH;
        ySize = SHAPER_HEIGHT;
    }

    private ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer(getShapeID());
        }
        return shapeRenderer;
    }

    private ShapeID getShapeID() {
        Slot slot = container.getSlot(ComposerTileEntity.SLOT_OUT);
        ItemStack stack = slot.getHasStack() ? slot.getStack() : ItemStack.EMPTY;

        return new ShapeID(tileEntity.getWorld().getDimension().getType(), tileEntity.getPos(), ShapeCardItem.getScanId(stack), false, ShapeCardItem.isSolid(stack));
    }


    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel(minecraft, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        getShapeRenderer().initView(getPreviewLeft(), guiTop+100);

        ShapeModifier[] modifiers = tileEntity.getModifiers();

        operationLabels[0] = null;
        for (int i = 0; i < ComposerTileEntity.SLOT_COUNT ; i++) {
            operationLabels[i] = new ChoiceLabel(minecraft, this).addChoices(
                    ShapeOperation.UNION.getCode(),
                    ShapeOperation.SUBTRACT.getCode(),
                    ShapeOperation.INTERSECT.getCode());
            for (ShapeOperation operation : ShapeOperation.values()) {
                operationLabels[i].setChoiceTooltip(operation.getCode(), operation.getDescription());
            }
            operationLabels[i].setLayoutHint(new PositionalLayout.PositionalHint(55, 7 + i*18, 26, 16));
            operationLabels[i].setChoice(modifiers[i].getOperation().getCode());
            operationLabels[i].addChoiceEvent((parent, newChoice) -> update());
            toplevel.addChild(operationLabels[i]);
        }
        operationLabels[0].setEnabled(false);

        for (int i = 0; i < ComposerTileEntity.SLOT_COUNT ; i++) {
            configButton[i] = new Button(minecraft, this).setText("?").setChannel("config" + i);
            configButton[i].setLayoutHint(new PositionalLayout.PositionalHint(3, 7 + i*18+2, 13, 12));
            configButton[i].setTooltips("Click to open the card gui");
            toplevel.addChild(configButton[i]);
        }
        outConfigButton = new Button(minecraft, this).setText("?").setChannel("outconfig");
        outConfigButton.setLayoutHint(new PositionalLayout.PositionalHint(3, 200+2, 13, 12));
        outConfigButton.setTooltips("Click to open the card gui");
        toplevel.addChild(outConfigButton);

        showAxis = ShapeGuiTools.createAxisButton(this, toplevel, 5, 176);
        showOuter = ShapeGuiTools.createBoxButton(this, toplevel, 31, 176);
        showScan = ShapeGuiTools.createScanButton(this, toplevel, 57, 176);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        Panel sidePanel = new Panel(minecraft, this).setLayout(new PositionalLayout()).setBackground(sideBackground);
        String[] tt = {
                "Drag left mouse button to rotate",
                "Shift drag left mouse to pan",
                "Use mouse wheel to zoom in/out",
                "Use middle click to reset rotation" };
        sidePanel.addChild(new Label(minecraft, this).setText("E").setColor(0xffff0000).setTooltips(tt).setLayoutHint(5, 175, 15, 15));
        sidePanel.addChild(new Label(minecraft, this).setText("W").setColor(0xffff0000).setTooltips(tt).setLayoutHint(40, 175, 15, 15));
        sidePanel.addChild(new Label(minecraft, this).setText("U").setColor(0xff00bb00).setTooltips(tt).setLayoutHint(5, 190, 15, 15));
        sidePanel.addChild(new Label(minecraft, this).setText("D").setColor(0xff00bb00).setTooltips(tt).setLayoutHint(40, 190, 15, 15));
        sidePanel.addChild(new Label(minecraft, this).setText("N").setColor(0xff0000ff).setTooltips(tt).setLayoutHint(5, 205, 15, 15));
        sidePanel.addChild(new Label(minecraft, this).setText("S").setColor(0xff0000ff).setTooltips(tt).setLayoutHint(40, 205, 15, 15));

        for (int i = 0; i < ComposerTileEntity.SLOT_COUNT ; i++) {
            ToggleButton flip = new ToggleButton(minecraft, this).setText("Flip").setCheckMarker(true).setLayoutHint(new PositionalLayout.PositionalHint(6, 7 + i*18, 35, 16));
            flip.setPressed(modifiers[i].isFlipY());
            flip.addButtonEvent(parent -> update());
            sidePanel.addChild(flip);
            flipButtons[i] = flip;
            ChoiceLabel rot = new ChoiceLabel(minecraft, this).addChoices("None", "X", "Y", "Z").setChoice("None").setLayoutHint(new PositionalLayout.PositionalHint(45, 7 + i*18, 35, 16));
            rot.setChoice(modifiers[i].getRotation().getCode());
            rot.addChoiceEvent((parent, newChoice) -> update());
            sidePanel.addChild(rot);
            rotationLabels[i] = rot;
        }

        sidePanel.setBounds(new Rectangle(guiLeft-SIDEWIDTH, guiTop, SIDEWIDTH, ySize));
        sideWindow = new Window(this, sidePanel);

        window = new Window(this, toplevel);

        for (int i = 0; i < ComposerTileEntity.SLOT_COUNT ; i++) {
            int finalI1 = i;
            window.event("config" + i, (source, params) -> openCardGui(finalI1));
        }
        window.event("outconfig", (source, params) -> openCardGui(-1));

    }


    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
    }

    @Override
    public int getPreviewLeft() {
        return getGuiLeft();
    }

    @Override
    public int getPreviewTop() {
        return getGuiTop();
    }

    private void openCardGui(int i) {
        int slot;
        if (i == -1) {
            slot = ComposerTileEntity.SLOT_OUT;
        } else {
            slot = ComposerTileEntity.SLOT_TABS+i;
        }
        ItemStack cardStack = container.getSlot(slot).getStack();
        if (!cardStack.isEmpty()) {
            PlayerEntity player = Minecraft.getInstance().player;
            GuiShapeCard.fromTEPos = tileEntity.getPos();
            GuiShapeCard.fromTEStackSlot = slot;
            GuiShapeCard.returnGui = this;
            // @todo 1.14
//            player.openGui(RFTools.instance, GuiProxy.GUI_SHAPECARD_COMPOSER, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

    private void update() {
        ShapeModifier[] modifiers = new ShapeModifier[ComposerTileEntity.SLOT_COUNT];
        for (int i = 0; i < ComposerTileEntity.SLOT_COUNT ; i++) {
            ShapeOperation op = ShapeOperation.getByName(operationLabels[i].getCurrentChoice());
            ShapeRotation rot = ShapeRotation.getByName(rotationLabels[i].getCurrentChoice());
            modifiers[i] = new ShapeModifier(op, flipButtons[i].isPressed(), rot);
        }
        network.sendToServer(new PacketSendComposerData(tileEntity.getPos(), modifiers));
    }

    // @todo 1.14
//    @Override
//    public void handleMouseInput() throws IOException {
//        super.handleMouseInput();
//        int x = Mouse.getEventX() * width / mc.displayWidth;
//        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
//        x -= guiLeft;
//        y -= guiTop;
//
//        getShapeRenderer().handleShapeDragging(x, y);
//    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int x, int y) {

        getShapeRenderer().handleMouseWheel();

        drawWindow();

        Slot slot = container.getSlot(ComposerTileEntity.SLOT_OUT);
        if (slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                getShapeRenderer().setShapeID(getShapeID());
                getShapeRenderer().renderShape(this, stack, guiLeft, guiTop, showAxis.isPressed(), showOuter.isPressed(), showScan.isPressed(), true);
            }
        }
    }

}

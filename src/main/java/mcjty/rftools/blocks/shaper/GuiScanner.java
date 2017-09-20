package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.IShapeParentGui;
import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiScanner extends GenericGuiContainer<ScannerTileEntity> implements IShapeParentGui {

    public static final int SCANNER_WIDTH = 256;
    public static final int SCANNER_HEIGHT = 238;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/scanner.png");

    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showMat;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private int filterCnt = 0;

    public GuiScanner(ScannerTileEntity shaperTileEntity, ScannerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, RFTools.GUI_MANUAL_MAIN, "scanner");

        xSize = SCANNER_WIDTH;
        ySize = SCANNER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());


        showAxis = new ToggleButton(mc, this).setCheckMarker(true).setText("A").setLayoutHint(new PositionalLayout.PositionalHint(5, 176, 24, 16));
        showAxis.setPressed(true);
        toplevel.addChild(showAxis);
        showOuter = new ToggleButton(mc, this).setCheckMarker(true).setText("B").setLayoutHint(new PositionalLayout.PositionalHint(31, 176, 24, 16));
        showOuter.setPressed(true);
        toplevel.addChild(showOuter);
        showMat = new ToggleButton(mc, this).setCheckMarker(true).setText("M").setLayoutHint(new PositionalLayout.PositionalHint(57, 176, 24, 16));
        showMat.setPressed(true);
        toplevel.addChild(showMat);

        toplevel.addChild(new Button(mc, this).setText("W").addButtonEvent(parent -> move(-16, 0, 0)).setLayoutHint(new PositionalLayout.PositionalHint(4, 30, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("w").addButtonEvent(parent -> move(-1, 0, 0)).setLayoutHint(new PositionalLayout.PositionalHint(20, 30, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("e").addButtonEvent(parent -> move(1, 0, 0)).setLayoutHint(new PositionalLayout.PositionalHint(45, 30, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("E").addButtonEvent(parent -> move(16, 0, 0)).setLayoutHint(new PositionalLayout.PositionalHint(61, 30, 16, 15)));

        toplevel.addChild(new Button(mc, this).setText("S").addButtonEvent(parent -> move(0, 0, -16)).setLayoutHint(new PositionalLayout.PositionalHint(4, 50, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("s").addButtonEvent(parent -> move(0, 0, -1)).setLayoutHint(new PositionalLayout.PositionalHint(20, 50, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("n").addButtonEvent(parent -> move(0, 0, 1)).setLayoutHint(new PositionalLayout.PositionalHint(45, 50, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("N").addButtonEvent(parent -> move(0, 0, 16)).setLayoutHint(new PositionalLayout.PositionalHint(61, 50, 16, 15)));

        toplevel.addChild(new Button(mc, this).setText("D").addButtonEvent(parent -> move(0, -16, 0)).setLayoutHint(new PositionalLayout.PositionalHint(4, 70, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("d").addButtonEvent(parent -> move(0, -1, 0)).setLayoutHint(new PositionalLayout.PositionalHint(20, 70, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("u").addButtonEvent(parent -> move(0, 1, 0)).setLayoutHint(new PositionalLayout.PositionalHint(45, 70, 16, 15)));
        toplevel.addChild(new Button(mc, this).setText("U").addButtonEvent(parent -> move(0, 16, 0)).setLayoutHint(new PositionalLayout.PositionalHint(61, 70, 16, 15)));

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        move(0, 0, 0);

        filterCnt = countFilters();
    }

    private int countFilters() {
        int cnt = 0;
        if (inventorySlots.getSlot(ScannerContainer.SLOT_IN).getHasStack()) {
            cnt++;
        }
        if (inventorySlots.getSlot(ScannerContainer.SLOT_FILTER).getHasStack()) {
            cnt++;
        }
        if (inventorySlots.getSlot(ScannerContainer.SLOT_MODIFIER).getHasStack()) {
            cnt++;
        }
        return cnt;
    }

    private void move(int x, int y, int z) {
        Slot slot = inventorySlots.getSlot(ScannerContainer.SLOT_OUT);
        if (slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            if (ItemStackTools.isValid(stack)) {
                BlockPos offset = ShapeCardItem.getOffset(stack);
                int offsetX = offset.getX() + x;
                int offsetY = offset.getY() + y;
                int offsetZ = offset.getZ() + z;
                sendServerCommand(network, ScannerTileEntity.CMD_SCAN, new Argument("offsetX", offsetX), new Argument("offsetY", offsetY), new Argument("offsetZ", offsetZ));
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        x -= guiLeft;
        y -= guiTop;

        shapeRenderer.handleShapeDragging(x, y);
    }

    @Override
    public int getPreviewLeft() {
        return getGuiLeft();
    }

    @Override
    public int getPreviewTop() {
        return getGuiTop();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int x, int y) {
        drawWindow();

        Slot slot = inventorySlots.getSlot(ScannerContainer.SLOT_OUT);
        if (slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            if (ItemStackTools.isValid(stack)) {
                int cnt = countFilters();
                if (cnt != filterCnt) {
                    filterCnt = cnt;
                    move(0, 0, 0);
                }

                shapeRenderer.renderShape(this, stack, guiLeft, guiTop, showAxis.isPressed(), showOuter.isPressed(), showMat.isPressed());
            }
        }
    }

}

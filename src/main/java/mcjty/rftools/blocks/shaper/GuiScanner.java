package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiScanner extends GenericGuiContainer<ScannerTileEntity> {

    public static final int SCANNER_WIDTH = 256;
    public static final int SCANNER_HEIGHT = 238;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/scanner.png");

    private ToggleButton showAxis;
    private ToggleButton showOuter;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();


    public GuiScanner(ScannerTileEntity shaperTileEntity, ScannerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, RFTools.GUI_MANUAL_MAIN, "scanner");

        xSize = SCANNER_WIDTH;
        ySize = SCANNER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());


        showAxis = new ToggleButton(mc, this).setCheckMarker(true).setText("Axis").setLayoutHint(new PositionalLayout.PositionalHint(3, 176, 38, 16));
        showAxis.setPressed(true);
        toplevel.addChild(showAxis);
        showOuter = new ToggleButton(mc, this).setCheckMarker(true).setText("Box").setLayoutHint(new PositionalLayout.PositionalHint(42, 176, 38, 16));
        showOuter.setPressed(true);
        toplevel.addChild(showOuter);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
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
    protected void drawGuiContainerBackgroundLayer(float v, int x, int y) {
        drawWindow();

        Slot slot = inventorySlots.getSlot(ScannerContainer.SLOT_OUT);
        if (slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            if (ItemStackTools.isValid(stack)) {
                shapeRenderer.renderShape(this, stack, guiLeft, guiTop, showAxis.isPressed(), showOuter.isPressed());
            }
        }
    }

}

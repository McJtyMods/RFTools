package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.IShapeParentGui;
import mcjty.rftools.shapes.ShapeID;
import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;

import java.awt.Rectangle;
import java.io.IOException;

public class GuiScanner extends GenericGuiContainer<ScannerTileEntity> implements IShapeParentGui {

    public static final int SCANNER_WIDTH = 256;
    public static final int SCANNER_HEIGHT = 238;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/scanner.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private EnergyBar energyBar;

    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showScan;

    private Button scanButton;

    private Label offsetLabel;
    private Label dimensionLabel;
    private Label progressLabel;

    private ShapeRenderer shapeRenderer = null;
    private int filterCnt = 0;

    public GuiScanner(ScannerTileEntity shaperTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, GuiProxy.GUI_MANUAL_SHAPE, "scanner");

        xSize = SCANNER_WIDTH;
        ySize = SCANNER_HEIGHT;
    }

    public GuiScanner(RemoteScannerTileEntity shaperTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, GuiProxy.GUI_MANUAL_SHAPE, "remote_scanner");

        xSize = SCANNER_WIDTH;
        ySize = SCANNER_HEIGHT;
    }

    private ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer(getShapeID());
        }
        return shapeRenderer;
    }

    private ShapeID getShapeID() {
        ItemStack renderStack = tileEntity.getRenderStack();
        return new ShapeID(0, null, ShapeCardItem.getScanId(renderStack), false, ShapeCardItem.isSolid(renderStack));
    }


    @Override
    public void initGui() {
        super.initGui();

        getShapeRenderer().initView(getPreviewLeft(), guiTop+100);

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        long maxEnergyStored = tileEntity.getCapacity();
        energyBar = new EnergyBar(mc, this).setHorizontal().setMaxValue(maxEnergyStored).setLayoutHint(8, 120, 70, 10).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        toplevel.addChild(energyBar);

        ImageChoiceLabel redstoneMode = initRedstoneMode();
        toplevel.addChild(redstoneMode);

        showAxis = ShapeGuiTools.createAxisButton(this, toplevel, 5, 176);
        showOuter = ShapeGuiTools.createBoxButton(this, toplevel, 31, 176);
        showScan = ShapeGuiTools.createScanButton(this, toplevel, 57, 176);

        scanButton = new Button(mc, this)
                .setName("scan")
                .setText("Scan")
                .setLayoutHint(5, 156, 40, 16);
        toplevel.addChild(scanButton);

        toplevel.addChild(new Button(mc, this).setText("W").addButtonEvent(parent -> move(-16, 0, 0)).setLayoutHint(4, 30, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("w").addButtonEvent(parent -> move(-1, 0, 0)).setLayoutHint(20, 30, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("e").addButtonEvent(parent -> move(1, 0, 0)).setLayoutHint(45, 30, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("E").addButtonEvent(parent -> move(16, 0, 0)).setLayoutHint(61, 30, 16, 15));

        toplevel.addChild(new Button(mc, this).setText("S").addButtonEvent(parent -> move(0, 0, -16)).setLayoutHint(4, 50, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("s").addButtonEvent(parent -> move(0, 0, -1)).setLayoutHint(20, 50, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("n").addButtonEvent(parent -> move(0, 0, 1)).setLayoutHint(45, 50, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("N").addButtonEvent(parent -> move(0, 0, 16)).setLayoutHint(61, 50, 16, 15));

        toplevel.addChild(new Button(mc, this).setText("D").addButtonEvent(parent -> move(0, -16, 0)).setLayoutHint(4, 70, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("d").addButtonEvent(parent -> move(0, -1, 0)).setLayoutHint(20, 70, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("u").addButtonEvent(parent -> move(0, 1, 0)).setLayoutHint(45, 70, 16, 15));
        toplevel.addChild(new Button(mc, this).setText("U").addButtonEvent(parent -> move(0, 16, 0)).setLayoutHint(61, 70, 16, 15));

        offsetLabel = new Label(mc, this).setText("Off: " + BlockPosTools.toString(tileEntity.getDataOffset())).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        offsetLabel.setLayoutHint(4, 90, 80, 14);
        toplevel.addChild(offsetLabel);
        dimensionLabel = new Label(mc, this).setText("Dim: " + BlockPosTools.toString(tileEntity.getDataDim())).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        dimensionLabel.setLayoutHint(4, 105, 80, 14);
        toplevel.addChild(dimensionLabel);
        progressLabel = new Label(mc, this).setText("");
        progressLabel.setLayoutHint(4, 135, 80, 14);
        toplevel.addChild(progressLabel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        move(0, 0, 0);

        filterCnt = countFilters();
        tileEntity.requestRfFromServer(RFTools.MODID);

        window.action(RFToolsMessages.INSTANCE, "scan", tileEntity, ScannerTileEntity.ACTION_SCAN);
        window.bind(RFToolsMessages.INSTANCE, "redstone", tileEntity, GenericTileEntity.VALUE_RSMODE.getName());
        window.bind(RFToolsMessages.INSTANCE, "offset", tileEntity, ScannerTileEntity.VALUE_OFFSET.getName());
    }

    private ImageChoiceLabel initRedstoneMode() {
        ImageChoiceLabel redstoneMode = new ImageChoiceLabel(mc, this).
                setName("redstone").
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(50, 156, 16, 16);
        return redstoneMode;
    }

    private int countFilters() {
        int cnt = 0;
        if (inventorySlots.getSlot(ScannerTileEntity.SLOT_IN).getHasStack()) {
            cnt++;
        }
        if (inventorySlots.getSlot(ScannerTileEntity.SLOT_FILTER).getHasStack()) {
            cnt++;
        }
        if (inventorySlots.getSlot(ScannerTileEntity.SLOT_MODIFIER).getHasStack()) {
            cnt++;
        }
        return cnt;
    }

    private void move(int x, int y, int z) {
        BlockPos offset = tileEntity.getDataOffset();
        int offsetX = offset.getX() + x;
        int offsetY = offset.getY() + y;
        int offsetZ = offset.getZ() + z;
        tileEntity.valueToServer(network, ScannerTileEntity.VALUE_OFFSET, new BlockPos(offsetX, offsetY, offsetZ));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        x -= guiLeft;
        y -= guiTop;

        getShapeRenderer().handleShapeDragging(x, y);
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

        getShapeRenderer().handleMouseWheel();

        offsetLabel.setText("Off: " + BlockPosTools.toString(tileEntity.getDataOffset()));
        dimensionLabel.setText("Dim: " + BlockPosTools.toString(tileEntity.getDataDim()));

        drawWindow();

        long currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        boolean instack = inventorySlots.getSlot(ScannerTileEntity.SLOT_IN).getHasStack();
        if (currentRF < ScannerConfiguration.SCANNER_PERTICK) {
            instack = false;
        }
        if (tileEntity.getScanProgress() >= 0) {
            instack = false;
            progressLabel.setText(tileEntity.getScanProgress() + "%");
        } else {
            progressLabel.setText("");
        }
        scanButton.setEnabled(instack);

        ItemStack stack = tileEntity.getRenderStack();
        if (!stack.isEmpty()) {
            int cnt = countFilters();
            if (cnt != filterCnt) {
                filterCnt = cnt;
                move(0, 0, 0);
            }

            getShapeRenderer().setShapeID(getShapeID());
            getShapeRenderer().renderShape(this, stack, guiLeft, guiTop, showAxis.isPressed(), showOuter.isPressed(), showScan.isPressed(), false);
        }
    }

}

package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.IShapeParentGui;
import mcjty.rftools.shapes.ShapeID;
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
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private EnergyBar energyBar;
    private ImageChoiceLabel redstoneMode;

    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showScan;

    private Button scanButton;

    private Label offsetLabel;
    private Label dimensionLabel;
    private Label progressLabel;

    private ShapeRenderer shapeRenderer = null;
    private int filterCnt = 0;

    public GuiScanner(ScannerTileEntity shaperTileEntity, ScannerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, RFTools.GUI_MANUAL_SHAPE, "scanner");

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
        return new ShapeID(0, null, ShapeCardItem.getScanId(tileEntity.getRenderStack()), false);
    }


    @Override
    public void initGui() {
        super.initGui();

        getShapeRenderer().initView(250, 70);

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        int maxEnergyStored = tileEntity.getMaxEnergyStored();
        energyBar = new EnergyBar(mc, this).setHorizontal().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(8, 120, 70, 10)).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        toplevel.addChild(energyBar);

        initRedstoneMode();
        toplevel.addChild(redstoneMode);

        showAxis = ShapeGuiTools.createAxisButton(this, toplevel, 5, 176);
        showOuter = ShapeGuiTools.createBoxButton(this, toplevel, 31, 176);
        showScan = ShapeGuiTools.createScanButton(this, toplevel, 57, 176);

        scanButton = new Button(mc, this).setText("Scan")
                .addButtonEvent(parent -> scan())
                .setLayoutHint(new PositionalLayout.PositionalHint(5, 156, 40, 16));
        toplevel.addChild(scanButton);

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

        offsetLabel = new Label(mc, this).setText("Off: " + BlockPosTools.toString(tileEntity.getDataOffset())).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        offsetLabel.setLayoutHint(new PositionalLayout.PositionalHint(4, 90, 80, 14));
        toplevel.addChild(offsetLabel);
        dimensionLabel = new Label(mc, this).setText("Dim: " + BlockPosTools.toString(tileEntity.getDataDim())).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        dimensionLabel.setLayoutHint(new PositionalLayout.PositionalHint(4, 105, 80, 14));
        toplevel.addChild(dimensionLabel);
        progressLabel = new Label(mc, this).setText("");
        progressLabel.setLayoutHint(new PositionalLayout.PositionalHint(4, 135, 80, 14));
        toplevel.addChild(progressLabel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        move(0, 0, 0);

        filterCnt = countFilters();
        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent((parent, newChoice) -> changeRedstoneMode()).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(50, 156, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRSMode(RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()]);
        sendServerCommand(RFToolsMessages.INSTANCE, ScannerTileEntity.CMD_MODE,
                new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()].getDescription()));
    }

    private void scan() {
        sendServerCommand(network, ScannerTileEntity.CMD_SCAN);
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
        BlockPos offset = tileEntity.getDataOffset();
        int offsetX = offset.getX() + x;
        int offsetY = offset.getY() + y;
        int offsetZ = offset.getZ() + z;
        sendServerCommand(network, ScannerTileEntity.CMD_SETOFFSET, new Argument("offsetX", offsetX), new Argument("offsetY", offsetY), new Argument("offsetZ", offsetZ));
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

        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        boolean instack = inventorySlots.getSlot(ScannerContainer.SLOT_IN).getHasStack();
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

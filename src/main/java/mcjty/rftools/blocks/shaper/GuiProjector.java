package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.IShapeParentGui;
import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiProjector extends GenericGuiContainer<ProjectorTileEntity> implements IShapeParentGui {

    public static final int PROJECTOR_WIDTH = 256;
    public static final int PROJECTOR_HEIGHT = 238;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/projector.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private EnergyBar energyBar;
    private ImageChoiceLabel redstoneMode;

    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showMat;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private int filterCnt = 0;

    public GuiProjector(ProjectorTileEntity te, ProjectorContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, RFTools.GUI_MANUAL_MAIN, "projector");

        xSize = PROJECTOR_WIDTH;
        ySize = PROJECTOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        shapeRenderer.initView(250, 70);

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        int maxEnergyStored = tileEntity.getMaxEnergyStored();
        energyBar = new EnergyBar(mc, this).setHorizontal().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(8, 120, 70, 10)).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        toplevel.addChild(energyBar);

        initRedstoneMode();
        toplevel.addChild(redstoneMode);

        showAxis = new ToggleButton(mc, this).setCheckMarker(true).setText("A").setLayoutHint(new PositionalLayout.PositionalHint(5, 176, 24, 16));
        showAxis.setPressed(true);
        toplevel.addChild(showAxis);
        showOuter = new ToggleButton(mc, this).setCheckMarker(true).setText("B").setLayoutHint(new PositionalLayout.PositionalHint(31, 176, 24, 16));
        showOuter.setPressed(true);
        toplevel.addChild(showOuter);
        showMat = new ToggleButton(mc, this).setCheckMarker(true).setText("M").setLayoutHint(new PositionalLayout.PositionalHint(57, 176, 24, 16));
        showMat.setPressed(true);
        toplevel.addChild(showMat);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

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

        shapeRenderer.handleMouseWheel();

        drawWindow();

        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        ItemStack stack = tileEntity.getRenderStack();
        if (ItemStackTools.isValid(stack)) {
            shapeRenderer.renderShape(this, stack, guiLeft, guiTop, showAxis.isPressed(), showOuter.isPressed(), showMat.isPressed());
        }
    }

}

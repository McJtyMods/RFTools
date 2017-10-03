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
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.IShapeParentGui;
import mcjty.rftools.shapes.ShapeID;
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

    private ScrollableLabel angleLabel;
    private ScrollableLabel offsetLabel;
    private ScrollableLabel scaleLabel;
    private Slider angleSlider;
    private Slider offsetSlider;
    private Slider scaleSlider;
    private ToggleButton autoRotate;

    private ShapeRenderer shapeRenderer = null;

    public GuiProjector(ProjectorTileEntity te, ProjectorContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, RFTools.GUI_MANUAL_MAIN, "projector");

        xSize = PROJECTOR_WIDTH;
        ySize = PROJECTOR_HEIGHT;
    }

    private ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer(getShapeID());
        }
        return shapeRenderer;
    }

    private ShapeID getShapeID() {
        int scanId = ShapeCardItem.getScanId(tileEntity.getRenderStack());
        if (scanId == 0) {
            return new ShapeID(tileEntity.getWorld().provider.getDimension(), tileEntity.getPos(), scanId);
        } else {
            return new ShapeID(0, null, scanId);
        }
    }


    @Override
    public void initGui() {
        super.initGui();

        getShapeRenderer().initView(250, 70);

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        int maxEnergyStored = tileEntity.getMaxEnergyStored();
        energyBar = new EnergyBar(mc, this).setHorizontal().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(26, 184, 55, 10)).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        toplevel.addChild(energyBar);

        initRedstoneMode();
        toplevel.addChild(redstoneMode);

        Label angleI = new Label(mc, this).setText("Angle:");
        angleI.setLayoutHint(new PositionalLayout.PositionalHint(23, 30, 18, 15));
        angleLabel = new ScrollableLabel(mc, this).setRealMinimum(0).setRealMaximum(360)
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT)
                .setLayoutHint(new PositionalLayout.PositionalHint(32, 30, 34, 15));
        angleLabel.setRealValue(tileEntity.getAngleInt());
        Button angleM = new Button(mc, this).setText("-").setLayoutHint(new PositionalLayout.PositionalHint(5, 30, 10, 15))
                .addButtonEvent(parent -> min(angleLabel));
        Button angleP = new Button(mc, this).setText("+").setLayoutHint(new PositionalLayout.PositionalHint(70, 30, 10, 15))
                .addButtonEvent(parent -> plus(angleLabel));
        angleSlider = new Slider(mc, this).setHorizontal().setScrollable(angleLabel)
                .setLayoutHint(new PositionalLayout.PositionalHint(5, 46, 76, 15));
        toplevel.addChild(angleI).addChild(angleLabel).addChild(angleSlider).addChild(angleM).addChild(angleP);

        Label scaleI = new Label(mc, this).setText("Scale:");
        scaleI.setLayoutHint(new PositionalLayout.PositionalHint(23, 62, 18, 15));
        scaleLabel = new ScrollableLabel(mc, this).setRealMinimum(0).setRealMaximum(100)
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT)
                .setLayoutHint(new PositionalLayout.PositionalHint(32, 62, 34, 15));
        scaleLabel.setRealValue(tileEntity.getScaleInt());
        Button scaleM = new Button(mc, this).setText("-").setLayoutHint(new PositionalLayout.PositionalHint(5, 62, 10, 15))
                .addButtonEvent(parent -> min(scaleLabel));
        Button scaleP = new Button(mc, this).setText("+").setLayoutHint(new PositionalLayout.PositionalHint(70, 62, 10, 15))
                .addButtonEvent(parent -> plus(scaleLabel));
        scaleSlider = new Slider(mc, this).setHorizontal().setScrollable(scaleLabel)
                .setLayoutHint(new PositionalLayout.PositionalHint(5, 78, 76, 15));
        toplevel.addChild(scaleI).addChild(scaleLabel).addChild(scaleSlider).addChild(scaleM).addChild(scaleP);

        Label offsetI = new Label(mc, this).setText("Offs:");
        offsetI.setLayoutHint(new PositionalLayout.PositionalHint(23, 94, 18, 15));
        offsetLabel = new ScrollableLabel(mc, this).setRealMinimum(0).setRealMaximum(100)
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT)
                .setLayoutHint(new PositionalLayout.PositionalHint(32, 94, 34, 15));
        offsetLabel.setRealValue(tileEntity.getOffsetInt());
        Button offsetM = new Button(mc, this).setText("-").setLayoutHint(new PositionalLayout.PositionalHint(5, 94, 10, 15))
                .addButtonEvent(parent -> min(offsetLabel));
        Button offsetP = new Button(mc, this).setText("+").setLayoutHint(new PositionalLayout.PositionalHint(70, 94, 10, 15))
                .addButtonEvent(parent -> plus(offsetLabel));
        offsetSlider = new Slider(mc, this).setHorizontal().setScrollable(offsetLabel)
                .setLayoutHint(new PositionalLayout.PositionalHint(5, 110, 76, 15));
        toplevel.addChild(offsetI).addChild(offsetLabel).addChild(offsetSlider).addChild(offsetM).addChild(offsetP);

        autoRotate = new ToggleButton(mc, this).setCheckMarker(true).setText("Auto").setLayoutHint(new PositionalLayout.PositionalHint(5, 126, 48, 16));
        autoRotate.setPressed(tileEntity.isAutoRotate());
        toplevel.addChild(autoRotate);

        angleLabel.addValueEvent((parent, newValue) -> update());
        scaleLabel.addValueEvent((parent, newValue) -> update());
        offsetLabel.addValueEvent((parent, newValue) -> update());
        autoRotate.addButtonEvent(parent -> update());


        showAxis = new ToggleButton(mc, this).setCheckMarker(true).setText("A").setLayoutHint(new PositionalLayout.PositionalHint(5, 200, 24, 16));
        showAxis.setPressed(true);
        toplevel.addChild(showAxis);
        showOuter = new ToggleButton(mc, this).setCheckMarker(true).setText("B").setLayoutHint(new PositionalLayout.PositionalHint(31, 200, 24, 16));
        showOuter.setPressed(true);
        toplevel.addChild(showOuter);
        showMat = new ToggleButton(mc, this).setCheckMarker(true).setText("M").setLayoutHint(new PositionalLayout.PositionalHint(57, 200, 24, 16));
        showMat.setPressed(true);
        toplevel.addChild(showMat);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void plus(ScrollableLabel l) {
        l.setRealValue(l.getRealValue()+1);
    }

    private void min(ScrollableLabel l) {
        l.setRealValue(l.getRealValue()-1);
    }

    private void update() {
        sendServerCommand(RFToolsMessages.INSTANCE, ProjectorTileEntity.CMD_SETTINGS,
                new Argument("scale", scaleLabel.getRealValue()),
                new Argument("offset", offsetLabel.getRealValue()),
                new Argument("angle", angleLabel.getRealValue()),
                new Argument("auto", autoRotate.isPressed())
                );
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent((parent, newChoice) -> changeRedstoneMode()).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(5, 180, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRSMode(RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()]);
        sendServerCommand(RFToolsMessages.INSTANCE, ProjectorTileEntity.CMD_MODE,
                new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()].getDescription()));
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

        drawWindow();

        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        ItemStack stack = tileEntity.getRenderStack();
        if (ItemStackTools.isValid(stack)) {
            getShapeRenderer().setShapeID(getShapeID());
            getShapeRenderer().renderShape(this, stack, guiLeft, guiTop, showAxis.isPressed(), showOuter.isPressed(), showMat.isPressed());
        }
    }

}

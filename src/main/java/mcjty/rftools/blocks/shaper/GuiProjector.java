package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.IShapeParentGui;
import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.Rectangle;
import java.io.IOException;

import static mcjty.rftools.blocks.shaper.ProjectorTileEntity.*;

public class GuiProjector extends GenericGuiContainer<ProjectorTileEntity> implements IShapeParentGui {

    public static final int SIDEWIDTH = 80;
    public static final int PROJECTOR_WIDTH = 256;
    public static final int PROJECTOR_HEIGHT = 238;

    private static final ResourceLocation sideBackground = new ResourceLocation(RFTools.MODID, "textures/gui/sidegui_projector.png");
    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/projector.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private EnergyBar energyBar;

    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showScan;

    private ScrollableLabel angleLabel;
    private ScrollableLabel offsetLabel;
    private ScrollableLabel scaleLabel;
    private Slider angleSlider;
    private Slider offsetSlider;
    private Slider scaleSlider;
    private ToggleButton autoRotate;
    private ToggleButton scanline;
    private ToggleButton sound;
    private ToggleButton grayScale;

    private ChoiceLabel[] rsLabelOn = new ChoiceLabel[4];
    private ChoiceLabel[] rsLabelOff = new ChoiceLabel[4];
    private TextField[] valOn = new TextField[4];
    private TextField[] valOff = new TextField[4];

    private Window sideWindow;

    private ShapeRenderer shapeRenderer = null;

    public GuiProjector(ProjectorTileEntity te, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, RFTools.GUI_MANUAL_SHAPE, "projector");

        xSize = PROJECTOR_WIDTH;
        ySize = PROJECTOR_HEIGHT;
    }

    private ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer(tileEntity.getShapeID());
        }
        return shapeRenderer;
    }

    @Override
    public void initGui() {
        super.initGui();

        getShapeRenderer().initView(getPreviewLeft(), guiTop+100);

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        long maxEnergyStored = tileEntity.getCapacity();
        energyBar = new EnergyBar(mc, this).setHorizontal().setMaxValue(maxEnergyStored).setLayoutHint(6, 184, 75, 10).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        toplevel.addChild(energyBar);

        Label angleI = new Label(mc, this).setText("Angle");
        angleI.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        angleI.setLayoutHint(16, 30, 32, 15);
        angleLabel = new ScrollableLabel(mc, this).setName("angle").setRealMinimum(0).setRealMaximum(360)
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT)
                .setLayoutHint(44, 30, 24, 15);
        angleLabel.setRealValue(tileEntity.getAngleInt());
        Button angleM = new Button(mc, this).setChannel("anglemin").setText("-").setLayoutHint(5, 30, 10, 15);
        Button angleP = new Button(mc, this).setChannel("angleplus").setText("+").setLayoutHint(70, 30, 10, 15);
        angleSlider = new Slider(mc, this).setHorizontal().setScrollableName("angle")
                .setLayoutHint(5, 46, 76, 15);
        toplevel.addChildren(angleI, angleLabel, angleSlider, angleM, angleP);

        Label scaleI = new Label(mc, this).setText("Scale");
        scaleI.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        scaleI.setLayoutHint(16, 62, 32, 15);
        scaleLabel = new ScrollableLabel(mc, this).setName("scale").setRealMinimum(0).setRealMaximum(100)
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT)
                .setLayoutHint(44, 62, 24, 15);
        scaleLabel.setRealValue(tileEntity.getScaleInt());
        Button scaleM = new Button(mc, this).setChannel("scalemin").setText("-").setLayoutHint(5, 62, 10, 15);
        Button scaleP = new Button(mc, this).setChannel("scaleplus").setText("+").setLayoutHint(70, 62, 10, 15);
        scaleSlider = new Slider(mc, this).setHorizontal().setScrollableName("scale")
                .setLayoutHint(5, 78, 76, 15);
        toplevel.addChildren(scaleI, scaleLabel, scaleSlider, scaleM, scaleP);

        Label offsetI = new Label(mc, this).setText("Offset");
        offsetI.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        offsetI.setLayoutHint(16, 94, 32, 15);
        offsetLabel = new ScrollableLabel(mc, this).setName("offset").setRealMinimum(0).setRealMaximum(100)
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT)
                .setLayoutHint(44, 94, 24, 15);
        offsetLabel.setRealValue(tileEntity.getOffsetInt());
        Button offsetM = new Button(mc, this).setChannel("offsetmin").setText("-").setLayoutHint(5, 94, 10, 15);
        Button offsetP = new Button(mc, this).setChannel("offsetplus").setText("+").setLayoutHint(70, 94, 10, 15);
        offsetSlider = new Slider(mc, this).setHorizontal().setScrollableName("offset")
                .setLayoutHint(5, 110, 76, 15);
        toplevel.addChildren(offsetI, offsetLabel, offsetSlider, offsetM, offsetP);

        autoRotate = new ToggleButton(mc, this).setCheckMarker(true)
                .setText("Auto")
                .setTooltips("Automatic client-side rotation")
                .setLayoutHint(2, 128, 39, 16);
        autoRotate.setPressed(tileEntity.isAutoRotate());
        toplevel.addChild(autoRotate);
        scanline = new ToggleButton(mc, this).setCheckMarker(true)
                .setText("SL")
                .setTooltips("Enable/disable visual scanlines when", "the scan is refreshed")
                .setLayoutHint(42, 128, 39, 16);
        scanline.setPressed(tileEntity.isScanline());
        toplevel.addChild(scanline);
        sound = new ToggleButton(mc, this).setCheckMarker(true)
                .setText("Snd")
                .setTooltips("Enable/disable sound during", "visual scan")
                .setLayoutHint(2, 146, 39, 16);
        sound.setPressed(tileEntity.isSound());
        toplevel.addChild(sound);
        grayScale = new ToggleButton(mc, this).setCheckMarker(true)
                .setText("Gray")
                .setTooltips("Enable/disable grayscale", "mode")
                .setLayoutHint(42, 146, 39, 16);
        grayScale.setPressed(tileEntity.isGrayscale());
        toplevel.addChild(grayScale);

        angleLabel.addValueEvent((parent, newValue) -> update());
        scaleLabel.addValueEvent((parent, newValue) -> update());
        offsetLabel.addValueEvent((parent, newValue) -> update());
        autoRotate.addButtonEvent(parent -> update());
        scanline.addButtonEvent(parent -> update());
        sound.addButtonEvent(parent -> update());
        grayScale.addButtonEvent(parent -> update());

        showAxis = ShapeGuiTools.createAxisButton(this, toplevel, 5, 200);
        showOuter = ShapeGuiTools.createBoxButton(this, toplevel, 31, 200);
        showScan = ShapeGuiTools.createScanButton(this, toplevel, 57, 200);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        initSidePanel();

        window = new Window(this, toplevel);

        tileEntity.requestRfFromServer(RFTools.MODID);

        window.event("anglemin", (source, params) -> min(angleLabel));
        window.event("angleplus", (source, params) -> plus(angleLabel));
        window.event("scalemin", (source, params) -> min(scaleLabel));
        window.event("scaleplus", (source, params) -> plus(scaleLabel));
        window.event("offsetmin", (source, params) -> min(offsetLabel));
        window.event("offsetplus", (source, params) -> plus(offsetLabel));
    }

    private void initSidePanel() {
        Panel sidePanel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(sideBackground);
        initRsPanel(sidePanel, 0, "S");
        initRsPanel(sidePanel, 1, "N");
        initRsPanel(sidePanel, 2, "E");
        initRsPanel(sidePanel, 3, "W");
        sidePanel.setBounds(new Rectangle(guiLeft-SIDEWIDTH, guiTop, SIDEWIDTH, ySize));
        sideWindow = new Window(this, sidePanel);
    }

    private void initRsPanel(Panel sidePanel, int o, String label) {
        int dy = o * 53;
        sidePanel.addChild(new Label(mc, this)
                .setText(label)
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                .setLayoutHint(new PositionalLayout.PositionalHint(8, dy+8, 12, 13)));

        ImageLabel redstoneOn = new ImageLabel(mc, this).setImage(iconGuiElements, 16, 96);
        redstoneOn.setLayoutHint(new PositionalLayout.PositionalHint(20, dy+8, 16, 16));
        ImageLabel redstoneOff = new ImageLabel(mc, this).setImage(iconGuiElements, 0, 96);
        redstoneOff.setLayoutHint(new PositionalLayout.PositionalHint(54, dy+8, 16, 16));
        sidePanel.addChild(redstoneOn).addChild(redstoneOff);

        rsLabelOn[o] = new ChoiceLabel(mc, this);
        rsLabelOn[o].addChoices(ProjectorOpcode.getChoices());
        rsLabelOn[o].setLayoutHint(new PositionalLayout.PositionalHint(8, dy+26, 32, 14));
        for (ProjectorOpcode operation : ProjectorOpcode.values()) {
            rsLabelOn[o].setChoiceTooltip(operation.getCode(), operation.getDescription());
        }
        sidePanel.addChild(rsLabelOn[o]);

        rsLabelOff[o] = new ChoiceLabel(mc, this);
        rsLabelOff[o].addChoices(ProjectorOpcode.getChoices());
        rsLabelOff[o].setLayoutHint(new PositionalLayout.PositionalHint(42, dy+26, 32, 14));
        for (ProjectorOpcode operation : ProjectorOpcode.values()) {
            rsLabelOff[o].setChoiceTooltip(operation.getCode(), operation.getDescription());
        }
        sidePanel.addChild(rsLabelOff[o]);

        valOn[o] = new TextField(mc, this);
        valOn[o].setLayoutHint(new PositionalLayout.PositionalHint(8, dy+41, 32, 14));
        sidePanel.addChild(valOn[o]);
        valOff[o] = new TextField(mc, this);
        valOff[o].setLayoutHint(new PositionalLayout.PositionalHint(42, dy+41, 32, 14));
        sidePanel.addChild(valOff[o]);
        rsLabelOn[o].addChoiceEvent((parent, newChoice) -> updateRs());
        rsLabelOff[o].addChoiceEvent((parent, newChoice) -> updateRs());
        valOn[o].addTextEvent((parent, newText) -> updateRs());
        valOff[o].addTextEvent((parent, newText) -> updateRs());

        ProjectorOperation op = tileEntity.getOperations()[o];
        rsLabelOn[o].setChoice(op.getOpcodeOn().getCode());
        rsLabelOff[o].setChoice(op.getOpcodeOff().getCode());
        valOn[o].setText(op.getValueOn() == null ? "" : op.getValueOn().toString());
        valOff[o].setText(op.getValueOff() == null ? "" : op.getValueOff().toString());
    }

    private void updateRs() {
        TypedMap.Builder builder = TypedMap.builder();
        for (int i = 0 ; i < 4 ; i++) {
            builder.put(ProjectorTileEntity.PARAM_OPON.get(i), rsLabelOn[i].getCurrentChoice());
            builder.put(ProjectorTileEntity.PARAM_OPOFF.get(i), rsLabelOff[i].getCurrentChoice());
            String text = valOn[i].getText();
            if (!text.trim().isEmpty()) {
                try {
                    builder.put(ProjectorTileEntity.PARAM_VALON.get(i), Double.parseDouble(text));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            text = valOff[i].getText();
            if (!text.trim().isEmpty()) {
                try {
                    builder.put(ProjectorTileEntity.PARAM_VALOFF.get(i), Double.parseDouble(text));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        sendServerCommand(RFToolsMessages.INSTANCE, ProjectorTileEntity.CMD_RSSETTINGS, builder.build());
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
    }



    private void plus(ScrollableLabel l) {
        l.setRealValue(l.getRealValue()+1);
    }

    private void min(ScrollableLabel l) {
        l.setRealValue(l.getRealValue()-1);
    }

    private void update() {
        sendServerCommand(RFToolsMessages.INSTANCE, ProjectorTileEntity.CMD_SETTINGS,
                TypedMap.builder()
                        .put(PARAM_SCALE, scaleLabel.getRealValue())
                        .put(PARAM_OFFSET, offsetLabel.getRealValue())
                        .put(PARAM_ANGLE, angleLabel.getRealValue())
                        .put(PARAM_AUTO, autoRotate.isPressed())
                        .put(PARAM_SCAN, scanline.isPressed())
                        .put(PARAM_SOUND, sound.isPressed())
                        .put(PARAM_GRAY, grayScale.isPressed())
                        .build());
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

        for (int i = 0 ; i < 4 ; i++) {
            ProjectorOperation op = tileEntity.getOperations()[i];
            valOn[i].setEnabled(op.getOpcodeOn().isNeedsValue());
            valOff[i].setEnabled(op.getOpcodeOff().isNeedsValue());
        }
        sound.setEnabled(scanline.isPressed());

        drawWindow();

        long currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        ItemStack stack = tileEntity.getRenderStack();
        if (!stack.isEmpty()) {
            getShapeRenderer().setShapeID(tileEntity.getShapeID());
            getShapeRenderer().renderShape(this, stack, guiLeft, guiTop, showAxis.isPressed(), showOuter.isPressed(), showScan.isPressed(), false);
        }
    }

}

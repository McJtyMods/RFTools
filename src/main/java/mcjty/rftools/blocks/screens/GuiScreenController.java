package mcjty.rftools.blocks.screens;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;

public class GuiScreenController extends GenericGuiContainer<ScreenControllerTileEntity> {
    public static final int CONTROLLER_WIDTH = 180;
    public static final int CONTROLLER_HEIGHT = 152;

    private EnergyBar energyBar;
    private Label infoLabel;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/screencontroller.png");

    public GuiScreenController(ScreenControllerTileEntity screenControllerTileEntity, ScreenControllerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, screenControllerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "screens");
        GenericEnergyStorageTileEntity.setCurrentRF(screenControllerTileEntity.getEnergyStored());

        xSize = CONTROLLER_WIDTH;
        ySize = CONTROLLER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored();
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(10, 7, 8, 54).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        Button scanButton = new Button(mc, this)
                .setName("scan")
                .setText("Scan").setTooltips("Find all nearby screens", "and connect to them").setLayoutHint(30, 7, 50, 14);
        Button detachButton = new Button(mc, this)
                .setName("detach")
                .setText("Detach").setTooltips("Detach from all screens").setLayoutHint(90, 7, 50, 14);
        infoLabel = new Label(mc, this);
        infoLabel.setLayoutHint(30, 25, 140, 14);

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout())
                .addChildren(energyBar, scanButton, detachButton, infoLabel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFTools.MODID);

        window.action(RFToolsMessages.INSTANCE, "scan", tileEntity, ScreenControllerTileEntity.ACTION_SCAN);
        window.action(RFToolsMessages.INSTANCE, "detach", tileEntity, ScreenControllerTileEntity.ACTION_DETACH);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        infoLabel.setText(tileEntity.getConnectedScreens().size() + " connected screens");

        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}

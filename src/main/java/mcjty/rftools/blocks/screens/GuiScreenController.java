package mcjty.rftools.blocks.screens;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiScreenController extends GenericGuiContainer<ScreenControllerTileEntity> {
    public static final int CONTROLLER_WIDTH = 180;
    public static final int CONTROLLER_HEIGHT = 152;

    private EnergyBar energyBar;
    private Label infoLabel;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/screencontroller.png");

    public GuiScreenController(ScreenControllerTileEntity screenControllerTileEntity, ScreenControllerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, screenControllerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "screens");
        screenControllerTileEntity.setCurrentRF(screenControllerTileEntity.getEnergyStored(EnumFacing.DOWN));

        xSize = CONTROLLER_WIDTH;
        ySize = CONTROLLER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(EnumFacing.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        Button scanButton = new Button(mc, this).setText("Scan").setTooltips("Find all nearby screens", "and connect to them").setLayoutHint(new PositionalLayout.PositionalHint(30, 7, 50, 14));
        scanButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                sendServerCommand(RFToolsMessages.INSTANCE, ScreenControllerTileEntity.CMD_SCAN);
            }
        });
        Button detachButton = new Button(mc, this).setText("Detach").setTooltips("Detach from all screens").setLayoutHint(new PositionalLayout.PositionalHint(90, 7, 50, 14));
        detachButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                sendServerCommand(RFToolsMessages.INSTANCE, ScreenControllerTileEntity.CMD_DETACH);
            }
        });
        infoLabel = new Label(mc, this);
        infoLabel.setLayoutHint(new PositionalLayout.PositionalHint(30, 25, 140, 14));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).addChild(scanButton).addChild(detachButton).
                addChild(infoLabel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFTools.MODID);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        energyBar.setValue(tileEntity.getCurrentRF());
        infoLabel.setText(tileEntity.getConnectedScreens().size() + " connected screens");

        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}

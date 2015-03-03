package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiScreenController extends GenericGuiContainer<ScreenControllerTileEntity> {
    public static final int CONTROLLER_WIDTH = 180;
    public static final int CONTROLLER_HEIGHT = 152;

    private EnergyBar energyBar;
    private Label infoLabel;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/screencontroller.png");

    public GuiScreenController(ScreenControllerTileEntity screenControllerTileEntity, ScreenControllerContainer container) {
        super(screenControllerTileEntity, container);
        screenControllerTileEntity.setCurrentRF(screenControllerTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = CONTROLLER_WIDTH;
        ySize = CONTROLLER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        Button scanButton = new Button(mc, this).setText("Scan").setTooltips("Find all nearby screens", "and connect to them").setLayoutHint(new PositionalLayout.PositionalHint(30, 7, 50, 14));
        scanButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                sendServerCommand(ScreenControllerTileEntity.CMD_SCAN);
            }
        });
        Button detachButton = new Button(mc, this).setText("Detach").setTooltips("Detach from all screens").setLayoutHint(new PositionalLayout.PositionalHint(90, 7, 50, 14));
        detachButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                sendServerCommand(ScreenControllerTileEntity.CMD_DETACH);
            }
        });
        infoLabel = new Label(mc, this);
        infoLabel.setLayoutHint(new PositionalLayout.PositionalHint(30, 25, 140, 14));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).addChild(scanButton).addChild(detachButton).
                addChild(infoLabel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();

        energyBar.setValue(tileEntity.getCurrentRF());
        infoLabel.setText(tileEntity.getConnectedScreens().size() + " connected screens");

        tileEntity.requestRfFromServer();
    }
}

package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.gui.NumericScrollable;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Panel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.*;


public class GuiStorageMonitor extends GuiContainer {
    public static final int STORAGE_MONITOR_WIDTH = 256;
    public static final int STORAGE_MONITOR_HEIGHT = 224;

    private Window window;
    private final StorageMonitorTileEntity storageMonitorTileEntity;

    public GuiStorageMonitor(StorageMonitorTileEntity storageMonitorTileEntity, StorageMonitorContainer storageMonitorContainer) {
        super(storageMonitorContainer);
        this.storageMonitorTileEntity = storageMonitorTileEntity;
        storageMonitorTileEntity.setOldRF(-1);
        storageMonitorTileEntity.setCurrentRF(storageMonitorTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = STORAGE_MONITOR_WIDTH;
        ySize = STORAGE_MONITOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        WidgetList storageList = new WidgetList(mc, this);
        WidgetList itemList = new WidgetList(mc, this);
        Panel topPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(storageList).addChild(itemList);

        Button scanButton = new Button(mc, this).setText("Scan").setTooltips("Start a scan of", "all storage units", "in radius");
        Slider radiusSlider = new Slider(mc, this).setHorizontal().setTooltips("Radius of scan").setScrollable(new NumericScrollable(20, 1, 1));
        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(20).addChild(scanButton).addChild(radiusSlider);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(topPanel).addChild(bottomPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        int currentRF = storageMonitorTileEntity.getCurrentRF();
//        energyBar.setValue(currentRF);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }

}

package com.mcjty.rftools.items.netmonitor;

import com.mcjty.gui.*;
import com.mcjty.gui.Label;
import com.mcjty.gui.Panel;
import com.mcjty.gui.Window;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.util.*;

public class GuiNetworkMonitor extends GuiScreen {
    private NetworkMonitorItem monitorItem;

    // A copy of the connected blocks we're currently showing
    Map<Coordinate, BlockInfo> connectedBlocks;
    // The labels in our list containing the RF information.
    Map<Coordinate, EnergyBar> labelMap;

    /** The X size of the window in pixels. */
    protected int xSize = 356;
    /** The Y size of the window in pixels. */
    protected int ySize = 180;

    public static final int TEXT_COLOR = 0x19979f;
    public static final int SEL_TEXT_COLOR = 0x092020;

    private Window window;
    private WidgetList list;
    private int listDirty;


    public GuiNetworkMonitor(NetworkMonitorItem monitorItem) {
        this.monitorItem = monitorItem;
        listDirty = 0;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        list = new WidgetList(mc, this).setRowheight(16);
        listDirty = 0;
        Slider listSlider = new Slider(mc, this).setDesiredWidth(15).setVertical().setScrollable(list);
        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new HorizontalLayout()).addChild(list).addChild(listSlider);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void refreshList() {
        for (Map.Entry<Coordinate,BlockInfo> me : connectedBlocks.entrySet()) {
            BlockInfo blockInfo = me.getValue();

            int energy = blockInfo.getEnergyStored();
            int maxEnergy = blockInfo.getMaxEnergyStored();

            EnergyBar energyLabel = labelMap.get(me.getKey());
            energyLabel.setValue(energy).setMaxValue(maxEnergy);
        }
    }

    private void populateList() {
        Map<Coordinate, BlockInfo> newConnectedBlocks = monitorItem.getConnectedBlocks();
        if (newConnectedBlocks.equals(connectedBlocks)) {
            refreshList();
            return;
        }

        connectedBlocks = new HashMap<Coordinate, BlockInfo>(newConnectedBlocks);
        labelMap = new HashMap<Coordinate, EnergyBar>();
        list.removeChildren();

        for (Map.Entry<Coordinate,BlockInfo> me : connectedBlocks.entrySet()) {
            BlockInfo blockInfo = me.getValue();
            Block block = blockInfo.getBlock();
            Coordinate coordinate = me.getKey();

            int energy = blockInfo.getEnergyStored();
            int maxEnergy = blockInfo.getMaxEnergyStored();

            int color = getTextColor(blockInfo);

            String displayName = blockInfo.getReadableName(mc.theWorld);

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());

            panel.addChild(new BlockRender(mc, this).setRenderItem(block));
            panel.addChild(new Label(mc, this).setText(displayName).setColor(color).setDesiredWidth(100));
            panel.addChild(new Label(mc, this).setText(coordinate.toString()).setColor(color).setDesiredWidth(75));
            EnergyBar energyLabel = new EnergyBar(mc, this).setValue(energy).setMaxValue(maxEnergy).setColor(TEXT_COLOR).setHorizontal();
            panel.addChild(energyLabel);
            list.addChild(panel);

            labelMap.put(coordinate, energyLabel);
        }
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

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        listDirty--;
        if (listDirty <= 0) {
            populateList();
            listDirty = 5;
        }

        window.draw();
    }

    private int getTextColor(BlockInfo blockInfo) {
        int color;
        if (blockInfo.isFirst()) {
            color = SEL_TEXT_COLOR;
        } else {
            color = TEXT_COLOR;
        }
        return color;
    }

}

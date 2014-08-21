package com.mcjty.rftools.gui;

import com.mcjty.gui.*;
import com.mcjty.gui.Label;
import com.mcjty.gui.Panel;
import com.mcjty.gui.Window;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.RFMonitorBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuiRFMonitor extends GuiScreen {
    private RFMonitorBlock monitorBlock;

    private Window window;
    private WidgetList list;
    private int listDirty;

    public static final int TEXT_COLOR = 0x19979f;

    // A copy of the adjacent blocks we're currently showing
    Map<Coordinate, BlockInfo> adjacentBlocks;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack.png");

    /** The X size of the window in pixels. */
    protected int xSize = 256;
    /** The Y size of the window in pixels. */
    protected int ySize = 180;


    public GuiRFMonitor(RFMonitorBlock monitorBlock) {
        this.monitorBlock = monitorBlock;
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
        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new HorizontalLayout()).addChild(list).addChild(listSlider);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));
        window = new Window(this, toplevel);
    }

    private void refreshList() {
//        for (Map.Entry<Coordinate,BlockInfo> me : adjacentBlocks.entrySet()) {
//            BlockInfo blockInfo = me.getValue();
//
//            int energy = blockInfo.getEnergyStored();
//            int maxEnergy = blockInfo.getMaxEnergyStored();
//
//            EnergyBar energyLabel = labelMap.get(me.getKey());
//            energyLabel.setValue(energy).setMaxValue(maxEnergy);
//        }
    }

    private void populateList() {
        Map<Coordinate, BlockInfo> newAdjacentBlocks = monitorBlock.getAdjacentBlocks();
        if (newAdjacentBlocks.equals(adjacentBlocks)) {
            refreshList();
            return;
        }

        adjacentBlocks = new HashMap<Coordinate, BlockInfo>(newAdjacentBlocks);
        list.removeChildren();

        for (Map.Entry<Coordinate,BlockInfo> me : adjacentBlocks.entrySet()) {
            BlockInfo blockInfo = me.getValue();
            Block block = blockInfo.getBlock();
            Coordinate coordinate = me.getKey();

            int color = TEXT_COLOR;

            String displayName = blockInfo.getReadableName(mc.theWorld, coordinate);

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new BlockRender(mc, this).setRenderItem(block));
            panel.addChild(new Label(mc, this).setText(displayName).setColor(color).setDesiredWidth(120));
            panel.addChild(new Label(mc, this).setText(coordinate.toString()).setColor(color));
            list.addChild(panel);
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
}

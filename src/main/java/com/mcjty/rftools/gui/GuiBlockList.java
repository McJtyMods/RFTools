package com.mcjty.rftools.gui;

import com.mcjty.gui.*;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.BlockInfo;
import com.mcjty.rftools.items.Coordinate;
import com.mcjty.rftools.items.NetworkMonitorItem;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GuiBlockList extends GuiScreen {
    private NetworkMonitorItem monitorItem;

    private WidgetList list;
    private int listDirty;

    // A copy of the connected blocks we're currently showing
    Map<Coordinate, BlockInfo> connectedBlocks;
    // The labels in our list containing the RF information.
    Map<Coordinate, EnergyBar> labelMap;

    /** The X size of the window in pixels. */
    protected int xSize = 356;
    /** The Y size of the window in pixels. */
    protected int ySize = 180;

//    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack.png");
    private static final ResourceLocation iconLocationLeft = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack_left.png");
    private static final ResourceLocation iconLocationRight = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack_right.png");
    public static final int TEXT_COLOR = 0x19979f;
    public static final int SEL_TEXT_COLOR = 0x092020;

    Widget toplevel;

    public GuiBlockList(NetworkMonitorItem monitorItem) {
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
        toplevel = new Panel(mc, this).setBackground(iconLocationLeft, iconLocationRight).setLayout(new HorizontalLayout()).addChild(list).addChild(listSlider);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));
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

            String displayName = getReadableName(block);
            int color = getTextColor(blockInfo);

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
        if (toplevel.getBounds().contains(x, y)) {
            toplevel.mouseClick(x, y, button);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        if (k == -1) {
            mouseMovedOrUp(x, y, k);
        }
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        // -1 == mouse move
        if (button != -1) {
            toplevel.mouseRelease(x, y, button);
        } else {
            toplevel.mouseMove(x, y);
        }
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        listDirty--;
        if (listDirty <= 0) {
            populateList();
            listDirty = 5;
        }

        toplevel.draw(0, 0);
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

    private String getReadableName(Block block) {
        ItemStack s = new ItemStack(block, 1, 0);
        String displayName = s.getDisplayName();
        if (displayName.startsWith("tile.")) {
            displayName = displayName.substring(5);
        }
        if (displayName.endsWith(".name")) {
            displayName = displayName.substring(0, displayName.length()-5);
        }
        return displayName;
    }
}

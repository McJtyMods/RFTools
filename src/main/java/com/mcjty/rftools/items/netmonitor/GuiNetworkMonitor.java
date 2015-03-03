package com.mcjty.rftools.items.netmonitor;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.DefaultSelectionEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiNetworkMonitor extends GuiScreen {
    // A copy of the connected blocks we're currently showing
    private Map<Coordinate, BlockInfo> connectedBlocks;
    // The labels in our list containing the RF information.
    private Map<Coordinate, EnergyBar> labelMap;

    // Previous rf for a given coordinate.
    private Map<Coordinate, Integer> previousRf = null;
    private long previousRfMillis = 0;

    // A map mapping index in our widget list to coordinates.
    private Map<Integer, Coordinate> indexToCoordinate;

    // The result of the server.
    private static Map<Coordinate, BlockInfo> serverConnectedBlocks = null;
    private static int selectedX;
    private static int selectedY;
    private static int selectedZ;

    /** The X size of the window in pixels. */
    protected int xSize = 356;
    /** The Y size of the window in pixels. */
    protected int ySize = 206;

    public static final int TEXT_COLOR = 0x19979f;
    public static final int SEL_TEXT_COLOR = 0x092020;

    private Window window;
    private ToggleButton showRfPerTick;
    private WidgetList list;
    private TextField filterTextField;
    private int listDirty;

    private String filter = null;

    public static void setSelected(int x, int y, int z) {
        selectedX = x;
        selectedY = y;
        selectedZ = z;
    }

    public GuiNetworkMonitor() {
        listDirty = 0;
        previousRfMillis = 0;
    }

    public static void setServerConnectedBlocks(Map<Coordinate, BlockInfo> serverConnectedBlocks) {
        GuiNetworkMonitor.serverConnectedBlocks = new HashMap<Coordinate, BlockInfo>(serverConnectedBlocks);
    }

    private void requestConnectedBlocksFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetConnectedBlocks(selectedX, selectedY, selectedZ));
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

        list = new WidgetList(mc, this).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void doubleClick(Widget parent, int index) {
                hilightBlock(index);
            }
        });
        listDirty = 0;
        Slider listSlider = new Slider(mc, this).setDesiredWidth(15).setVertical().setScrollable(list);
        Panel listPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(list).addChild(listSlider);

        showRfPerTick = new ToggleButton(mc, this).setCheckMarker(true).setText("RF/tick").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                previousRfMillis = 0;
            }
        }).setDesiredHeight(13);
        filterTextField = new TextField(mc, this).setDesiredHeight(13).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                filter = filterTextField.getText();
                if (filter.trim().isEmpty()) {
                    filter = null;
                }
                connectedBlocks = null;
            }
        });
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(showRfPerTick).addChild(new Label(mc, this).setText("Filter:")).addChild(filterTextField).setDesiredHeight(16);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(listPanel).addChild(buttonPanel).setDesiredHeight(13);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);

        serverConnectedBlocks = null;
    }

    private void hilightBlock(int index) {
        if (index == -1) {
            return;
        }
        Coordinate c = indexToCoordinate.get(index);
        RFTools.instance.clientInfo.hilightBlock(c, System.currentTimeMillis()+1000* NetworkMonitorConfiguration.hilightTime);
        RFTools.message(mc.thePlayer, "The block is now highlighted");
        Minecraft.getMinecraft().thePlayer.closeScreen();
    }

    private void refreshList(boolean recalcPerTick) {
        long millis = System.currentTimeMillis();
        boolean rftick = showRfPerTick.isPressed();

        for (Map.Entry<Coordinate,BlockInfo> me : connectedBlocks.entrySet()) {
            BlockInfo blockInfo = me.getValue();

            int energy = blockInfo.getEnergyStored();
            int maxEnergy = blockInfo.getMaxEnergyStored();

            EnergyBar energyLabel = labelMap.get(me.getKey());
            // First test if this label isn't filtered out.
            if (energyLabel != null) {
                setEnergyLabel(millis, rftick, recalcPerTick, me, energy, maxEnergy, energyLabel);
            }
        }
    }

    private void setEnergyLabel(long millis, boolean rftick, boolean recalcPerTick, Map.Entry<Coordinate, BlockInfo> me, int energy, int maxEnergy, EnergyBar energyLabel) {
        energyLabel.setValue(energy).setMaxValue(maxEnergy).setShowRfPerTick(rftick);
        if (rftick && recalcPerTick) {
            long dt = millis - previousRfMillis;
            int rft = 0;
            if (dt > 0 && previousRf != null && previousRf.containsKey(me.getKey())) {
                rft = energy - previousRf.get(me.getKey());
                rft = rft * 20 / (int)dt;
            }
            energyLabel.setRfPerTick(rft);
        }
    }

    private void populateList() {
        requestConnectedBlocksFromServer();

        if (serverConnectedBlocks == null) {
            return;
        }

        boolean rftick = showRfPerTick.isPressed();
        long millis = System.currentTimeMillis();
        boolean recalcPerTick = previousRfMillis == 0 || (millis - previousRfMillis) > 1000;

        if (serverConnectedBlocks.equals(connectedBlocks)) {
            refreshList(recalcPerTick);
        } else {
            connectedBlocks = new HashMap<Coordinate, BlockInfo>(serverConnectedBlocks);
            Map<Coordinate, EnergyBar> oldLabelMap = labelMap;
            labelMap = new HashMap<Coordinate, EnergyBar>();
            indexToCoordinate = new HashMap<Integer, Coordinate>();
            list.removeChildren();

            int index = 0;
            for (Map.Entry<Coordinate, BlockInfo> me : connectedBlocks.entrySet()) {
                BlockInfo blockInfo = me.getValue();
                Coordinate coordinate = me.getKey();
                Block block = mc.theWorld.getBlock(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                if (block == null || block.isAir(mc.theWorld, coordinate.getX(), coordinate.getY(), coordinate.getZ())) {
                    continue;
                }

                int energy = blockInfo.getEnergyStored();
                int maxEnergy = blockInfo.getMaxEnergyStored();

                int color = getTextColor(blockInfo);

                int meta = mc.theWorld.getBlockMetadata(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                String displayName = BlockInfo.getReadableName(block, coordinate, meta, mc.theWorld);

                if (filter != null) {
                    if (!displayName.toLowerCase().contains(filter)) {
                        continue;
                    }
                }

                Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());

                panel.addChild(new BlockRender(mc, this).setRenderItem(block));
                panel.addChild(new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setText(displayName).setColor(color).setDesiredWidth(100));
                panel.addChild(new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setText(coordinate.toString()).setColor(color).setDesiredWidth(75));
                EnergyBar energyLabel = oldLabelMap == null ? null : oldLabelMap.get(coordinate);
                if (energyLabel == null) {
                    energyLabel = new EnergyBar(mc, this).setColor(TEXT_COLOR).setHorizontal();
                }
                setEnergyLabel(millis, rftick, recalcPerTick, me, energy, maxEnergy, energyLabel);

                panel.addChild(energyLabel);
                list.addChild(panel);

                labelMap.put(coordinate, energyLabel);
                indexToCoordinate.put(index, coordinate);
                index++;
            }
        }

        if (rftick && recalcPerTick) {
            previousRfMillis = millis;
            previousRf = new HashMap<Coordinate, Integer>(connectedBlocks.size());
            for (Map.Entry<Coordinate, BlockInfo> me : connectedBlocks.entrySet()) {
                previousRf.put(me.getKey(), me.getValue().getEnergyStored());
            }
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
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        listDirty--;
        if (listDirty <= 0) {
            populateList();
            listDirty = 10;
        }

        window.draw();
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }

    private int getTextColor(BlockInfo blockInfo) {
        int color;
        Coordinate c = blockInfo.getCoordinate();
        if (c.getX() == selectedX && c.getY() == selectedY && c.getZ() == selectedZ) {
            color = SEL_TEXT_COLOR;
        } else {
            color = TEXT_COLOR;
        }
        return color;
    }

}

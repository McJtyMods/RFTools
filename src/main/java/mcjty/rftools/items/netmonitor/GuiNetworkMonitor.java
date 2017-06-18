package mcjty.rftools.items.netmonitor;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GuiNetworkMonitor extends GuiItemScreen {
    private static final int MONITOR_XSIZE = 356;
    private static final int MONITOR_YSIZE = 206;

    // A copy of the connected blocks we're currently showing
    private Map<BlockPos, BlockInfo> connectedBlocks;
    // The labels in our list containing the RF information.
    private Map<BlockPos, EnergyBar> labelMap;

    // Previous rf for a given coordinate.
    private Map<BlockPos, Integer> previousRf = null;
    private long previousRfMillis = 0;

    // A map mapping index in our widget list to coordinates.
    private Map<Integer, BlockPos> indexToCoordinate;

    // The result of the server.
    private static Map<BlockPos, BlockInfo> serverConnectedBlocks = null;
    private static BlockPos selected;

    public static final int TEXT_COLOR = 0x000000;
    public static final int SEL_TEXT_COLOR = 0xffffff;

    private ToggleButton showRfPerTick;
    private WidgetList list;
    private TextField filterTextField;
    private int listDirty;

    private String filter = null;

    public static void setSelected(BlockPos selected) {
        GuiNetworkMonitor.selected = selected;
    }

    public GuiNetworkMonitor() {
        super(RFTools.instance, RFToolsMessages.INSTANCE, MONITOR_XSIZE, MONITOR_YSIZE, RFTools.GUI_MANUAL_MAIN, "netmon");
        listDirty = 0;
        previousRfMillis = 0;
    }

    public static void setServerConnectedBlocks(Map<BlockPos, BlockInfo> serverConnectedBlocks) {
        GuiNetworkMonitor.serverConnectedBlocks = new HashMap<BlockPos, BlockInfo>(serverConnectedBlocks);
    }

    private void requestConnectedBlocksFromServer() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetConnectedBlocks(selected));
    }

    @Override
    public void initGui() {
        super.initGui();

        list = new WidgetList(mc, this).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void doubleClick(Widget parent, int index) {
                hilightBlock(index);
            }
        });
        listDirty = 0;
        Slider listSlider = new Slider(mc, this).setDesiredWidth(11).setVertical().setScrollable(list);
        Panel listPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChild(list).addChild(listSlider);

        showRfPerTick = new ToggleButton(mc, this).setCheckMarker(true).setText("RF/tick").setDesiredWidth(80).addButtonEvent(parent -> previousRfMillis = 0).setDesiredHeight(14);
        filterTextField = new TextField(mc, this).setDesiredHeight(14).addTextEvent((parent, newText) -> {
            filter = filterTextField.getText();
            if (filter.trim().isEmpty()) {
                filter = null;
            }
            connectedBlocks = null;
        });
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(showRfPerTick).addChild(new Label(mc, this).setText("Filter:")).addChild(filterTextField).setDesiredHeight(17);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setVerticalMargin(3)).addChild(listPanel).addChild(buttonPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        serverConnectedBlocks = null;
    }

    private void hilightBlock(int index) {
        if (index == -1) {
            return;
        }
        BlockPos c = indexToCoordinate.get(index);
        RFTools.instance.clientInfo.hilightBlock(c, System.currentTimeMillis()+1000* NetworkMonitorConfiguration.hilightTime);
        Logging.message(mc.player, "The block is now highlighted");
        Minecraft.getMinecraft().player.closeScreen();
    }

    private void refreshList(boolean recalcPerTick) {
        long millis = System.currentTimeMillis();
        boolean rftick = showRfPerTick.isPressed();

        for (Map.Entry<BlockPos,BlockInfo> me : connectedBlocks.entrySet()) {
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

    private void setEnergyLabel(long millis, boolean rftick, boolean recalcPerTick, Map.Entry<BlockPos, BlockInfo> me, int energy, int maxEnergy, EnergyBar energyLabel) {
        energyLabel.setValue(energy).setMaxValue(maxEnergy).setShowRfPerTick(rftick);
        if (rftick && recalcPerTick) {
            long dt = millis - previousRfMillis;
            int rft = 0;
            if (dt > 0 && previousRf != null && previousRf.containsKey(me.getKey())) {
                rft = energy - previousRf.get(me.getKey());
                rft = rft * 50 / (int)dt;
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
            connectedBlocks = new HashMap<BlockPos, BlockInfo>(serverConnectedBlocks);
            Map<BlockPos, EnergyBar> oldLabelMap = labelMap;
            labelMap = new HashMap<BlockPos, EnergyBar>();
            indexToCoordinate = new HashMap<Integer, BlockPos>();
            list.removeChildren();

            int index = 0;
            for (Map.Entry<BlockPos, BlockInfo> me : connectedBlocks.entrySet()) {
                BlockInfo blockInfo = me.getValue();
                BlockPos coordinate = me.getKey();
                if (mc.world.isAirBlock(coordinate)) {
                    continue;
                }

                int energy = blockInfo.getEnergyStored();
                int maxEnergy = blockInfo.getMaxEnergyStored();

                int color = getTextColor(blockInfo);

                IBlockState state = mc.world.getBlockState(coordinate);
                String displayName = BlockInfo.getReadableName(state);

                if (filter != null) {
                    if (!displayName.toLowerCase().contains(filter)) {
                        continue;
                    }
                }

                Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());

                panel.addChild(new BlockRender(mc, this).setRenderItem(state.getBlock()));
                panel.addChild(new Label(mc, this).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setText(displayName).setColor(color).setDesiredWidth(100));
                panel.addChild(new Label(mc, this).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setText(BlockPosTools.toString(coordinate)).setColor(color).setDesiredWidth(75));
                EnergyBar energyLabel = oldLabelMap == null ? null : oldLabelMap.get(coordinate);
                if (energyLabel == null) {
                    energyLabel = new EnergyBar(mc, this).setHorizontal();
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
            previousRf = new HashMap<BlockPos, Integer>(connectedBlocks.size());
            for (Map.Entry<BlockPos, BlockInfo> me : connectedBlocks.entrySet()) {
                previousRf.put(me.getKey(), me.getValue().getEnergyStored());
            }
        }
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        listDirty--;
        if (listDirty <= 0) {
            populateList();
            listDirty = 10;
        }

        drawWindow();
    }

    private int getTextColor(BlockInfo blockInfo) {
        int color;
        BlockPos c = blockInfo.getCoordinate();
        if (c.equals(selected)) {
            color = SEL_TEXT_COLOR;
        } else {
            color = TEXT_COLOR;
        }
        return color;
    }

}

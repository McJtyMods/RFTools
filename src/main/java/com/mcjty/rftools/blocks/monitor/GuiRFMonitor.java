package com.mcjty.rftools.blocks.monitor;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.events.DefaultSelectionEvent;
import com.mcjty.gui.events.ValueEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.inventory.Container;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiRFMonitor extends GenericGuiContainer<RFMonitorBlockTileEntity> {
    private WidgetList list;
    private ChoiceLabel alarmModeChoiceLabel;
    private ScrollableLabel alarmLabel;
    private int listDirty;

    public static final int TEXT_COLOR = 0x19979f;

    // A copy of the adjacent blocks we're currently showing
    private List<Coordinate> adjacentBlocks = null;

    // From server.
    public static List<Coordinate> fromServer_clientAdjacentBlocks = null;


    public GuiRFMonitor(RFMonitorBlockTileEntity monitorBlockTileEntity, Container container) {
        super(monitorBlockTileEntity, container);
    }

    @Override
    public void initGui() {
        super.initGui();

        int xSize = 256;
        int ySize = 180;

        int k = (this.width - xSize) / 2;
        int l = (this.height - ySize) / 2;

        list = new WidgetList(mc, this).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                setSelectedBlock(index);
            }
        });
        listDirty = 0;
        Slider listSlider = new Slider(mc, this).setDesiredWidth(15).setVertical().setScrollable(list);
        Panel listPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(list).addChild(listSlider);

        alarmModeChoiceLabel = new ChoiceLabel(mc, this).addChoices(
                RFMonitorMode.MODE_OFF.getDescription(), RFMonitorMode.MODE_LESS.getDescription(), RFMonitorMode.MODE_MORE.getDescription()).
                setDesiredWidth(60).setDesiredHeight(15).
                setTooltips("Control when a redstone", "signal should be sent").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeAlarmMode(RFMonitorMode.getModeFromDescription(newChoice));
                    }
                });
        alarmModeChoiceLabel.setChoice(tileEntity.getAlarmMode().getDescription());

        alarmLabel = new ScrollableLabel(mc, this).setSuffix("%").setDesiredWidth(30).setRealMinimum(0).setRealMaximum(100).
                setRealValue(tileEntity.getAlarmLevel()).
                addValueEvent(new ValueEvent() {
                    @Override
                    public void valueChanged(Widget parent, int newValue) {
                        changeAlarmValue(newValue);
                    }
                });
        Slider alarmSlider = new Slider(mc, this).
                setDesiredHeight(15).
                setHorizontal().
                setTooltips("Alarm level").
                setScrollable(alarmLabel);
        Panel alarmPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(alarmModeChoiceLabel).addChild(alarmSlider).addChild(alarmLabel).setDesiredHeight(20);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(listPanel).addChild(alarmPanel);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));
        window = new Window(this, toplevel);

        fromServer_clientAdjacentBlocks = new ArrayList<Coordinate>();
        PacketHandler.INSTANCE.sendToServer(new PacketGetAdjacentBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
    }

    private void changeAlarmMode(RFMonitorMode mode) {
        int alarmLevel = alarmLabel.getRealValue();
        tileEntity.setAlarm(mode, alarmLevel);
        PacketHandler.INSTANCE.sendToServer(new PacketRFMonitor(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, mode, alarmLevel));
    }

    private void changeAlarmValue(int newValue) {
        RFMonitorMode mode = RFMonitorMode.getModeFromDescription(alarmModeChoiceLabel.getCurrentChoice());
        tileEntity.setAlarm(mode, newValue);
        PacketHandler.INSTANCE.sendToServer(new PacketRFMonitor(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, mode, newValue));
    }

    private void refreshList() {
    }

    private void setSelectedBlock(int index) {
        if (index != -1) {
            Coordinate c = adjacentBlocks.get(index);
            tileEntity.setMonitor(c);
            PacketHandler.INSTANCE.sendToServer(new PacketRFMonitor(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, c));
        } else {
            tileEntity.setInvalid();
            PacketHandler.INSTANCE.sendToServer(new PacketRFMonitor(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, Coordinate.INVALID));
        }
    }

    private void populateList() {
        List<Coordinate> newAdjacentBlocks = fromServer_clientAdjacentBlocks;
        if (newAdjacentBlocks == null) {
            return;
        }
        if (newAdjacentBlocks.equals(adjacentBlocks)) {
            refreshList();
            return;
        }

        adjacentBlocks = new ArrayList<Coordinate>(newAdjacentBlocks);
        list.removeChildren();

        int index = 0, sel = -1;
        for (Coordinate coordinate : adjacentBlocks) {
            Block block = mc.theWorld.getBlock(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            int meta = mc.theWorld.getBlockMetadata(coordinate.getX(), coordinate.getY(), coordinate.getZ());

            int color = TEXT_COLOR;

            String displayName = BlockInfo.getReadableName(block, coordinate, meta, mc.theWorld);

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new BlockRender(mc, this).setRenderItem(block));
            panel.addChild(new Label(mc, this).setText(displayName).setColor(color).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(90));
            panel.addChild(new Label(mc, this).setDynamic(true).setText(coordinate.toString()).setColor(color));
            list.addChild(panel);

            if (coordinate.getX() == tileEntity.getMonitorX() &&
                    coordinate.getY() == tileEntity.getMonitorY() &&
                    coordinate.getZ() == tileEntity.getMonitorZ()) {
                sel = index;
            }
            index++;
        }

        list.setSelected(sel);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        listDirty--;
        if (listDirty <= 0) {
            populateList();
            listDirty = 5;
        }

        window.draw();
    }
}

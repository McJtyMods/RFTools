package com.mcjty.rftools.blocks.monitor;

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
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiRFMonitor extends GuiScreen {
    private RFMonitorBlock monitorBlock;
    private RFMonitorBlockTileEntity monitorBlockTileEntity;

    private Window window;
    private WidgetList list;
    private ChoiceLabel alarmModeChoiceLabel;
    private ScrollableLabel alarmLabel;
    private int listDirty;

    public static final int TEXT_COLOR = 0x19979f;

    // A copy of the adjacent blocks we're currently showing
    private List<Coordinate> adjacentBlocks;

    /** The X size of the window in pixels. */
    private int xSize = 256;
    /** The Y size of the window in pixels. */
    private int ySize = 180;


    public GuiRFMonitor(RFMonitorBlock monitorBlock, RFMonitorBlockTileEntity monitorBlockTileEntity) {
        this.monitorBlock = monitorBlock;
        this.monitorBlockTileEntity = monitorBlockTileEntity;
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

        list = new WidgetList(mc, this).setRowheight(16).addSelectionEvent(new DefaultSelectionEvent() {
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
        alarmModeChoiceLabel.setChoice(monitorBlockTileEntity.getAlarmMode().getDescription());

        alarmLabel = new ScrollableLabel(mc, this).setSuffix("%").setDesiredWidth(30).setRealMinimum(0).setRealMaximum(100).
                setRealValue(monitorBlockTileEntity.getAlarmLevel()).
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

        monitorBlockTileEntity.storeAdjacentBlocksForClient(new ArrayList<Coordinate>());
        requestAdjacentBlocksFromServer();
    }

    private void changeAlarmMode(RFMonitorMode mode) {
        int alarmLevel = alarmLabel.getRealValue();
        monitorBlockTileEntity.setAlarm(mode, alarmLevel);
        sendChangeToServer(mode, alarmLevel);
    }

    private void changeAlarmValue(int newValue) {
        RFMonitorMode mode = RFMonitorMode.getModeFromDescription(alarmModeChoiceLabel.getCurrentChoice());
        monitorBlockTileEntity.setAlarm(mode, newValue);
        sendChangeToServer(mode, newValue);
    }

    private void refreshList() {
    }

    private void setSelectedBlock(int index) {
        if (index != -1) {
            Coordinate c = adjacentBlocks.get(index);
            monitorBlockTileEntity.setMonitor(c);
            sendChangeToServer(c);
        } else {
            monitorBlockTileEntity.setInvalid();
            sendChangeToServer(Coordinate.INVALID);
        }
    }

    private void sendChangeToServer(Coordinate c) {
        PacketHandler.INSTANCE.sendToServer(new PacketRFMonitor(monitorBlockTileEntity.xCoord, monitorBlockTileEntity.yCoord, monitorBlockTileEntity.zCoord, c));
    }

    private void sendChangeToServer(RFMonitorMode mode, int level) {
        PacketHandler.INSTANCE.sendToServer(new PacketRFMonitor(monitorBlockTileEntity.xCoord, monitorBlockTileEntity.yCoord, monitorBlockTileEntity.zCoord, mode, level));
    }

    private void requestAdjacentBlocksFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetAdjacentBlocks(monitorBlockTileEntity.xCoord, monitorBlockTileEntity.yCoord, monitorBlockTileEntity.zCoord));
    }

    private void populateList() {
        List<Coordinate> newAdjacentBlocks = monitorBlockTileEntity.getClientAdjacentBlocks();
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
            panel.addChild(new Label(mc, this).setText(displayName).setColor(color).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(120));
            panel.addChild(new Label(mc, this).setDynamic(true).setText(coordinate.toString()).setColor(color));
            list.addChild(panel);

            if (coordinate.getX() == monitorBlockTileEntity.getMonitorX() &&
                    coordinate.getY() == monitorBlockTileEntity.getMonitorY() &&
                    coordinate.getZ() == monitorBlockTileEntity.getMonitorZ()) {
                sel = index;
            }
            index++;
        }

        list.setSelected(sel);
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
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x, y, mc.fontRenderer);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }
}

package mcjty.rftools.blocks.monitor;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.events.DefaultSelectionEvent;
import mcjty.gui.events.ValueEvent;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import mcjty.network.PacketHandler;
import mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.inventory.Container;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiLiquidMonitor extends GenericGuiContainer<LiquidMonitorBlockTileEntity> {
    private WidgetList list;
    private ChoiceLabel alarmModeChoiceLabel;
    private ScrollableLabel alarmLabel;
    private int listDirty;

    public static final int TEXT_COLOR = 0x000000;
    public static final int TEXT_COLOR_SELECTED = 0xFFFFFF;

    // A copy of the adjacent blocks we're currently showing
    private List<Coordinate> adjacentBlocks = null;

    // From server.
    public static List<Coordinate> fromServer_clientAdjacentBlocks = null;


    public GuiLiquidMonitor(LiquidMonitorBlockTileEntity liquidMonitorBlockTileEntity, Container container) {
        super(RFTools.instance, liquidMonitorBlockTileEntity, container, RFTools.GUI_MANUAL_MAIN, "liqmonitor");
        xSize = 256;
        ySize = 180;
    }

    @Override
    public void initGui() {
        super.initGui();

        list = createStyledList().addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                setSelectedBlock(index);
            }
        });
        listDirty = 0;
        Slider listSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(list);
        Panel listPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(3).setSpacing(1)).addChild(list).addChild(listSlider);

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
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);

        fromServer_clientAdjacentBlocks = new ArrayList<Coordinate>();
        PacketHandler.INSTANCE.sendToServer(new PacketGetAdjacentTankBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
    }

    private void changeAlarmMode(RFMonitorMode mode) {
        int alarmLevel = alarmLabel.getRealValue();
        tileEntity.setAlarm(mode, alarmLevel);
        PacketHandler.INSTANCE.sendToServer(new PacketContentsMonitor(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, mode, alarmLevel));
    }

    private void changeAlarmValue(int newValue) {
        RFMonitorMode mode = RFMonitorMode.getModeFromDescription(alarmModeChoiceLabel.getCurrentChoice());
        tileEntity.setAlarm(mode, newValue);
        PacketHandler.INSTANCE.sendToServer(new PacketContentsMonitor(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, mode, newValue));
    }

    private void refreshList() {
    }

    private void setSelectedBlock(int index) {
        if (index != -1) {
            Coordinate c = adjacentBlocks.get(index);
            tileEntity.setMonitor(c);
            PacketHandler.INSTANCE.sendToServer(new PacketContentsMonitor(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, c));
        } else {
            tileEntity.setInvalid();
            PacketHandler.INSTANCE.sendToServer(new PacketContentsMonitor(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, Coordinate.INVALID));
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

            if (coordinate.getX() == tileEntity.getMonitorX() &&
                    coordinate.getY() == tileEntity.getMonitorY() &&
                    coordinate.getZ() == tileEntity.getMonitorZ()) {
                sel = index;
                color = TEXT_COLOR_SELECTED;
            }

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new BlockRender(mc, this).setRenderItem(block));
            panel.addChild(new Label(mc, this).setText(displayName).setColor(color).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(90));
            panel.addChild(new Label(mc, this).setDynamic(true).setText(coordinate.toString()).setColor(color));
            list.addChild(panel);

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

        drawWindow();
    }
}

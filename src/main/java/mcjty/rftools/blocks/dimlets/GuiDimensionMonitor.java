package mcjty.rftools.blocks.dimlets;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ValueEvent;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.ScrollableLabel;
import mcjty.gui.widgets.Slider;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import mcjty.network.Argument;
import net.minecraft.inventory.Container;

import java.awt.*;

public class GuiDimensionMonitor extends GenericGuiContainer<DimensionMonitorTileEntity> {
    public static final int MONITOR_WIDTH = 160;
    public static final int MONITOR_HEIGHT = 30;

    private ScrollableLabel alarmLevel;

    public GuiDimensionMonitor(DimensionMonitorTileEntity dimensionMonitorTileEntity, Container container) {
        super(RFTools.instance, dimensionMonitorTileEntity, container, RFTools.GUI_MANUAL_DIMENSION, "monitor");
        xSize = MONITOR_WIDTH;
        ySize = MONITOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        alarmLevel = new ScrollableLabel(mc, this).setSuffix("%").setDesiredWidth(30).setRealMinimum(0).setRealMaximum(100).
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
                setScrollable(alarmLevel);

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(alarmLevel).addChild(alarmSlider);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, MONITOR_WIDTH, MONITOR_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void changeAlarmValue(int newValue) {
        tileEntity.setAlarmLevel(newValue);
        sendServerCommand(DimensionMonitorTileEntity.CMD_SETALARM, new Argument("level", newValue));
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}

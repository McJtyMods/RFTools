package mcjty.rftools.blocks.logic.timer;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;

import java.awt.*;

public class GuiTimer extends GenericGuiContainer<TimerTileEntity> {
    public static final int TIMER_WIDTH = 168;
    public static final int TIMER_HEIGHT = 48;

    public GuiTimer(TimerTileEntity timerTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, timerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "timer");
        xSize = TIMER_WIDTH;
        ySize = TIMER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        Label label = new Label(mc, this).setText("Delay:");
        TextField speedField = new TextField(mc, this)
                .setName("speed")
                .setChannel("speed")
                .setTooltips("Set the delay in ticks", "(20 ticks is one second)");
        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChildren(label, speedField);
        toplevel.addChild(bottomPanel);
        ToggleButton redstonePauses = new ToggleButton(mc, this)
                .setName("pauses")
                .setChannel("pauses")
                .setText("Pause while redstone active").setDesiredHeight(16).setCheckMarker(true).setPressed(tileEntity.getRedstonePauses());
        toplevel.addChild(redstonePauses);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, TIMER_WIDTH, TIMER_HEIGHT));
        window = new Window(this, toplevel);

        initializeFields();

        window.addChannelEvent("speed", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, TimerTileEntity.CMD_SETDELAY, params));
        window.addChannelEvent("pauses", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, TimerTileEntity.CMD_SETPAUSES, params));
    }

    private void initializeFields() {
        int delay = tileEntity.getDelay();
        if (delay <= 0) {
            delay = 1;
        }
        TextField speedField = window.findChild("speed");
        speedField.setText(String.valueOf(delay));

        ToggleButton redstonePauses = window.findChild("pauses");
        redstonePauses.setPressed(tileEntity.getRedstonePauses());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}

package mcjty.rftools.blocks.logic.timer;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;

import java.awt.*;

public class GuiTimer extends GenericGuiContainer<TimerTileEntity> {
    public static final int TIMER_WIDTH = 168;
    public static final int TIMER_HEIGHT = 48;

    private TextField speedField;
    private ToggleButton redstonePauses;

    public GuiTimer(TimerTileEntity timerTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, timerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "timer");
        xSize = TIMER_WIDTH;
        ySize = TIMER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        Label label = new Label(mc, this).setText("Delay:");
        speedField = new TextField(mc, this).setTooltips("Set the delay in ticks", "(20 ticks is one second)").addTextEvent((parent, newText) -> setDelay());
        int delay = tileEntity.getDelay();
        if (delay <= 0) {
            delay = 1;
        }
        speedField.setText(String.valueOf(delay));
        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(label).addChild(speedField);
        toplevel.addChild(bottomPanel);
        redstonePauses = new ToggleButton(mc, this).setText("Pause while redstone active").setDesiredHeight(16).setCheckMarker(true).setPressed(tileEntity.getRedstonePauses())
                .addButtonEvent(parent -> setRedstonePauses());
        toplevel.addChild(redstonePauses);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, TIMER_WIDTH, TIMER_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void setDelay() {
        String d = speedField.getText();
        int delay;
        try {
            delay = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            delay = 1;
        }
        tileEntity.setDelay(delay);
        sendServerCommand(RFToolsMessages.INSTANCE, TimerTileEntity.CMD_SETDELAY, new Argument("delay", delay));
    }

    private void setRedstonePauses() {
        boolean pauses = redstonePauses.isPressed();
        tileEntity.setRedstonePauses(pauses);
        sendServerCommand(RFToolsMessages.INSTANCE, TimerTileEntity.CMD_SETPAUSES, new Argument("pauses", pauses));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}

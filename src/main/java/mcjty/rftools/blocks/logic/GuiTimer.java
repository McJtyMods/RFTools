package mcjty.rftools.blocks.logic;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.TextEvent;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import mcjty.network.Argument;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.inventory.Container;

import java.awt.*;

public class GuiTimer extends GenericGuiContainer<TimerTileEntity> {
    public static final int TIMER_WIDTH = 160;
    public static final int TIMER_HEIGHT = 30;

    private TextField speedField;

    public GuiTimer(TimerTileEntity timerTileEntity, Container container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, timerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "timer");
        xSize = TIMER_WIDTH;
        ySize = TIMER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        Label label = new Label(mc, this).setText("Delay:");
        speedField = new TextField(mc, this).setTooltips("Set the delay in ticks", "(20 ticks is one second)").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                setDelay();
            }
        });
        int delay = tileEntity.getDelay();
        if (delay <= 0) {
            delay = 1;
        }
        speedField.setText(String.valueOf(delay));

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(label).addChild(speedField);
        toplevel.addChild(bottomPanel);

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

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}

package com.mcjty.rftools.blocks.logic;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.network.Argument;
import net.minecraft.inventory.Container;

import java.awt.*;

public class GuiCounter extends GenericGuiContainer<CounterTileEntity> {
    public static final int COUNTER_WIDTH = 200;
    public static final int COUNTER_HEIGHT = 30;

    private TextField counterField;
    private TextField currentField;

    public GuiCounter(CounterTileEntity counterTileEntity, Container container) {
        super(counterTileEntity, container);
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - COUNTER_WIDTH) / 2;
        int l = (this.height - COUNTER_HEIGHT) / 2;

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        counterField = new TextField(mc, this).setTooltips("Set the counter in pulses").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                setCounter();
            }
        });
        int delay = tileEntity.getCounter();
        if (delay <= 0) {
            delay = 1;
        }
        counterField.setText(String.valueOf(delay));

        currentField = new TextField(mc, this).setTooltips("Set the current value", "(fires when it reaches counter)").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                setCurrent();
            }
        });
        int current = tileEntity.getCurrent();
        if (current < 0) {
            current = 0;
        }
        currentField.setText(String.valueOf(current));

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, this).setText("Counter:")).addChild(counterField).
                addChild(new Label(mc, this).setText("Current:")).addChild(currentField);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(k, l, COUNTER_WIDTH, COUNTER_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void setCounter() {
        String d = counterField.getText();
        int counter;
        try {
            counter = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            counter = 1;
        }
        tileEntity.setCounter(counter);
        sendServerCommand(CounterTileEntity.CMD_SETCOUNTER, new Argument("counter", counter));
    }

    private void setCurrent() {
        String d = currentField.getText();
        int current;
        try {
            current = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            current = 0;
        }
        tileEntity.setCounter(current);
        sendServerCommand(CounterTileEntity.CMD_SETCURRENT, new Argument("current", current));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

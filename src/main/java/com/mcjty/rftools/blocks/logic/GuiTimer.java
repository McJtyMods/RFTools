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

public class GuiTimer extends GenericGuiContainer<TimerTileEntity> {
    public static final int TIMER_WIDTH = 160;
    public static final int TIMER_HEIGHT = 30;

    private TextField speedField;

    public GuiTimer(TimerTileEntity timerTileEntity, Container container) {
        super(timerTileEntity, container);
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - TIMER_WIDTH) / 2;
        int l = (this.height - TIMER_HEIGHT) / 2;

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        Label label = new Label(mc, this).setText("Delay:");
        speedField = new TextField(mc, this).addTextEvent(new TextEvent() {
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

        toplevel.setBounds(new Rectangle(k, l, TIMER_WIDTH, TIMER_HEIGHT));
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
        sendServerCommand(TimerTileEntity.CMD_SETDELAY, new Argument("delay", delay));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

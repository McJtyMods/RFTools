package com.mcjty.rftools.blocks.logic;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketServerCommand;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;

public class GuiTimer extends GuiScreen {
    public static final int TIMER_WIDTH = 160;
    public static final int TIMER_HEIGHT = 30;

    private Window window;
    private TextField speedField;

    private final TimerTileEntity timerTileEntity;

    public GuiTimer(TimerTileEntity timerTileEntity) {
        this.timerTileEntity = timerTileEntity;
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
        int delay = timerTileEntity.getDelay();
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
        timerTileEntity.setDelay(delay);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(timerTileEntity.xCoord, timerTileEntity.yCoord, timerTileEntity.zCoord,
                TimerTileEntity.CMD_SETDELAY,
                new Argument("delay", delay)));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
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

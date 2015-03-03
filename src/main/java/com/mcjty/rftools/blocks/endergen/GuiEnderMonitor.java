package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.ChoiceLabel;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.network.Argument;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiEnderMonitor extends GenericGuiContainer<EnderMonitorTileEntity> {
    public static final int MONITOR_WIDTH = 140;
    public static final int MONITOR_HEIGHT = 30;

    private ChoiceLabel mode;

    public GuiEnderMonitor(EnderMonitorTileEntity enderMonitorTileEntity, Container container) {
        super(enderMonitorTileEntity, container);
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - MONITOR_WIDTH) / 2;
        int l = (this.height - MONITOR_HEIGHT) / 2;

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        Label label = new Label(mc, this).setText("Mode:");
        initGuiMode();

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(label).addChild(mode);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(k, l, MONITOR_WIDTH, MONITOR_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void initGuiMode() {
        mode = new ChoiceLabel(mc, this).setDesiredHeight(13).setDesiredWidth(80);
        for (EnderMonitorMode m : EnderMonitorMode.values()) {
            mode.addChoices(m.getDescription());
        }

        mode.setChoiceTooltip(EnderMonitorMode.MODE_LOSTPEARL.getDescription(), "Send a redstone pulse when a", "pearl is lost");
        mode.setChoiceTooltip(EnderMonitorMode.MODE_PEARLFIRED.getDescription(), "Send a redstone pulse when a", "pearl is fired");
        mode.setChoiceTooltip(EnderMonitorMode.MODE_PEARLARRIVED.getDescription(), "Send a redstone pulse when a", "pearl arrives");
        mode.setChoice(tileEntity.getMode().getDescription());
        mode.addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeMode();
            }
        });
    }

    private void changeMode() {
        EnderMonitorMode newMode = EnderMonitorMode.getMode(mode.getCurrentChoice());
        tileEntity.setMode(newMode);
        sendServerCommand(EnderMonitorTileEntity.CMD_MODE, new Argument("mode", newMode.getDescription()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x, y, mc.fontRenderer);
        }
    }
}

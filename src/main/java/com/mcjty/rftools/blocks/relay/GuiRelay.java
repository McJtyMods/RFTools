package com.mcjty.rftools.blocks.relay;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.ChoiceLabel;
import com.mcjty.gui.widgets.ImageLabel;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiRelay extends GenericGuiContainer<RelayTileEntity> {
    public static final int RELAY_WIDTH = 140;
    public static final int RELAY_HEIGHT = 50;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ChoiceLabel choicesOn;
    private ChoiceLabel choicesOff;

    public GuiRelay(RelayTileEntity relayTileEntity, Container container) {
        super(relayTileEntity, container);
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - RELAY_WIDTH) / 2;
        int l = (this.height - RELAY_HEIGHT) / 2;

        ImageLabel redstoneOff = new ImageLabel(mc, this).setImage(iconGuiElements, 16, 0);
        redstoneOff.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal off");
        choicesOff = new ChoiceLabel(mc, this).addChoices("0", "100", "500", "1000", "5000", "10000", "20000").setDesiredWidth(100).
                setTooltips("Amount of RF to output", "when redstone is off").setDesiredHeight(16).addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeRfOutput();
            }
        });
        choicesOff.setChoice(Integer.toString(tileEntity.getRfOff()));
        Panel panelOff = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(redstoneOff).addChild(choicesOff);

        ImageLabel redstoneOn = new ImageLabel(mc, this).setImage(iconGuiElements, 32, 0);
        redstoneOn.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal on");
        choicesOn = new ChoiceLabel(mc, this).addChoices("0", "100", "500", "1000", "5000", "10000", "20000").setDesiredWidth(100).
            setTooltips("Amount of RF to output", "when redstone is on").setDesiredHeight(16).addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeRfOutput();
            }
        });
        choicesOn.setChoice(Integer.toString(tileEntity.getRfOn()));
        Panel panelOn = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(redstoneOn).addChild(choicesOn);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(panelOff).addChild(panelOn);
        toplevel.setBounds(new Rectangle(k, l, RELAY_WIDTH, RELAY_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void changeRfOutput() {
        sendServerCommand(RelayTileEntity.CMD_SETTINGS,
                new Argument("on", new Integer(choicesOn.getCurrentChoice())),
                new Argument("off", new Integer(choicesOff.getCurrentChoice())));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

package com.mcjty.rftools.blocks.relay;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiRelay extends GenericGuiContainer<RelayTileEntity> {
    public static final int RELAY_WIDTH = 240;
    public static final int RELAY_HEIGHT = 50;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private Button offButtonSub1000;
    private Button offButtonSub100;
    private TextField offEnergy;
    private Button offButtonAdd100;
    private Button offButtonAdd1000;

    private Button onButtonSub1000;
    private Button onButtonSub100;
    private TextField onEnergy;
    private Button onButtonAdd100;
    private Button onButtonAdd1000;


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
        offEnergy = new TextField(mc, this).setTooltips("Amount of RF to output", "when redstone is off").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                adjustEnergy(offEnergy, 0);
            }
        });
        offButtonSub1000 = createEnergyOffsetButton(offEnergy, "-1000", -1000);
        offButtonSub100 = createEnergyOffsetButton(offEnergy, "-100", -100);
        offButtonAdd100 = createEnergyOffsetButton(offEnergy, "+100", 100);
        offButtonAdd1000 = createEnergyOffsetButton(offEnergy, "+1000", 1000);
        offEnergy.setText(Integer.toString(tileEntity.getRfOff()));

        Panel panelOff = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(redstoneOff).
                addChild(offButtonSub1000).
                addChild(offButtonSub100).
                addChild(offEnergy).
                addChild(offButtonAdd100).
                addChild(offButtonAdd1000);

        ImageLabel redstoneOn = new ImageLabel(mc, this).setImage(iconGuiElements, 32, 0);
        redstoneOn.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal on");
        onEnergy = new TextField(mc, this).setTooltips("Amount of RF to output", "when redstone is on").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                adjustEnergy(onEnergy, 0);
            }
        });
        onButtonSub1000 = createEnergyOffsetButton(onEnergy, "-1000", -1000);
        onButtonSub100 = createEnergyOffsetButton(onEnergy, "-100", -100);
        onButtonAdd100 = createEnergyOffsetButton(onEnergy, "+100", 100);
        onButtonAdd1000 = createEnergyOffsetButton(onEnergy, "+1000", 1000);
        onEnergy.setText(Integer.toString(tileEntity.getRfOn()));

        Panel panelOn = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(redstoneOn).
                addChild(onButtonSub1000).
                addChild(onButtonSub100).
                addChild(onEnergy).
                addChild(onButtonAdd100).
                addChild(onButtonAdd1000);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(panelOff).addChild(panelOn);
        toplevel.setBounds(new Rectangle(k, l, RELAY_WIDTH, RELAY_HEIGHT));
        window = new Window(this, toplevel);
    }

    private Button createEnergyOffsetButton(final TextField energyField, String label, final int amount) {
        return new Button(mc, this).setText(label).setDesiredHeight(16).addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                adjustEnergy(energyField, amount);
            }
        });
    }

    private void adjustEnergy(TextField energyField, int amount) {
        int energy;
        try {
            energy = Integer.parseInt(energyField.getText());
        } catch (NumberFormatException e) {
            energy = 0;
        }
        energy += amount;
        if (energy < 0) {
            energy = 0;
        } else if (energy > 50000) {
            energy = 50000;
        }
        energyField.setText(Integer.toString(energy));
        changeRfOutput();
    }

    private void changeRfOutput() {
        sendServerCommand(RelayTileEntity.CMD_SETTINGS,
                new Argument("on", Integer.parseInt(onEnergy.getText())),
                new Argument("off", Integer.parseInt(offEnergy.getText())));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

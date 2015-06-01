package mcjty.rftools.blocks.relay;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.events.TextEvent;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.Argument;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiRelay extends GenericGuiContainer<RelayTileEntity> {
//    public static final int RELAY_WIDTH = 240;
//    public static final int RELAY_HEIGHT = 50;
    public static final int RELAY_WIDTH = 255;
    public static final int RELAY_HEIGHT = 130;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private TextField offEnergy;
    private TextField onEnergy;

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
        Button offButtonSub1000 = createEnergyOffsetButton(offEnergy, "-1000", -1000);
        Button offButtonSub100 = createEnergyOffsetButton(offEnergy, "-100", -100);
        Button offButtonAdd100 = createEnergyOffsetButton(offEnergy, "+100", 100);
        Button offButtonAdd1000 = createEnergyOffsetButton(offEnergy, "+1000", 1000);
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
        Button onButtonSub1000 = createEnergyOffsetButton(onEnergy, "-1000", -1000);
        Button onButtonSub100 = createEnergyOffsetButton(onEnergy, "-100", -100);
        Button onButtonAdd100 = createEnergyOffsetButton(onEnergy, "+100", 100);
        Button onButtonAdd1000 = createEnergyOffsetButton(onEnergy, "+1000", 1000);
        onEnergy.setText(Integer.toString(tileEntity.getRfOn()));

        Panel panelOn = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(redstoneOn).
                addChild(onButtonSub1000).
                addChild(onButtonSub100).
                addChild(onEnergy).
                addChild(onButtonAdd100).
                addChild(onButtonAdd1000);

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        toplevel.addChild(createRedstonePanel());
        toplevel.addChild(createSidePanel("D"));
        toplevel.addChild(createSidePanel("U"));
        toplevel.addChild(createSidePanel("N"));
        toplevel.addChild(createSidePanel("S"));
        toplevel.addChild(createSidePanel("W"));
        toplevel.addChild(createSidePanel("E"));

//        .addChild(panelOff).addChild(panelOn);

        toplevel.setBounds(new Rectangle(k, l, RELAY_WIDTH, RELAY_HEIGHT));
        window = new Window(this, toplevel);
    }

    private Panel createRedstonePanel() {
        ImageLabel redstoneOff = new ImageLabel(mc, this).setImage(iconGuiElements, 16, 0);
        redstoneOff.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal off").setLayoutHint(new PositionalLayout.PositionalHint(70, 0, 16, 16));
        ImageLabel redstoneOn = new ImageLabel(mc, this).setImage(iconGuiElements, 32, 0);
        redstoneOn.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal on").setLayoutHint(new PositionalLayout.PositionalHint(190, 0, 16, 16));
        Panel panel = new Panel(mc, this).setLayout(new PositionalLayout()).
                addChild(redstoneOff).
                addChild(redstoneOn);
        return panel;
    }

    private Panel createSidePanel(String labelText) {
        Label label = new Label(mc, this).setText(labelText);
        label.setDesiredWidth(14).setDesiredHeight(14);
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(1)).
                addChild(label).
                addChild(createSubPanel("Off").setDesiredWidth(115)).
                addChild(createSubPanel("On").setDesiredWidth(115));
        return panel;
    }

    private Panel createSubPanel(String redstoneState) {
        ImageChoiceLabel inputOutput = new ImageChoiceLabel(mc, this).
                setDesiredWidth(14).setDesiredHeight(14).
                addChoice("Output", "Side set to output mode", iconGuiElements, 80, 16).
                addChoice("Input", "Side set to input mode", iconGuiElements, 96, 16);
        TextField energyField = new TextField(mc, this).setTooltips("Amount of RF to output", "when redstone is " + redstoneState).
                setDesiredWidth(42).setDesiredHeight(14).
                addTextEvent(new TextEvent() {
                    @Override
                    public void textChanged(Widget parent, String newText) {
                        adjustEnergy((TextField) parent, 0);
                    }
                });
        Button sub100 = createEnergyOffsetButton(energyField, "-", -500);
        Button add100 = createEnergyOffsetButton(energyField, "+", 500);
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(1)).
                addChild(inputOutput).
                addChild(sub100).
                addChild(energyField).
                addChild(add100);
        return panel;
    }

    private Button createEnergyOffsetButton(final TextField energyField, String label, final int amount) {
        return new Button(mc, this).setText(label).setDesiredHeight(14).setDesiredWidth(16).addButtonEvent(new ButtonEvent() {
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

    private void updateButtons() {

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateButtons();
        window.draw();
    }
}

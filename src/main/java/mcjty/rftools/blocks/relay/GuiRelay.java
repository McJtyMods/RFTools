package mcjty.rftools.blocks.relay;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.events.ChoiceEvent;
import mcjty.lib.gui.events.TextEvent;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiRelay extends GenericGuiContainer<RelayTileEntity> {
    public static final int RELAY_WIDTH = 255;
    public static final int RELAY_HEIGHT = 130;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    // Maps from specific label to list of related widgets that needs to be enabled/disabled.
    private Map<String,ImageChoiceLabel> inputOutputs = new HashMap<String, ImageChoiceLabel>();
    private Map<String,TextField> energyValues = new HashMap<String, TextField>();

    public GuiRelay(RelayTileEntity relayTileEntity, Container container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, relayTileEntity, container, RFTools.GUI_MANUAL_MAIN, "prelay");

        xSize = RELAY_WIDTH;
        ySize = RELAY_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        toplevel.addChild(createRedstonePanel());
        for (int i = 0 ; i < 6 ; i++) {
            toplevel.addChild(createSidePanel(i));
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, RELAY_WIDTH, RELAY_HEIGHT));
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

    private Panel createSidePanel(int side) {
        String labelText = String.valueOf(RelayTileEntity.DUNSWE.charAt(side));
        Label label = new Label(mc, this).setText(labelText);
        label.setDesiredWidth(14).setDesiredHeight(14);
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(1)).
                addChild(label).
                addChild(createSubPanel(side, "Off").setDesiredWidth(115)).
                addChild(createSubPanel(side, "On").setDesiredWidth(115));
        return panel;
    }

    private Panel createSubPanel(int side, String redstoneState) {
        String labelText = String.valueOf(RelayTileEntity.DUNSWE.charAt(side));

        int rf;
        boolean input;
        if ("Off".equals(redstoneState)) {
            rf = tileEntity.getRfOff(side);
            input = tileEntity.isInputModeOff(side);
        } else {
            rf = tileEntity.getRfOn(side);
            input = tileEntity.isInputModeOn(side);
        }

        ImageChoiceLabel inputOutput = new ImageChoiceLabel(mc, this).
                setDesiredWidth(14).setDesiredHeight(14).
                addChoice("Output", "Side set to output mode", iconGuiElements, 80, 16).
                addChoice("Input", "Side set to input mode", iconGuiElements, 96, 16).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeSettings();
                    }
                });
        String key = labelText + redstoneState;
        if (input) {
            inputOutput.setCurrentChoice("Input");
        } else {
            inputOutput.setCurrentChoice("Output");
        }
        inputOutputs.put(key, inputOutput);
        TextField energyField = new TextField(mc, this).setTooltips("Amount of RF to input/output", "when redstone is " + redstoneState).
                setDesiredWidth(42).setDesiredHeight(14).
                addTextEvent(new TextEvent() {
                    @Override
                    public void textChanged(Widget parent, String newText) {
                        adjustEnergy((TextField) parent, 0);
                    }
                });
        energyField.setText(String.valueOf(rf));
        Button sub100 = createEnergyOffsetButton(energyField, "-", -500);
        Button add100 = createEnergyOffsetButton(energyField, "+", 500);
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(1)).
                addChild(inputOutput).
                addChild(sub100).
                addChild(energyField).
                addChild(add100);
        energyValues.put(key, energyField);
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
        changeSettings();
    }

    private void changeSettings() {
        List<Argument> arguments = new ArrayList<Argument>();
        for (int i = 0 ; i < 6 ; i++) {
            addArgument(arguments, i, "On");
            addArgument(arguments, i, "Off");
        }

        sendServerCommand(RFToolsMessages.INSTANCE, RelayTileEntity.CMD_SETTINGS, arguments.toArray(new Argument[arguments.size()]));
    }

    private void addArgument(List<Argument> arguments, int i, String suffix) {
        char prefix = RelayTileEntity.DUNSWE.charAt(i);
        String key = prefix + suffix;
        int energy = Integer.parseInt(energyValues.get(key).getText());
        boolean input = "Input".equals(inputOutputs.get(key).getCurrentChoice());
        arguments.add(new Argument(key, energy));
        arguments.add(new Argument(prefix + "In" + suffix, input));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}

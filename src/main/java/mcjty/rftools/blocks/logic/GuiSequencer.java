package mcjty.rftools.blocks.logic;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.events.TextEvent;
import mcjty.gui.layout.HorizontalLayout;
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
import java.util.ArrayList;
import java.util.List;

public class GuiSequencer extends GenericGuiContainer<SequencerTileEntity> {
    public static final int SEQUENCER_WIDTH = 160;
    public static final int SEQUENCER_HEIGHT = 184;

    private List<ImageChoiceLabel> bits = new ArrayList<ImageChoiceLabel>();
    private ChoiceLabel mode;
    private TextField speedField;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiSequencer(SequencerTileEntity sequencerTileEntity, Container container) {
        super(sequencerTileEntity, container);
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - SEQUENCER_WIDTH) / 2;
        int l = (this.height - SEQUENCER_HEIGHT) / 2;

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        initGuiGrid(toplevel);

        Button clearButton = new Button(mc, this).setText("Clear").setTooltips("Clear the grid").setDesiredHeight(13).setDesiredWidth(45).addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                fillGrid(false);
            }
        });
        Button fillButton = new Button(mc, this).setText("Fill").setTooltips("Fill the grid").setDesiredHeight(13).setDesiredWidth(45).addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                fillGrid(true);
            }
        });
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(clearButton).addChild(fillButton);
        toplevel.addChild(buttonPanel);

        initGuiMode();
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

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(mode).addChild(label).addChild(speedField);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(k, l, SEQUENCER_WIDTH, SEQUENCER_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void initGuiGrid(Panel toplevel) {
        for (int row = 0 ; row < 8 ; row++) {
            Panel rowPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(13);
            toplevel.addChild(rowPanel);
            for (int col = 0 ; col < 8 ; col++) {
                final int bit = row * 8 + col;
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).
                        addChoiceEvent(new ChoiceEvent() {
                            @Override
                            public void choiceChanged(Widget parent, String newChoice) {
                                changeBit(bit, newChoice);
                            }
                        }).
                        setDesiredHeight(12).
                        addChoice("0", "Disabled", iconGuiElements, 160, 0).
                        addChoice("1", "Enabled", iconGuiElements, 176, 0);
                choiceLabel.setCurrentChoice(tileEntity.getCycleBit(bit) ? 1 : 0);
                bits.add(choiceLabel);
                rowPanel.addChild(choiceLabel);
            }
        }
    }

    private void initGuiMode() {
        mode = new ChoiceLabel(mc, this).setDesiredHeight(13).setDesiredWidth(55);
        mode.addChoices(SequencerMode.MODE_ONCE1.getDescription());
        mode.addChoices(SequencerMode.MODE_ONCE2.getDescription());
        mode.addChoices(SequencerMode.MODE_LOOP1.getDescription());
        mode.addChoices(SequencerMode.MODE_LOOP2.getDescription());
        mode.addChoices(SequencerMode.MODE_LOOP3.getDescription());
        mode.addChoices(SequencerMode.MODE_LOOP4.getDescription());
        mode.addChoices(SequencerMode.MODE_STEP.getDescription());
        mode.setChoiceTooltip(SequencerMode.MODE_ONCE1.getDescription(), "When a redstone signal is", "received, loop the cycle once.", "Ignore further pulses");
        mode.setChoiceTooltip(SequencerMode.MODE_ONCE2.getDescription(), "When a redstone signal is", "received, loop the cycle once.", "Restart if new pulse arrives");
        mode.setChoiceTooltip(SequencerMode.MODE_LOOP1.getDescription(), "Loop the cycle all the time.", "Ignore redstone signals");
        mode.setChoiceTooltip(SequencerMode.MODE_LOOP2.getDescription(), "Loop the cycle all the time.", "Restart on redstone pulse");
        mode.setChoiceTooltip(SequencerMode.MODE_LOOP3.getDescription(), "Loop the cycle when redstone.", "signal is present. Continue at current step");
        mode.setChoiceTooltip(SequencerMode.MODE_LOOP4.getDescription(), "Loop the cycle when redstone.", "signal is present. Restart on no signal");
        mode.setChoiceTooltip(SequencerMode.MODE_STEP.getDescription(), "Do one step in the cycle", "for every redstone pulse");
        mode.setChoice(tileEntity.getMode().getDescription());
        mode.addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeMode();
            }
        });
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
        sendServerCommand(SequencerTileEntity.CMD_SETDELAY, new Argument("delay", delay));
    }

    private void fillGrid(boolean value) {
        for (int bit = 0 ; bit < 64 ; bit++) {
            bits.get(bit).setCurrentChoice(value ? 1 : 0);
        }
        tileEntity.setCycleBits(0, 63, value);
        sendServerCommand(SequencerTileEntity.CMD_SETBITS,
                new Argument("start", 0),
                new Argument("stop", 63),
                new Argument("choice", value));
    }

    private void changeBit(int bit, String choice) {
        boolean newChoice = "1".equals(choice);
        tileEntity.setCycleBit(bit, newChoice);
        sendServerCommand(SequencerTileEntity.CMD_SETBIT,
                new Argument("bit", bit),
                new Argument("choice", newChoice));
    }

    private void changeMode() {
        SequencerMode newMode = SequencerMode.getMode(mode.getCurrentChoice());
        tileEntity.setMode(newMode);
        sendServerCommand(SequencerTileEntity.CMD_MODE, new Argument("mode", newMode.getDescription()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

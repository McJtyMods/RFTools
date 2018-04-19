package mcjty.rftools.blocks.logic.sequencer;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiSequencer extends GenericGuiContainer<SequencerTileEntity> {
    public static final int SEQUENCER_WIDTH = 160;
    public static final int SEQUENCER_HEIGHT = 208;

    private List<ImageChoiceLabel> bits = new ArrayList<>();
    private ChoiceLabel mode;
    private TextField speedField, countField;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiSequencer(SequencerTileEntity sequencerTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, sequencerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "sequencer");
        xSize = SEQUENCER_WIDTH;
        ySize = SEQUENCER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        initGuiGrid(toplevel);

        Button clearButton = new Button(mc, this).setText("Clear").setTooltips("Clear the grid").setDesiredHeight(13).setDesiredWidth(38).addButtonEvent(parent -> fillGrid());
        Button flipButton = new Button(mc, this).setText("Flip").setTooltips("Invert all values in the grid").setDesiredHeight(13).setDesiredWidth(34).addButtonEvent(parent -> flipGrid());
        Label endLabel = new Label(mc, this).setText("End on:");
        ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).
                addChoiceEvent((parent, newChoice) -> setEndState(newChoice)).
                setDesiredHeight(11).
                addChoice("0", "Disabled", iconGuiElements, 160, 0).
                addChoice("1", "Enabled", iconGuiElements, 176, 0);
        choiceLabel.setCurrentChoice(tileEntity.getEndState() ? 1 : 0);
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChildren(clearButton, flipButton, endLabel, choiceLabel);
        toplevel.addChild(buttonPanel);

        initGuiMode();
        Label speedLabel = new Label(mc, this).setText("Delay:");

        speedField = new TextField(mc, this).addTextEvent((parent, newText) -> setDelay());
        int delay = tileEntity.getDelay();
        if (delay <= 0) {
            delay = 1;
        }
        speedField.setText(String.valueOf(delay));

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChildren(mode, speedLabel, speedField);
        toplevel.addChild(bottomPanel);

        Label countLabel = new Label(mc, this).setText("Sequence length:");

        countField = new TextField(mc, this).addTextEvent((parent, newText) -> setCount());
        int count = tileEntity.getStepCount();
        if (count < 1 || count > 64) {
            count = 64;
        }
        countField.setText(String.valueOf(count));

        Panel countPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChildren(countLabel, countField);
        toplevel.addChild(countPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, SEQUENCER_WIDTH, SEQUENCER_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void initGuiGrid(Panel toplevel) {
        for (int row = 0 ; row < 8 ; row++) {
            Panel rowPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(13);
            toplevel.addChild(rowPanel);
            for (int col = 0 ; col < 8 ; col++) {
                final int bit = row * 8 + col;
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).
                        addChoiceEvent((parent, newChoice) -> changeBit(bit, newChoice)).
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
        mode.addChoiceEvent((parent, newChoice) -> changeMode());
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
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_SETDELAY, new Argument("delay", delay));
    }

    private void setCount() {
        String c = countField.getText();
        int count;
        try {
            count = Integer.parseInt(c);
        } catch (NumberFormatException e) {
            count = 64;
        }
        tileEntity.setStepCount(count);
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_SETCOUNT, new Argument("count", count));
    }

    private void setEndState(String choice) {
        boolean newChoice = "1".equals(choice);
        tileEntity.setEndState(newChoice);
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_SETENDSTATE,
                new Argument("endState", newChoice));
    }

    private void flipGrid() {
        for(ImageChoiceLabel bit : bits) {
            bit.setCurrentChoice(1 - bit.getCurrentChoiceIndex());
        }
        tileEntity.flipCycleBits();
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_FLIPBITS);
    }

    private void fillGrid() {
        for(ImageChoiceLabel bit : bits) {
            bit.setCurrentChoice(0);
        }
        tileEntity.clearCycleBits();
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_CLEARBITS);
    }

    private void changeBit(int bit, String choice) {
        boolean newChoice = "1".equals(choice);
        tileEntity.setCycleBit(bit, newChoice);
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_SETBIT,
                new Argument("bit", bit),
                new Argument("choice", newChoice));
    }

    private void changeMode() {
        SequencerMode newMode = SequencerMode.getMode(mode.getCurrentChoice());
        tileEntity.setMode(newMode);
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_MODE, new Argument("mode", newMode.getDescription()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}

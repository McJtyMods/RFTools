package mcjty.rftools.blocks.logic.sequencer;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.typed.TypedMap;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GuiSequencer extends GenericGuiContainer<SequencerTileEntity> {
    public static final int SEQUENCER_WIDTH = 160;
    public static final int SEQUENCER_HEIGHT = 208;

    private List<ImageChoiceLabel> bits = new ArrayList<>();

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
                setName("endchoice").
                setChannel("endchoice").
                setDesiredHeight(11).
                addChoice("0", "Disabled", iconGuiElements, 160, 0).
                addChoice("1", "Enabled", iconGuiElements, 176, 0);
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChildren(clearButton, flipButton, endLabel, choiceLabel);
        toplevel.addChild(buttonPanel);

        ChoiceLabel mode = initGuiMode();
        Label speedLabel = new Label(mc, this).setText("Delay:");

        TextField speedField = new TextField(mc, this)
                .setName("speed")
                .setChannel("speed");

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChildren(mode, speedLabel, speedField);
        toplevel.addChild(bottomPanel);

        Label countLabel = new Label(mc, this).setText("Sequence length:");

        TextField countField = new TextField(mc, this)
                .setName("count")
                .setChannel("count");

        Panel countPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChildren(countLabel, countField);
        toplevel.addChild(countPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, SEQUENCER_WIDTH, SEQUENCER_HEIGHT));
        window = new Window(this, toplevel);

        initializeFields();
        setupEvents();
    }

    private void initializeFields() {
        ImageChoiceLabel choiceLabel = window.findChild("endchoice");
        choiceLabel.setCurrentChoice(tileEntity.getEndState() ? 1 : 0);

        TextField countField = window.findChild("count");
        int count = tileEntity.getStepCount();
        if (count < 1 || count > 64) {
            count = 64;
        }
        countField.setText(String.valueOf(count));

        TextField speedField = window.findChild("speed");
        int delay = tileEntity.getDelay();
        if (delay <= 0) {
            delay = 1;
        }
        speedField.setText(String.valueOf(delay));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                final int bit = row * 8 + col;
                ImageChoiceLabel label = window.findChild("grid" + bit);
                label.setCurrentChoice(tileEntity.getCycleBit(bit) ? 1 : 0);
                bits.add(label);
            }
        }

        ChoiceLabel mode = window.findChild("mode");
        mode.setChoice(tileEntity.getMode().getDescription());
    }

    private void setupEvents() {
        window.addChannelEvent("mode", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_MODE, params));
        window.addChannelEvent("count", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_SETCOUNT, params));
        window.addChannelEvent("speed", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_SETDELAY, params));
        window.addChannelEvent("endchoice", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_SETENDSTATE, params));
        window.addChannelEvent("grid", (source, params) -> {
            int bit = Integer.parseInt(source.getName().substring("grid".length()));
            changeBit(bit, params.get(ImageChoiceLabel.PARAM_CHOICE));
        });
    }

    private void initGuiGrid(Panel toplevel) {
        for (int row = 0 ; row < 8 ; row++) {
            Panel rowPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(13);
            toplevel.addChild(rowPanel);
            for (int col = 0 ; col < 8 ; col++) {
                final int bit = row * 8 + col;
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).
                        setName("grid" + bit).
                        setChannel("grid").
                        setDesiredHeight(12).
                        addChoice("0", "Disabled", iconGuiElements, 160, 0).
                        addChoice("1", "Enabled", iconGuiElements, 176, 0);
                bits.add(choiceLabel);
                rowPanel.addChild(choiceLabel);
            }
        }
    }

    private ChoiceLabel initGuiMode() {
        ChoiceLabel mode = new ChoiceLabel(mc, this)
                .setName("mode")
                .setChannel("mode")
                .setDesiredHeight(13).setDesiredWidth(55);
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
        return mode;
    }

    private void flipGrid() {
        for(ImageChoiceLabel bit : bits) {
            bit.setCurrentChoice(1 - bit.getCurrentChoiceIndex());
        }
        tileEntity.flipCycleBits();
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_FLIPBITS, TypedMap.EMPTY);
    }

    private void fillGrid() {
        for(ImageChoiceLabel bit : bits) {
            bit.setCurrentChoice(0);
        }
        tileEntity.clearCycleBits();
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_CLEARBITS, TypedMap.EMPTY);
    }

    private void changeBit(int bit, String choice) {
        boolean newChoice = "1".equals(choice);
        tileEntity.setCycleBit(bit, newChoice);
        sendServerCommand(RFToolsMessages.INSTANCE, SequencerTileEntity.CMD_SETBIT,
                TypedMap.builder()
                        .put(SequencerTileEntity.PARAM_BIT, bit)
                        .put(SequencerTileEntity.PARAM_CHOICE, newChoice)
                        .build());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}

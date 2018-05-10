package mcjty.rftools.blocks.logic.sequencer;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.lib.typed.TypedMap;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GuiSequencer extends GenericGuiContainer<SequencerTileEntity> {

    private List<ImageChoiceLabel> bits = new ArrayList<>();

    public GuiSequencer(SequencerTileEntity sequencerTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, sequencerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "sequencer");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/sequencer.gui"));
        super.initGui();

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
        window.event("grid", (source, params) -> {
            int bit = Integer.parseInt(source.getName().substring("grid".length()));
            changeBit(bit, params.get(ImageChoiceLabel.PARAM_CHOICE));
        });
        window.event("flip", (source, params) -> flipGrid());
        window.event("clear", (source, params) -> fillGrid());
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

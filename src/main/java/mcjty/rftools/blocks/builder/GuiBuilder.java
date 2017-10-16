package mcjty.rftools.blocks.builder;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;

import static mcjty.rftools.blocks.builder.BuilderTileEntity.*;

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity> {
    public static final int BUILDER_WIDTH = 180;
    public static final int BUILDER_HEIGHT = 152;

    private EnergyBar energyBar;
    private ImageChoiceLabel redstoneMode;

    private ChoiceLabel modeChoice;
    private ImageChoiceLabel silentMode;
    private ImageChoiceLabel supportMode;
    private ImageChoiceLabel entityMode;
    private ImageChoiceLabel loopMode;
    private ImageChoiceLabel waitMode;
    private ImageChoiceLabel hilightMode;
    private Button currentLevel;

    private ImageChoiceLabel anchor[] = new ImageChoiceLabel[4];
    private ChoiceLabel rotateButton;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/builder.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiBuilder(BuilderTileEntity builderTileEntity, BuilderContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, builderTileEntity, container, RFTools.GUI_MANUAL_SHAPE, "builder");
        setCurrentRF(builderTileEntity.getEnergyStored(EnumFacing.DOWN));

        xSize = BUILDER_WIDTH;
        ySize = BUILDER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(EnumFacing.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 6, 8, 59)).setShowText(false);
        energyBar.setValue(getCurrentRF());

        initRedstoneMode();

        currentLevel = new Button(mc, this);
        currentLevel.setText("Y:").setTooltips("Current level the builder is at", TextFormatting.YELLOW + "Press to restart!").setLayoutHint(new PositionalLayout.PositionalHint(81, 31, 45, 13))
            .addButtonEvent(parent -> restart());

        Panel positionPanel = setupPositionPanel();
        Panel modePanel = setupModePanel();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(modePanel).addChild(positionPanel).addChild(currentLevel).addChild(redstoneMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFTools.MODID);
        tileEntity.requestCurrentLevel();
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent((parent, newChoice) -> changeRedstoneMode()).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", guiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", guiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", guiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(106, 46, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRSMode(RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()]);
        sendServerCommand(RFToolsMessages.INSTANCE, BuilderTileEntity.CMD_MODE,
                new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()].getDescription()));
    }

    private void restart() {
        sendServerCommand(RFToolsMessages.INSTANCE, BuilderTileEntity.CMD_RESTART,
                new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()].getDescription()));
    }

    private Panel setupPositionPanel() {
        rotateButton = new ChoiceLabel(mc, this).addChoices(ROTATE_0, ROTATE_90, ROTATE_180, ROTATE_270).setLayoutHint(new PositionalLayout.PositionalHint(4, 4, 36, 14)).
                setTooltips("Set the horizontal rotation angle").
                addChoiceEvent(
                        (parent, newChoice) -> updateRotate()
                );
        switch (tileEntity.getRotate()) {
            case 0: rotateButton.setChoice(ROTATE_0); break;
            case 1: rotateButton.setChoice(ROTATE_90); break;
            case 2: rotateButton.setChoice(ROTATE_180); break;
            case 3: rotateButton.setChoice(ROTATE_270); break;
        }

        Panel positionPanel = new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(128, 6, 44, 59))
                .addChild(rotateButton)
                .setFilledRectThickness(-2)
                .setFilledBackground(StyleConfig.colorListBackground);

        String[] choiceDescriptions = { "Builder at south west corner", "Builder at south east corner", "Builder at north west corner", "Builder at north east corner" };
        for (int y = 0 ; y <= 1 ; y++) {
            for (int x = 0 ; x <= 1 ; x++) {
                final int index = x + y * 2;
                anchor[index] = new ImageChoiceLabel(mc, this)
                        .setWithBorder(true)
                        .setHighlightedChoice(1)
                        .setLayoutHint(new PositionalLayout.PositionalHint(4 + x * 19, 18 + (1 - y) * 19, 17, 17))
                        .setTooltips("Set the anchor where you want to", "place the blocks in front of the", "builder");
                anchor[index].addChoice("off", choiceDescriptions[index], guiElements, (7+index*2) * 16, 4*16);
                anchor[index].addChoice("on", choiceDescriptions[index], guiElements, (6+index*2) * 16, 4*16);
                anchor[index].addChoiceEvent((widget, s) -> selectAnchor(index));
                positionPanel.addChild(anchor[index]);
            }
        }
        if (!isShapeCard()) {
            anchor[tileEntity.getAnchor()].setCurrentChoice(1);
        }
        return positionPanel;
    }

    private Panel setupModePanel() {
        modeChoice = new ChoiceLabel(mc, this).addChoices(MODES[MODE_COPY], MODES[MODE_MOVE], MODES[MODE_SWAP], MODES[MODE_BACK], MODES[MODE_COLLECT])
                .setTooltips("Set the building mode").setLayoutHint(new PositionalLayout.PositionalHint(9, 4, 42, 14))
                .setChoiceTooltip(MODES[MODE_COPY], "Copy from space chamber to here", "Chest on top or below with materials")
                .setChoiceTooltip(MODES[MODE_MOVE], "Move from space chamber to here")
                .setChoiceTooltip(MODES[MODE_SWAP], "Swap space chamber contents with here")
                .setChoiceTooltip(MODES[MODE_BACK], "Move back from here to space chamber")
                .setChoiceTooltip(MODES[MODE_COLLECT], "Collect items in space chamber", "Items will go to chest on top or below")
                .addChoiceEvent((parent, newChoice) -> updateMode());
        modeChoice.setChoice(MODES[tileEntity.getMode()]);

        silentMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(4, 18, 17, 17))
                .setWithBorder(true)
                .setHighlightedChoice(1)
                .setTooltips("Suppress the placement/breaking sound", "when moving blocks")
                .addChoiceEvent((parent, newChoice) -> setSilentMode());
        silentMode.addChoice("off", "Moving blocks make sound", guiElements, 11*16, 3*16);
        silentMode.addChoice("on", "Block sounds are muted", guiElements, 10 * 16, 3 * 16);
        silentMode.setCurrentChoice(tileEntity.isSilent() ? 1 : 0);

        supportMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(22, 18, 17, 17))
                .setWithBorder(true)
                .setHighlightedChoice(1)
                .setTooltips("Use supporting blocks when moving.", "Useful for liquids, gravel, ...")
                .addChoiceEvent((parent, newChoice) -> setSupportMode());
        supportMode.addChoice("off", "Support/preview mode disabled", guiElements, 7*16, 3*16);
        supportMode.addChoice("on", "Support/preview mode enabled", guiElements, 6*16, 3*16);
        supportMode.setCurrentChoice(tileEntity.hasSupportMode() ? 1 : 0);

        entityMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(4, 37, 17, 17))
                .setWithBorder(true)
                .setHighlightedChoice(1)
                .setTooltips("Move entities")
                .addChoiceEvent((parent, newChoice) -> setEntityMode());
        entityMode.addChoice("off", "Entities are not moved", guiElements, 9*16, 3*16);
        entityMode.addChoice("on", "Entities are moved", guiElements, 8*16, 3*16);
        entityMode.setCurrentChoice(tileEntity.hasEntityMode() ? 1 : 0);

        loopMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(22, 37, 17, 17))
                .setWithBorder(true)
                .setHighlightedChoice(1)
                .setTooltips("Loop mode")
                .addChoiceEvent((parent, newChoice) -> setLoopMode());
        loopMode.addChoice("off", "Do a single run and stop", guiElements, 13*16, 3*16);
        loopMode.addChoice("on", "Keep running with redstone signal", guiElements, 12*16, 3*16);
        loopMode.setCurrentChoice(tileEntity.hasLoopMode() ? 1 : 0);

        waitMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(40, 18, 17, 17))
                .setWithBorder(true)
                .setHighlightedChoice(1)
                .setTooltips("Wait mode")
                .addChoiceEvent((parent, newChoice) -> setWaitMode());
        waitMode.addChoice("off", "Don't wait on a position if\nthe operation is not possible", guiElements, 7*16, 5*16);
        waitMode.addChoice("on", "If the operation is not possible\nwait on the current position", guiElements, 6*16, 5*16);
        waitMode.setCurrentChoice(tileEntity.isWaitMode() ? 1 : 0);

        hilightMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(40, 37, 17, 17))
                .setWithBorder(true)
                .setHighlightedChoice(1)
                .setTooltips("Hilight mode")
                .addChoiceEvent((parent, newChoice) -> setHilightMode());
        hilightMode.addChoice("off", "No hilighting", guiElements, 9*16, 5*16);
        hilightMode.addChoice("on", "Visually hilight the position\nthe Builder is working on", guiElements, 8*16, 5*16);
        hilightMode.setCurrentChoice(tileEntity.isHilightMode() ? 1 : 0);

        return new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(19, 6, 61, 59))
                .addChild(modeChoice).addChild(silentMode).addChild(supportMode).addChild(entityMode).addChild(loopMode)
                .addChild(waitMode).addChild(hilightMode)
                .setFilledRectThickness(-2)
                .setFilledBackground(StyleConfig.colorListBackground);
    }

    private void setLoopMode() {
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETLOOP, new Argument("loop", loopMode.getCurrentChoiceIndex() == 1));
    }

    private void setSilentMode() {
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETSILENT, new Argument("silent", silentMode.getCurrentChoiceIndex() == 1));
    }

    private void setSupportMode() {
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETSUPPORT, new Argument("support", supportMode.getCurrentChoiceIndex() == 1));
    }

    private void setEntityMode() {
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETENTITIES, new Argument("entities", entityMode.getCurrentChoiceIndex() == 1));
    }

    private void setWaitMode() {
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETWAIT, new Argument("wait", waitMode.getCurrentChoiceIndex() == 1));
    }

    private void setHilightMode() {
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETHILIGHT, new Argument("hilight", hilightMode.getCurrentChoiceIndex() == 1));
    }

    private void selectAnchor(int index) {
        updateAnchorSettings(index);
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETANCHOR, new Argument("anchor", index));
    }

    private void updateAnchorSettings(int index) {
        for (int i = 0 ; i < anchor.length ; i++) {
            if (isShapeCard()) {
                anchor[i].setCurrentChoice(0);
            } else {
                if ((anchor[i].getCurrentChoiceIndex() == 1) != (i == index)) {
                    anchor[i].setCurrentChoice(i == index ? 1 : 0);
                }
            }
        }
    }

    private void updateMode() {
        String currentChoice = modeChoice.getCurrentChoice();
        int mode = 0;
        for (int i = 0 ; i < MODES.length ; i++) {
            if (currentChoice.equals(MODES[i])) {
                mode = i;
                break;
            }
        }
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETMODE, new Argument("mode", mode));
    }

    private void updateRotate() {
        String choice = rotateButton.getCurrentChoice();
        int index = 0;
        if (ROTATE_0.equals(choice)) {
            index = 0;
        } else if (ROTATE_90.equals(choice)) {
            index = 1;
        } else if (ROTATE_180.equals(choice)) {
            index = 2;
        } else if (ROTATE_270.equals(choice)) {
            index = 3;
        }
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETROTATE, new Argument("rotate", index));
    }

    private boolean isShapeCard() {
        ItemStack card = tileEntity.getStackInSlot(BuilderContainer.SLOT_TAB);
        return ItemStackTools.isValid(card) && card.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        int cury = getCurrentLevelClientSide();
        currentLevel.setText("Y: " + (cury == -1 ? "stop" : cury));

        ItemStack card = tileEntity.getStackInSlot(BuilderContainer.SLOT_TAB);
        boolean enabled;
        if (ItemStackTools.isEmpty(card)) {
            enabled = false;
        } else if (card.getItem() == BuilderSetup.shapeCardItem) {
            enabled = false;
        } else {
            enabled = true;
        }
        modeChoice.setEnabled(enabled);
        rotateButton.setEnabled(enabled);
        updateAnchorSettings(tileEntity.getAnchor());

        drawWindow();

        energyBar.setValue(getCurrentRF());

        tileEntity.requestRfFromServer(RFTools.MODID);
        tileEntity.requestCurrentLevel();
    }
}

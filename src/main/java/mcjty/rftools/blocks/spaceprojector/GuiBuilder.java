package mcjty.rftools.blocks.spaceprojector;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.network.Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

import static mcjty.rftools.blocks.spaceprojector.BuilderTileEntity.*;

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity> {
    public static final int BUILDER_WIDTH = 180;
    public static final int BUILDER_HEIGHT = 152;

    private EnergyBar energyBar;
    private ChoiceLabel modeChoice;
    private ImageChoiceLabel silentMode;
    private ImageChoiceLabel supportMode;
    private ImageChoiceLabel entityMode;
    private ImageChoiceLabel loopMode;

    private ToggleButton anchor[] = new ToggleButton[4];
    private String[] anchorLabels = new String[] { "O", "O", "O", "O" };
    private ChoiceLabel rotateButton;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/spaceprojector.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiBuilder(BuilderTileEntity builderTileEntity, BuilderContainer container) {
        super(builderTileEntity, container, RFTools.GUI_MANUAL_MAIN, "builder");
        builderTileEntity.setCurrentRF(builderTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = BUILDER_WIDTH;
        ySize = BUILDER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        modeChoice = new ChoiceLabel(mc, this).addChoices(MODES[MODE_COPY], MODES[MODE_MOVE], MODES[MODE_SWAP], MODES[MODE_BACK]).
                setTooltips("Set the building mode").setLayoutHint(new PositionalLayout.PositionalHint(48, 7, 45, 14)).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateMode();
                    }
                });
        modeChoice.setChoice(MODES[tileEntity.getMode()]);

        rotateButton = new ChoiceLabel(mc, this).addChoices(ROTATE_0, ROTATE_90, ROTATE_180, ROTATE_270).setLayoutHint(new PositionalLayout.PositionalHint(130, 7, 40, 14)).
                setTooltips("Set the horizontal rotation angle").
                addChoiceEvent(
                        new ChoiceEvent() {
                            @Override
                            public void choiceChanged(Widget parent, String newChoice) {
                                updateRotate();
                            }
                        }
                );
        switch (tileEntity.getRotate()) {
            case 0: rotateButton.setChoice(ROTATE_0); break;
            case 1: rotateButton.setChoice(ROTATE_90); break;
            case 2: rotateButton.setChoice(ROTATE_180); break;
            case 3: rotateButton.setChoice(ROTATE_270); break;
        }

        silentMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(48, 24, 16, 16)).
                setTooltips("Suppress the placement/breaking sound", "when moving blocks").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        setSilentMode();
                    }
                });
        silentMode.addChoice("off", "Moving blocks make sound", guiElements, 11*16, 3*16);
        silentMode.addChoice("on", "Block sounds are muted", guiElements, 10 * 16, 3 * 16);
        silentMode.setCurrentChoice(tileEntity.isSilent() ? 1 : 0);

        supportMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(66, 24, 16, 16)).
                setTooltips("Use supporting blocks when moving.", "Useful for liquids, gravel, ...").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        setSupportMode();
                    }
                });
        supportMode.addChoice("off", "Support mode is disabled", guiElements, 7*16, 3*16);
        supportMode.addChoice("on", "Support mode is enabled", guiElements, 6*16, 3*16);
        supportMode.setCurrentChoice(tileEntity.hasSupportMode() ? 1 : 0);

        entityMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(48, 42, 16, 16)).
                setTooltips("Move entities").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        setEntityMode();
                    }
                });
        entityMode.addChoice("off", "Entities are not moved", guiElements, 9*16, 3*16);
        entityMode.addChoice("on", "Entities are moved", guiElements, 8*16, 3*16);
        entityMode.setCurrentChoice(tileEntity.hasEntityMode() ? 1 : 0);

        loopMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(66, 42, 16, 16)).
                setTooltips("Loop mode").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        setLoopMode();
                    }
                });
        loopMode.addChoice("off", "Do a single run and stop", guiElements, 13*16, 3*16);
        loopMode.addChoice("on", "Keep running with redstone signal", guiElements, 12*16, 3*16);
        loopMode.setCurrentChoice(tileEntity.hasLoopMode() ? 1 : 0);

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(modeChoice).addChild(rotateButton).addChild(silentMode).addChild(supportMode).addChild(entityMode).addChild(loopMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        for (int y = 0 ; y <= 1 ; y++) {
            for (int x = 0 ; x <= 1 ; x++) {
                final int index = x + y * 2;
                anchor[index] = new ToggleButton(mc, this).setText(anchorLabels[index]).setLayoutHint(new PositionalLayout.PositionalHint(132 + x * 20, 24 + (1-y) * 15, 18, 13)).
                    setTooltips("Set the anchor where you want to", "place the blocks in front of the", "builder");
                anchor[index].addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        selectAnchor(index);
                    }
                });
                toplevel.addChild(anchor[index]);
            }
        }
        anchor[tileEntity.getAnchor()].setPressed(true);

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer();
    }

    private void setLoopMode() {
        sendServerCommand(CMD_SETLOOP, new Argument("loop", loopMode.getCurrentChoiceIndex() == 1));
    }

    private void setSilentMode() {
        sendServerCommand(CMD_SETSILENT, new Argument("silent", silentMode.getCurrentChoiceIndex() == 1));
    }

    private void setSupportMode() {
        sendServerCommand(CMD_SETSUPPORT, new Argument("support", supportMode.getCurrentChoiceIndex() == 1));
    }

    private void setEntityMode() {
        sendServerCommand(CMD_SETENTITIES, new Argument("entities", entityMode.getCurrentChoiceIndex() == 1));
    }

    private void selectAnchor(int index) {
        for (int i = 0 ; i < anchor.length ; i++) {
            if (anchor[i].isPressed() != (i == index)) {
                anchor[i].setPressed(i == index);
            }
        }
        sendServerCommand(CMD_SETANCHOR, new Argument("anchor", index));
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
        sendServerCommand(CMD_SETMODE, new Argument("mode", mode));
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
        sendServerCommand(CMD_SETROTATE, new Argument("rotate", index));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer();
    }
}

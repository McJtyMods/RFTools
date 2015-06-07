package mcjty.rftools.blocks.spaceprojector;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

import static mcjty.rftools.blocks.spaceprojector.BuilderTileEntity.*;

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity> {
    public static final int BUILDER_WIDTH = 180;
    public static final int BUILDER_HEIGHT = 152;

    private EnergyBar energyBar;
    private ChoiceLabel modeChoice;
    private ToggleButton silentMode;
    private ToggleButton supportMode;

    private ToggleButton anchor[] = new ToggleButton[4];
    private String[] anchorLabels = new String[] { "O", "O", "O", "O" };
    private ChoiceLabel rotateButton;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/spaceprojector.png");

    public GuiBuilder(BuilderTileEntity builderTileEntity, BuilderContainer container) {
        super(builderTileEntity, container);
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
                setTooltips("Set the building mode").setLayoutHint(new PositionalLayout.PositionalHint(100, 7, 50, 14)).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateMode();
                    }
                });
        modeChoice.setChoice(MODES[tileEntity.getMode()]);

        rotateButton = new ChoiceLabel(mc, this).addChoices(ROTATE_0, ROTATE_90, ROTATE_180, ROTATE_270).setLayoutHint(new PositionalLayout.PositionalHint(48, 7, 45, 14)).
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

        silentMode = new ToggleButton(mc, this).setCheckMarker(true).setText("Silent").setLayoutHint(new PositionalLayout.PositionalHint(48, 23, 45, 14)).
                setTooltips("Suppress the placement/breaking sound", "when moving blocks").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        setSilentMode();
                    }
                });
        silentMode.setPressed(tileEntity.isSilent());

        supportMode = new ToggleButton(mc, this).setCheckMarker(true).setText("Support").setLayoutHint(new PositionalLayout.PositionalHint(48, 40, 45, 14)).
                setTooltips("Use supporting blocks when moving.", "Useful for liquids, gravel, ...").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        setSupportMode();
                    }
                });
        supportMode.setPressed(tileEntity.hasSupportMode());

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(modeChoice).addChild(rotateButton).addChild(silentMode).addChild(supportMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        for (int y = 0 ; y <= 1 ; y++) {
            for (int x = 0 ; x <= 1 ; x++) {
                final int index = x + y * 2;
                anchor[index] = new ToggleButton(mc, this).setText(anchorLabels[index]).setLayoutHint(new PositionalLayout.PositionalHint(100 + x * 20, 24 + (1-y) * 15, 18, 13)).
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

    private void setSilentMode() {
        sendServerCommand(CMD_SETSILENT, new Argument("silent", silentMode.isPressed()));
    }

    private void setSupportMode() {
        sendServerCommand(CMD_SETSUPPORT, new Argument("support", supportMode.isPressed()));
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
        window.draw();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer();
    }
}

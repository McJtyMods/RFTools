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

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity> {
    public static final int BUILDER_WIDTH = 180;
    public static final int BUILDER_HEIGHT = 152;

    private EnergyBar energyBar;
    private ChoiceLabel modeChoice;

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

        modeChoice = new ChoiceLabel(mc, this).addChoices(BuilderTileEntity.MODE_MOVE, BuilderTileEntity.MODE_COPY, BuilderTileEntity.MODE_SWAP, BuilderTileEntity.MODE_BACK).
                setTooltips("Set the building mode").setLayoutHint(new PositionalLayout.PositionalHint(100, 7, 50, 14)).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateMode();
                    }
                });
        modeChoice.setChoice(tileEntity.getMode());

        rotateButton = new ChoiceLabel(mc, this).addChoices(BuilderTileEntity.ROTATE_0, BuilderTileEntity.ROTATE_90, BuilderTileEntity.ROTATE_180, BuilderTileEntity.ROTATE_270).setLayoutHint(new PositionalLayout.PositionalHint(48, 7, 45, 14)).
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
            case 0: rotateButton.setChoice(BuilderTileEntity.ROTATE_0); break;
            case 1: rotateButton.setChoice(BuilderTileEntity.ROTATE_90); break;
            case 2: rotateButton.setChoice(BuilderTileEntity.ROTATE_180); break;
            case 3: rotateButton.setChoice(BuilderTileEntity.ROTATE_270); break;
        }

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(modeChoice).addChild(rotateButton);
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

    private void selectAnchor(int index) {
        for (int i = 0 ; i < anchor.length ; i++) {
            if (anchor[i].isPressed() != (i == index)) {
                anchor[i].setPressed(i == index);
            }
        }
        sendServerCommand(BuilderTileEntity.CMD_SETANCHOR, new Argument("anchor", index));
    }

    private void updateMode() {
        sendServerCommand(BuilderTileEntity.CMD_SETMODE, new Argument("mode", modeChoice.getCurrentChoice()));
    }

    private void updateRotate() {
        String choice = rotateButton.getCurrentChoice();
        int index = 0;
        if (BuilderTileEntity.ROTATE_0.equals(choice)) {
            index = 0;
        } else if (BuilderTileEntity.ROTATE_90.equals(choice)) {
            index = 1;
        } else if (BuilderTileEntity.ROTATE_180.equals(choice)) {
            index = 2;
        } else if (BuilderTileEntity.ROTATE_270.equals(choice)) {
            index = 3;
        }
        sendServerCommand(BuilderTileEntity.CMD_SETROTATE, new Argument("rotate", index));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer();
    }
}

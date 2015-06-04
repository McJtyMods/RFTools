package mcjty.rftools.blocks.spaceprojector;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.RedstoneMode;
import mcjty.rftools.network.Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity> {
    public static final int BUILDER_WIDTH = 180;
    public static final int BUILDER_HEIGHT = 152;

    private EnergyBar energyBar;
    private ChoiceLabel modeChoice;

    private ToggleButton anchor[] = new ToggleButton[9];
    private String[] anchorLabels= new String[] { "SW", "SC", "SE", "CW", "WC", "WE", "NW", "NC", "NE" };
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

        rotateButton = new ChoiceLabel(mc, this).addChoices("0°", "90°", "180°", "270°").setLayoutHint(new PositionalLayout.PositionalHint(48, 7, 45, 14));

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(modeChoice).addChild(rotateButton);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        for (int y = 0 ; y <= 2 ; y++) {
            for (int x = 0 ; x <= 2 ; x++) {
                final int index = x + y * 3;
                anchor[index] = new ToggleButton(mc, this).setText(anchorLabels[index]).setLayoutHint(new PositionalLayout.PositionalHint(100 + x * 20, 24 + y * 15, 18, 13));
                anchor[index].addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        selectAnchor(index);
                    }
                });
                toplevel.addChild(anchor[index]);
            }
        }

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer();
    }

    private void selectAnchor(int index) {
        for (int i = 0 ; i < anchor.length ; i++) {
            if (i != index) {
                anchor[i].setPressed(false);
            }
        }
    }

    private void updateMode() {
        sendServerCommand(BuilderTileEntity.CMD_SETMODE, new Argument("mode", modeChoice.getCurrentChoice()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer();
    }
}

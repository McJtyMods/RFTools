package com.mcjty.rftools.blocks.itemfilter;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.ImageChoiceLabel;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiDimletFilter extends GenericGuiContainer<DimletFilterTileEntity> {
    public static final int DIMLETFILTER_WIDTH = 195;
    public static final int DIMLETFILTER_HEIGHT = 212;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimletfilter.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ImageChoiceLabel[] bits = new ImageChoiceLabel[6];

    public GuiDimletFilter(DimletFilterTileEntity dimletFilterTileEntity, DimletFilterContainer container) {
        super(dimletFilterTileEntity, container);

        xSize = DIMLETFILTER_WIDTH;
        ySize = DIMLETFILTER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        int[] inputMode = tileEntity.getInputMode();

        for (ForgeDirection direction : ForgeDirection.values()) {
            if (!ForgeDirection.UNKNOWN.equals(direction)) {
                final int side = direction.ordinal();
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(25, 22 + side * 13, 12, 12)).
                        addChoice("0", "Disabled", iconGuiElements, 160, 0).
                        addChoice("1", "Input", iconGuiElements, 96, 16).
                        addChoice("2", "Output", iconGuiElements, 80, 16);
                bits[side] = choiceLabel;
                if (inputMode[side] == DimletFilterTileEntity.INPUT) {
                    choiceLabel.setCurrentChoice(1);
                } else if (inputMode[side] == DimletFilterTileEntity.OUTPUT) {
                    choiceLabel.setCurrentChoice(2);
                } else {
                    choiceLabel.setCurrentChoice(0);
                }
                choiceLabel.addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeMode(side);
                    }
                });
                toplevel.addChild(choiceLabel);
            }
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }



    private void changeMode(int side) {
        ImageChoiceLabel choiceLabel = bits[side];
        int input = choiceLabel.getCurrentChoice();
        sendServerCommand(DimletFilterTileEntity.CMD_SETMODE,
                new Argument("side", side),
                new Argument("input", input));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

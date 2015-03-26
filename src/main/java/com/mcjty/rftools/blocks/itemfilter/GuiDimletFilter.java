package com.mcjty.rftools.blocks.itemfilter;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.network.Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.Rectangle;

public class GuiDimletFilter extends GenericGuiContainer<DimletFilterTileEntity> {
    public static final int DIMLETFILTER_WIDTH = 195;
    public static final int DIMLETFILTER_HEIGHT = 212;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimletfilter.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ImageChoiceLabel[] bits = new ImageChoiceLabel[6];
    private TextField[] minText = new TextField[6];
    private TextField[] maxText = new TextField[6];
    private ChoiceLabel[] types = new ChoiceLabel[6];

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
        int[] minRarity = tileEntity.getMinRarity();
        int[] maxRarity = tileEntity.getMaxRarity();
        DimletType[] dimletTypes = tileEntity.getTypes();

        for (ForgeDirection direction : ForgeDirection.values()) {
            if (!ForgeDirection.UNKNOWN.equals(direction)) {
                final int side = direction.ordinal();
                Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(18, 21 + side * 13, 174, 12));
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).setDesiredWidth(12).setDesiredHeight(12). //setLayoutHint(new PositionalLayout.PositionalHint(25, 22 + side * 13, 12, 12)).
                        addChoice("0", "Disabled", iconGuiElements, 160, 0).
                        addChoice("1", "Input", iconGuiElements, 96, 16).
                        addChoice("2", "Output", iconGuiElements, 80, 16);
                bits[side] = choiceLabel;
                choiceLabel.setCurrentChoice(inputMode[side]);
                choiceLabel.addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeMode(side);
                    }
                });

                Label minLabel = new Label(mc, this).setText("R:");
                minText[side] = new TextField(mc, this).setDesiredHeight(12).setDesiredWidth(24).addTextEvent(new TextEvent() {
                    @Override
                    public void textChanged(Widget parent, String newText) {
                        setMinimumRarity(side);
                    }
                });
                minText[side].setText(Integer.toString(minRarity[side]));
                maxText[side] = new TextField(mc, this).setDesiredHeight(12).setDesiredWidth(24).addTextEvent(new TextEvent() {
                    @Override
                    public void textChanged(Widget parent, String newText) {
                        setMaximumRarity(side);
                    }
                });
                maxText[side].setText(Integer.toString(maxRarity[side]));

                types[side] = new ChoiceLabel(mc, this).setDesiredHeight(12).setDesiredWidth(68).addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        setType(side);
                    }
                });
                types[side].addChoices("*");
                for (DimletType type : DimletType.values()) {
                    types[side].addChoices(type.dimletType.getName());
                }
                if (dimletTypes[side] == null) {
                    types[side].setChoice("*");
                } else {
                    types[side].setChoice(dimletTypes[side].dimletType.getName());
                }

                panel.addChild(choiceLabel).addChild(minLabel).addChild(minText[side]).addChild(maxText[side]).addChild(types[side]);

                toplevel.addChild(panel);

            }
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void setMinimumRarity(int side) {
        int value = 0;
        try {
            value = Integer.parseInt(minText[side].getText());
            if (value < 0) {
                value = 0;
            } else if (value > 6) {
                value = 6;
            }
        } catch (NumberFormatException e) {
            value = 0;
        }
        sendServerCommand(DimletFilterTileEntity.CMD_SETMINRARITY,
                new Argument("side", side),
                new Argument("value", value));
    }

    private void setMaximumRarity(int side) {
        int value = 0;
        try {
            value = Integer.parseInt(maxText[side].getText());
            if (value < 0) {
                value = 0;
            } else if (value > 6) {
                value = 6;
            }
        } catch (NumberFormatException e) {
            value = 0;
        }
        sendServerCommand(DimletFilterTileEntity.CMD_SETMAXRARITY,
                new Argument("side", side),
                new Argument("value", value));
    }

    private void setType(int side) {
        String choice = types[side].getCurrentChoice();
        int type;
        if ("*".equals(choice)) {
            type = -1;
        } else {
            type = DimletType.getTypeByName(choice).ordinal();
        }
        sendServerCommand(DimletFilterTileEntity.CMD_SETTYPE,
                new Argument("side", side),
                new Argument("type", type));
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

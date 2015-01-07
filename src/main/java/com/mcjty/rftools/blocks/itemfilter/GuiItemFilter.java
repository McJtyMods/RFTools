package com.mcjty.rftools.blocks.itemfilter;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.ChoiceLabel;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiItemFilter extends GenericGuiContainer<ItemFilterTileEntity> {
    public static final int ITEMFILTER_WIDTH = 181;
    public static final int ITEMFILTER_HEIGHT = 224;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/itemfilter.png");

    public static final String DISABLED = "Disabled";
    public static final String INPUT = "Input";
    public static final String INPUT_EXACT = "Input Exact";
    public static final String OUTPUT = "Output";
    public static final String OUTPUT_EXACT = "Output Exact";

    private ChoiceLabel inputMode[] = new ChoiceLabel[6];

    public GuiItemFilter(ItemFilterTileEntity itemFilterTileEntity, ItemFilterContainer container) {
        super(itemFilterTileEntity, container);

        xSize = ITEMFILTER_WIDTH;
        ySize = ITEMFILTER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        byte[] modes = tileEntity.getInputMode();
        for (ForgeDirection direction : ForgeDirection.values()) {
            if (!ForgeDirection.UNKNOWN.equals(direction)) {
                final int i = direction.ordinal();
                inputMode[i] = new ChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(94, 6 + i * 18, 80, 16)).addChoices(DISABLED, INPUT, INPUT_EXACT, OUTPUT, OUTPUT_EXACT).
                        addChoiceEvent(new ChoiceEvent() {
                            @Override
                            public void choiceChanged(Widget parent, String newChoice) {
                                changeMode(i, newChoice);
                            }
                        });
                inputMode[i].setChoiceTooltip(DISABLED, "No input/output on this side");
                inputMode[i].setChoiceTooltip(INPUT, "Items can come from this side", "and go to any matching slot");
                inputMode[i].setChoiceTooltip(INPUT_EXACT, "Items can come from this side", "and go only to this slot");
                inputMode[i].setChoiceTooltip(OUTPUT, "Items can come out of this side", "from any slot");
                inputMode[i].setChoiceTooltip(OUTPUT_EXACT, "Items can come out of this side", "but only from this slot");
                if (modes[i] == ItemFilterTileEntity.MODE_DISABLED) {
                    inputMode[i].setChoice(DISABLED);
                } else if (modes[i] == ItemFilterTileEntity.MODE_OUTPUT) {
                    inputMode[i].setChoice(OUTPUT);
                } else if (modes[i] == ItemFilterTileEntity.MODE_OUTPUT_EXACT) {
                    inputMode[i].setChoice(OUTPUT_EXACT);
                } else if (modes[i] == ItemFilterTileEntity.MODE_INPUT) {
                    inputMode[i].setChoice(INPUT);
                } else {
                    inputMode[i].setChoice(INPUT_EXACT);
                }
                toplevel.addChild(inputMode[i]);
            }
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void changeMode(int i, String newChoice) {
        int m;
        if (INPUT.equals(newChoice)) {
            m = ItemFilterTileEntity.MODE_INPUT;
        } else if (INPUT_EXACT.equals(newChoice)) {
            m = ItemFilterTileEntity.MODE_INPUT_EXACT;
        } else if (OUTPUT.equals(newChoice)) {
            m = ItemFilterTileEntity.MODE_OUTPUT;
        } else if (OUTPUT_EXACT.equals(newChoice)) {
            m = ItemFilterTileEntity.MODE_OUTPUT_EXACT;
        } else {
            m = ItemFilterTileEntity.MODE_DISABLED;
        }
        sendServerCommand(ItemFilterTileEntity.CMD_SETMODE,
                new Argument("index", i),
                new Argument("input", m));

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}

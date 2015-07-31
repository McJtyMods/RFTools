package mcjty.rftools.blocks.itemfilter;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.ImageChoiceLabel;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import mcjty.network.Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiItemFilter extends GenericGuiContainer<ItemFilterTileEntity> {
    public static final int ITEMFILTER_WIDTH = 195;
    public static final int ITEMFILTER_HEIGHT = 212;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/itemfilter.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ImageChoiceLabel[] bits = new ImageChoiceLabel[ItemFilterContainer.BUFFER_SIZE * 6];

    public GuiItemFilter(ItemFilterTileEntity itemFilterTileEntity, ItemFilterContainer container) {
        super(RFTools.instance, itemFilterTileEntity, container, RFTools.GUI_MANUAL_MAIN, "filter");

        xSize = ITEMFILTER_WIDTH;
        ySize = ITEMFILTER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        int[] inputMode = tileEntity.getInputMode();
        int[] outputMode = tileEntity.getOutputMode();

        for (ForgeDirection direction : ForgeDirection.values()) {
            if (!ForgeDirection.UNKNOWN.equals(direction)) {
                final int side = direction.ordinal();
                for (int slot = 0 ; slot < ItemFilterContainer.BUFFER_SIZE ; slot++) {
                    ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(25 + slot * 18, 4 + side * 13, 12, 12)).
                            addChoice("0", "Disabled", iconGuiElements, 160, 0).
                            addChoice("1", "Input", iconGuiElements, 96, 16).
                            addChoice("2", "Output", iconGuiElements, 80, 16);
                    bits[side * ItemFilterContainer.BUFFER_SIZE + slot] = choiceLabel;
                    if ((inputMode[side] & (1<<slot)) != 0) {
                        choiceLabel.setCurrentChoice(1);
                    } else if ((outputMode[side] & (1<<slot)) != 0) {
                        choiceLabel.setCurrentChoice(2);
                    } else {
                        choiceLabel.setCurrentChoice(0);
                    }
                    final int finalSlot = slot;
                    choiceLabel.addChoiceEvent(new ChoiceEvent() {
                        @Override
                        public void choiceChanged(Widget parent, String newChoice) {
                            changeMode(side, finalSlot);
                        }
                    });
                    toplevel.addChild(choiceLabel);
                }
            }
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }



    private void changeMode(int side, int slot) {
        ImageChoiceLabel choiceLabel = bits[side * ItemFilterContainer.BUFFER_SIZE + slot];
        int c = choiceLabel.getCurrentChoiceIndex();
        boolean input = false;
        boolean output = false;
        if (c == 1) {
            input = true;
            output = false;
        } else if (c == 2) {
            input = false;
            output = true;
        }
        sendServerCommand(ItemFilterTileEntity.CMD_SETMODE,
                new Argument("side", side),
                new Argument("slot", slot),
                new Argument("input", input),
                new Argument("output", output));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}

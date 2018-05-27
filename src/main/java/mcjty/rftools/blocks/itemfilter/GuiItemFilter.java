package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

import static mcjty.rftools.blocks.itemfilter.ItemFilterTileEntity.*;

public class GuiItemFilter extends GenericGuiContainer<ItemFilterTileEntity> {
    public static final int ITEMFILTER_WIDTH = 195;
    public static final int ITEMFILTER_HEIGHT = 212;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/itemfilter.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ImageChoiceLabel[] bits = new ImageChoiceLabel[ItemFilterTileEntity.BUFFER_SIZE * 6];

    public GuiItemFilter(ItemFilterTileEntity itemFilterTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, itemFilterTileEntity, container, RFTools.GUI_MANUAL_MAIN, "filter");

        xSize = ITEMFILTER_WIDTH;
        ySize = ITEMFILTER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        int[] inputMode = tileEntity.getInputMode();
        int[] outputMode = tileEntity.getOutputMode();

        for (EnumFacing direction : EnumFacing.VALUES) {
            final int side = direction.ordinal();
            for (int slot = 0; slot < ItemFilterTileEntity.BUFFER_SIZE ; slot++) {
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(25 + slot * 18, 4 + side * 13, 12, 12)).
                        addChoice("0", "Disabled", iconGuiElements, 160, 0).
                        addChoice("1", "Input", iconGuiElements, 96, 16).
                        addChoice("2", "Output", iconGuiElements, 80, 16);
                bits[side * ItemFilterTileEntity.BUFFER_SIZE + slot] = choiceLabel;
                if ((inputMode[side] & (1<<slot)) != 0) {
                    choiceLabel.setCurrentChoice(1);
                } else if ((outputMode[side] & (1<<slot)) != 0) {
                    choiceLabel.setCurrentChoice(2);
                } else {
                    choiceLabel.setCurrentChoice(0);
                }
                final int finalSlot = slot;
                choiceLabel.addChoiceEvent((parent, newChoice) -> changeMode(side, finalSlot));
                toplevel.addChild(choiceLabel);
            }
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }



    private void changeMode(int side, int slot) {
        ImageChoiceLabel choiceLabel = bits[side * ItemFilterTileEntity.BUFFER_SIZE + slot];
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
        sendServerCommand(RFToolsMessages.INSTANCE, ItemFilterTileEntity.CMD_SETMODE,
                TypedMap.builder()
                        .put(PARAM_SIDE, side)
                        .put(PARAM_SLOT, slot)
                        .put(PARAM_INPUT, input)
                        .put(PARAM_OUTPUT, output)
                        .build());
    }
}

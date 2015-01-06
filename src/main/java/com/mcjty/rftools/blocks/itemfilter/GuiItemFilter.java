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
                inputMode[i] = new ChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(100, 6 + i * 18, 60, 16)).addChoices("Disabled", "Input", "Output").addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeMode(i, newChoice);
                    }
                });
                if (modes[i] == ItemFilterTileEntity.MODE_DISABLED) {
                    inputMode[i].setChoice("Disabled");
                } else if (modes[i] == ItemFilterTileEntity.MODE_OUTPUT) {
                    inputMode[i].setChoice("Output");
                } else {
                    inputMode[i].setChoice("Input");
                }
                toplevel.addChild(inputMode[i]);
            }
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void changeMode(int i, String newChoice) {
        int m;
        if ("Input".equals(newChoice)) {
            m = ItemFilterTileEntity.MODE_INPUT;
        } else if ("Output".equals(newChoice)) {
            m = ItemFilterTileEntity.MODE_OUTPUT;
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

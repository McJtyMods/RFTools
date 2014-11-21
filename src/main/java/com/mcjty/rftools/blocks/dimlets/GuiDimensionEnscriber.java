package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.RealizedDimensionTab;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiDimensionEnscriber extends GenericGuiContainer<DimensionEnscriberTileEntity> {
    public static final int ENSCRIBER_WIDTH = 256;
    public static final int ENSCRIBER_HEIGHT = 224;

    private Button extractButton;
    private Button storeButton;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimensionenscriber.png");

    public GuiDimensionEnscriber(DimensionEnscriberTileEntity dimensionEnscriberTileEntity, DimensionEnscriberContainer container) {
        super(dimensionEnscriberTileEntity, container);

        xSize = ENSCRIBER_WIDTH;
        ySize = ENSCRIBER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        extractButton = new Button(mc, this).setText("Extract").setLayoutHint(new PositionalLayout.PositionalHint(13, 164, 60, 18)).addButtonEvent(
                new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        extractDimlets();
                    }
                }
        );
        storeButton = new Button(mc, this).setText("Store").setLayoutHint(new PositionalLayout.PositionalHint(13, 184, 60, 18)).addButtonEvent(
                new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        storeDimlets();
                    }
                }
        );

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(extractButton).addChild(storeButton);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void extractDimlets() {
        for (int i = 0 ; i < DimensionEnscriberContainer.SIZE_DIMLETS ; i++) {
            ItemStack stack = inventorySlots.getSlot(i + DimensionEnscriberContainer.SLOT_DIMLETS).getStack();
            if (stack != null && stack.stackSize > 0) {
                // Cannot extract. There are still items in the way.
                RFTools.warn(mc.thePlayer, "You cannot extract. Remove all dimlets first!");
                return;
            }
        }
        sendServerCommand(DimensionEnscriberTileEntity.CMD_EXTRACT);
    }

    private void storeDimlets() {
        sendServerCommand(DimensionEnscriberTileEntity.CMD_STORE);
    }

    private void enableButtons() {
        Slot slot = inventorySlots.getSlot(DimensionEnscriberContainer.SLOT_TAB);
        extractButton.setEnabled(false);
        storeButton.setEnabled(false);
        if (slot.getStack() != null) {
            if (slot.getStack().getItem() == ModItems.emptyDimensionTab) {
                storeButton.setEnabled(true);
            } else if (slot.getStack().getItem() == ModItems.realizedDimensionTab) {
                extractButton.setEnabled(true);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        enableButtons();
        window.draw();
    }
}

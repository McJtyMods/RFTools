package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.network.Argument;
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
    private TextField nameField;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimensionenscriber.png");

    public GuiDimensionEnscriber(DimensionEnscriberTileEntity dimensionEnscriberTileEntity, DimensionEnscriberContainer container) {
        super(dimensionEnscriberTileEntity, container);

        xSize = ENSCRIBER_WIDTH;
        ySize = ENSCRIBER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        extractButton = new Button(mc, this).setText("Extract").setLayoutHint(new PositionalLayout.PositionalHint(13, 164, 60, 16)).addButtonEvent(
                new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        extractDimlets();
                    }
                }
        ).setTooltips("Extract the dimlets out of", "a realized dimension tab");
        storeButton = new Button(mc, this).setText("Store").setLayoutHint(new PositionalLayout.PositionalHint(13, 182, 60, 16)).addButtonEvent(
                new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        storeDimlets();
                    }
                }
        ).setTooltips("Store dimlets in a", "empty dimension tab");
        nameField = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                storeName(newText);
            }
        }).setLayoutHint(new PositionalLayout.PositionalHint(13, 200, 60, 16));
        setNameFromDimensionTab();

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(extractButton).addChild(storeButton).
                addChild(nameField);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void storeName(String name) {
        sendServerCommand(DimensionEnscriberTileEntity.CMD_SETNAME, new Argument("name", name));
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
        sendServerCommand(DimensionEnscriberTileEntity.CMD_STORE, new Argument("name", nameField.getText()));
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

        if (tileEntity.hasTabSlotChangedAndClear()) {
            setNameFromDimensionTab();
        }

        window.draw();
    }

    private void setNameFromDimensionTab() {
        Slot slot = inventorySlots.getSlot(DimensionEnscriberContainer.SLOT_TAB);
        if (slot.getStack() != null && slot.getStack().getItem() == ModItems.realizedDimensionTab) {
            NBTTagCompound tagCompound = slot.getStack().getTagCompound();
            if (tagCompound != null) {
                String name = tagCompound.getString("name");
                if (name != null) {
                    nameField.setText(name);
                }
            }
        }
    }
}

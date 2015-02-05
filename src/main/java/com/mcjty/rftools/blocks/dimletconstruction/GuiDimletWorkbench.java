package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiDimletWorkbench extends GenericGuiContainer<DimletWorkbenchTileEntity> {
    public static final int WORKBENCH_WIDTH = 200;
    public static final int WORKBENCH_HEIGHT = 224;

    private Button extractButton;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimletworkbench.png");

    public GuiDimletWorkbench(DimletWorkbenchTileEntity dimletWorkbenchTileEntity, DimletWorkbenchContainer container) {
        super(dimletWorkbenchTileEntity, container);

        xSize = WORKBENCH_WIDTH;
        ySize = WORKBENCH_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        extractButton = new Button(mc, this).setText("Extract").setLayoutHint(new PositionalLayout.PositionalHint(13, 40, 60, 16)).addButtonEvent(
                new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                    }
                }
        ).setTooltips("Extract the dimlets out of", "a realized dimension tab");

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(extractButton);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void enableButtons() {
//        Slot slot = inventorySlots.getSlot(DimensionEnscriberContainer.SLOT_TAB);
//        extractButton.setEnabled(false);
//        storeButton.setEnabled(false);
//        if (slot.getStack() != null) {
//            if (slot.getStack().getItem() == ModItems.emptyDimensionTab) {
//                storeButton.setEnabled(true);
//            } else if (slot.getStack().getItem() == ModItems.realizedDimensionTab) {
//                extractButton.setEnabled(true);
//            }
//        }
    }



    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        enableButtons();

        window.draw();
    }
}

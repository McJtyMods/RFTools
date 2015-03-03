package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.Achievements;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.rftools.network.Argument;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiDimletWorkbench extends GenericGuiContainer<DimletWorkbenchTileEntity> {
    public static final int WORKBENCH_WIDTH = 200;
    public static final int WORKBENCH_HEIGHT = 224;

    private EnergyBar energyBar;
    private Button extractButton;
    private ToggleButton autoExtract;
    private ImageLabel progressIcon;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimletworkbench.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiDimletWorkbench(DimletWorkbenchTileEntity dimletWorkbenchTileEntity, DimletWorkbenchContainer container) {
        super(dimletWorkbenchTileEntity, container);

        xSize = WORKBENCH_WIDTH;
        ySize = WORKBENCH_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(8, 142, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        progressIcon = new ImageLabel(mc, this).setImage(iconGuiElements, 4 * 16, 16);
        progressIcon.setLayoutHint(new PositionalLayout.PositionalHint(135, 6, 16, 16));

        extractButton = new Button(mc, this).setText("Extract").setLayoutHint(new PositionalLayout.PositionalHint(40, 7, 55, 14)).addButtonEvent(
                new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        extractDimlet();
                    }
                }
        ).setTooltips("Deconstruct a dimlet into its parts");

        autoExtract = new ToggleButton(mc, this).setText("Auto").setLayoutHint(new PositionalLayout.PositionalHint(100, 7, 30, 14)).addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                setAutoExtract();
            }
        }).setTooltips("Automatically extract").setCheckMarker(true);

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(extractButton).addChild(energyBar).addChild(progressIcon).addChild(autoExtract);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void setAutoExtract() {
        sendServerCommand(DimletWorkbenchTileEntity.CMD_SETAUTOEXTRACT, new Argument("auto", autoExtract.isPressed()));
    }

    private void extractDimlet() {
        Slot slot = inventorySlots.getSlot(DimletWorkbenchContainer.SLOT_INPUT);
        if (slot.getStack() != null) {
            ItemStack itemStack = slot.getStack();
            if (ModItems.knownDimlet.equals(itemStack.getItem())) {
                int id = itemStack.getItemDamage();
                if (!KnownDimletConfiguration.craftableDimlets.contains(id)) {
                    Achievements.trigger(Minecraft.getMinecraft().thePlayer, Achievements.smallBits);
                    sendServerCommand(DimletWorkbenchTileEntity.CMD_STARTEXTRACT);
                }
            }
        }
    }

    private void enableButtons() {
        boolean enabled = false;
        Slot slot = inventorySlots.getSlot(DimletWorkbenchContainer.SLOT_INPUT);
        if (slot.getStack() != null) {
            ItemStack itemStack = slot.getStack();
            if (ModItems.knownDimlet.equals(itemStack.getItem())) {
                int id = itemStack.getItemDamage();
                if (!KnownDimletConfiguration.craftableDimlets.contains(id)) {
                    enabled = true;
                }
            }
        }
        extractButton.setEnabled(enabled);
    }



    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        enableButtons();

        int extracting = tileEntity.getExtracting();
        if (extracting == 0) {
            progressIcon.setImage(iconGuiElements, 4 * 16, 16);
        } else {
            progressIcon.setImage(iconGuiElements, (extracting % 4) * 16, 16);
        }

        window.draw();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer();
        tileEntity.requestExtractingFromServer();
    }
}

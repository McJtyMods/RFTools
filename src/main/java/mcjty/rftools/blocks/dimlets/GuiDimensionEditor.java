package mcjty.rftools.blocks.dimlets;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.varia.BlockTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiDimensionEditor extends GenericGuiContainer<DimensionEditorTileEntity> {
    public static final int EDITOR_WIDTH = 180;
    public static final int EDITOR_HEIGHT = 152;

    private EnergyBar energyBar;
    private ImageLabel arrow;
    private Label percentage;
    private Label destroy;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimensioneditor.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiDimensionEditor(DimensionEditorTileEntity dimensionEditorTileEntity, DimensionEditorContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, dimensionEditorTileEntity, container, RFTools.GUI_MANUAL_DIMENSION, "editor");
        GenericEnergyStorageTileEntity.setCurrentRF(dimensionEditorTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = EDITOR_WIDTH;
        ySize = EDITOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        arrow = new ImageLabel(mc, this).setImage(iconGuiElements, 192, 0);
        arrow.setLayoutHint(new PositionalLayout.PositionalHint(90, 26, 16, 16));

        destroy = new Label(mc, this).setColor(0xff0000);
        destroy.setText("Destroying dimension!");
        destroy.setLayoutHint(new PositionalLayout.PositionalHint(30, 53, 150, 16));
        destroy.setVisible(false);


        percentage = new Label(mc, this).setText("0%");
        percentage.setLayoutHint(new PositionalLayout.PositionalHint(80, 43, 40, 16));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(arrow).addChild(percentage).addChild(destroy);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
        tileEntity.requestBuildingPercentage();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        int pct = DimensionEditorTileEntity.getEditPercentage();
        if (pct > 0) {
            arrow.setImage(iconGuiElements, 144, 0);
        } else {
            arrow.setImage(iconGuiElements, 192, 0);
        }
        percentage.setText(pct + "%");

        drawWindow();

        destroy.setVisible(false);
        Slot slot = this.inventorySlots.getSlot(DimensionEditorContainer.SLOT_INJECTINPUT);
        if (slot.getHasStack()) {
            Block block = BlockTools.getBlock(slot.getStack());
            if (block == Blocks.tnt) {
                destroy.setVisible(true);
            }
        }

        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
        tileEntity.requestBuildingPercentage();
    }
}

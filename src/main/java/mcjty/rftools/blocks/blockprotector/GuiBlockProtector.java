package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

public class GuiBlockProtector extends GenericGuiContainer<BlockProtectorTileEntity> {
    private EnergyBar energyBar;

    public GuiBlockProtector(BlockProtectorTileEntity blockProtectorTileEntity, BlockProtectorContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, blockProtectorTileEntity, container, RFTools.GUI_MANUAL_MAIN, "protect");
        GenericEnergyStorageTileEntity.setCurrentRF(blockProtectorTileEntity.getStoredPower());
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/block_protector.gui"));
        super.initGui();

        initializeFields();

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");

        energyBar.setMaxValue(tileEntity.getCapacity());
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();

        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}

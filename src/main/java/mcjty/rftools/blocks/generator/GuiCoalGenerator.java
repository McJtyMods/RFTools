package mcjty.rftools.blocks.generator;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import static mcjty.lib.entity.GenericEnergyStorageTileEntity.getCurrentRF;

public class GuiCoalGenerator extends GenericGuiContainer<CoalGeneratorTileEntity> {

    private EnergyBar energyBar;

    public GuiCoalGenerator(CoalGeneratorTileEntity tileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, tileEntity, container, RFTools.GUI_MANUAL_MAIN, "coalgen");
        GenericEnergyStorageTileEntity.setCurrentRF(tileEntity.getEnergyStored());
    }

    @Override
    public void initGui() {
        window = new Window(this, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/coalgenerator.gui"));
        super.initGui();

        initializeFields();

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
        energyBar.setMaxValue(tileEntity.getMaxEnergyStored());
        energyBar.setValue(getCurrentRF());

        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();

        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}

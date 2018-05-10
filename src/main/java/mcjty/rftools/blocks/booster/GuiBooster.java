package mcjty.rftools.blocks.booster;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

public class GuiBooster extends GenericGuiContainer<BoosterTileEntity> {

    private EnergyBar energyBar;

    public GuiBooster(BoosterTileEntity boosterTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, boosterTileEntity, container, RFTools.GUI_MANUAL_MAIN, "booster");
        GenericEnergyStorageTileEntity.setCurrentRF(boosterTileEntity.getEnergyStored());
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/booster.gui"));
        super.initGui();

        initializeFields();
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");

        energyBar.setMaxValue(tileEntity.getMaxEnergyStored());
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        drawWindow();
    }
}

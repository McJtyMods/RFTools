package mcjty.rftools.blocks.booster;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;

public class GuiBooster extends GenericGuiContainer<BoosterTileEntity, GenericContainer> {

    private EnergyBar energyBar;

    public GuiBooster(BoosterTileEntity boosterTileEntity, GenericContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, boosterTileEntity, container, inventory, GuiProxy.GUI_MANUAL_MAIN, "booster");
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/booster.gui"));
        super.init();

        initializeFields();
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
            energyBar.setMaxValue(((GenericEnergyStorage)e).getCapacity());
            energyBar.setValue(((GenericEnergyStorage)e).getEnergy());
        });

    }
}

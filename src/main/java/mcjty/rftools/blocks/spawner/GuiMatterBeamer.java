package mcjty.rftools.blocks.spawner;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;

import java.awt.*;

public class GuiMatterBeamer extends GenericGuiContainer<MatterBeamerTileEntity, GenericContainer> {
    private static final int BEAMER_WIDTH = 180;
    private static final int BEAMER_HEIGHT = 152;

    private EnergyBar energyBar;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/matterbeamer.png");

    public GuiMatterBeamer(MatterBeamerTileEntity beamerTileEntity, GenericContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, beamerTileEntity, container, inventory, GuiProxy.GUI_MANUAL_MAIN, "spawner");

        xSize = BEAMER_WIDTH;
        ySize = BEAMER_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        energyBar = new EnergyBar(minecraft, this).setVertical().setLayoutHint(10, 7, 8, 54).setShowText(false);

        Panel toplevel = new Panel(minecraft, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
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

package mcjty.rftools.blocks.spawner;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiMatterBeamer extends GenericGuiContainer<MatterBeamerTileEntity> {
    private static final int BEAMER_WIDTH = 180;
    private static final int BEAMER_HEIGHT = 152;

    private EnergyBar energyBar;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/matterbeamer.png");

    public GuiMatterBeamer(MatterBeamerTileEntity beamerTileEntity, MatterBeamerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, beamerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "spawner");
        GenericEnergyStorageTileEntity.setCurrentRF(beamerTileEntity.getEnergyStored());

        xSize = BEAMER_WIDTH;
        ySize = BEAMER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored();
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(10, 7, 8, 54).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}
